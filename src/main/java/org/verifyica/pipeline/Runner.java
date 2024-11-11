/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.verifyica.pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.verifyica.pipeline.common.Replacer;
import org.verifyica.pipeline.common.Stopwatch;
import org.verifyica.pipeline.common.Timestamp;

/** Class to implement Runner */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Runner {

    private static final String PROPERTIES_RESOURCE = "/pipeline.properties";

    private static final String KEY_VERSION = "version";

    private static final String VALUE_UNKNOWN = "unknown";

    /**
     * Main method
     *
     * @param args args
     */
    public static void main(String[] args) {
        if (args == null) {
            System.exit(1);
        }

        if (args.length == 0) {
            System.exit(2);
        }

        for (String argument : args) {
            if (!argument.trim().isEmpty()) {
                int exitCode = run(argument);
                if (exitCode != 0) {
                    System.exit(exitCode);
                    break;
                }
            }
        }

        System.exit(0);
    }

    /** Constructor */
    private Runner() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to run a pipeline YAML file
     *
     * @param pipelineYamlFilename pipelineYamlFilename
     * @return the exit code
     */
    public static int run(String pipelineYamlFilename) {
        Stopwatch runnerStopwatch = new Stopwatch();
        Stopwatch jobStopwatch = new Stopwatch();
        Stopwatch stepStopwatch = new Stopwatch();

        Pipeline pipeline = null;
        int pipelineExitCode = 0;

        info("Info Verifyica Pipeline " + version());
        info("Info YAML {\"%s\"}", new File(pipelineYamlFilename).getAbsoluteFile());

        try {
            pipeline = PipelineFactory.createPipeline(pipelineYamlFilename);
        } catch (Throwable e) {
            error("YAML [%s] format error", pipelineYamlFilename);
            e.printStackTrace(System.err);
            System.exit(1);
        }

        info("Pipeline {\"%s\"}", pipeline.getName());

        for (Job job : pipeline.getJob()) {
            if (job.isEnabled()) {
                jobStopwatch.reset();
                int jobExitCode = 0;
                info("Job {\"%s\"}", job.getName());

                for (Step step : job.getStep()) {
                    if (step.isEnabled()) {
                        info("Step {\"%s\"}", step.getName());
                        stepStopwatch.reset();
                        run(pipeline, job, step, System.out, System.err);
                        info(
                                "Step {\"%s\"} exit.code=%d %d ms",
                                step.getName(),
                                step.getExitCode(),
                                stepStopwatch.elapsedTime().toMillis());
                        if (step.getExitCode() != 0) {
                            jobExitCode = step.getExitCode();
                            pipelineExitCode = jobExitCode;
                            break;
                        }
                    }
                }

                info(
                        "Job {\"%s\"} exit.code=%d %d ms ",
                        job.getName(), jobExitCode, jobStopwatch.elapsedTime().toMillis());
            }
        }

        info(
                "Pipeline {\"%s\"} exit.code=%d %d ms ",
                pipeline.getName(),
                pipelineExitCode,
                runnerStopwatch.elapsedTime().toMillis());

        return pipelineExitCode;
    }

    /**
     * Method to run the step
     *
     * @param pipeline pipeline
     * @param job job
     * @param step step
     * @param outPrintStream outPrintStream
     * @param errorPrintStream errorPrintStream
     */
    private static void run(
            Pipeline pipeline, Job job, Step step, PrintStream outPrintStream, PrintStream errorPrintStream) {
        Map<String, String> properties = merge(pipeline.getProperties(), job.getProperties(), step.getProperties());

        String command = Replacer.replace(properties, true, step.getRun());
        outPrintStream.println(Timestamp.now() + " $ " + command);

        Map<String, String> properties2 = merge(System.getenv(), properties);

        String workingDirectory = Replacer.replace(properties, true, step.getWorkingDirectory());
        String command2 = Replacer.replace(properties2, true, step.getRun());

        Map<String, String> environmentVariables = merge(
                System.getenv(),
                pipeline.getEnvironmentVariables(),
                job.getEnvironmentVariables(),
                step.getEnvironmentVariables());

        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.environment().putAll(environmentVariables);
        processBuilder.directory(new File(workingDirectory));
        processBuilder.command("bash", "-e", "-c", command2);

        try {
            Process process;

            try {
                process = processBuilder.start();
            } catch (Throwable t) {
                processBuilder.environment().putAll(environmentVariables);
                processBuilder.directory(new File(workingDirectory));
                processBuilder.command("sh", "-e", "-c", command2);
                process = processBuilder.start();
            }

            try (BufferedReader outputBufferedReader =
                            new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorBufferedReader =
                            new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                log(outputBufferedReader, outPrintStream);
                log(errorBufferedReader, errorPrintStream);
            }

            step.setExitCode(process.waitFor());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(errorPrintStream);
            step.setExitCode(1);
        }
    }

    /**
     * Method to merge an array of Maps into a single Map
     *
     * @param maps maps
     * @return a merged Map
     */
    @SafeVarargs
    private static Map<String, String> merge(Map<String, String>... maps) {
        Map<String, String> mergedMap = new LinkedHashMap<>();

        for (Map<String, String> map : maps) {
            mergedMap.putAll(map);
        }

        return mergedMap;
    }

    /**
     * Method to read and log output
     *
     * @param bufferedReader bufferedReader
     * @param printStream printStream
     */
    private static void log(BufferedReader bufferedReader, PrintStream printStream) throws IOException {
        String line;
        String[] tokens;

        while ((line = bufferedReader.readLine()) != null) {
            tokens = line.split("\\R");
            for (String token : tokens) {
                printStream.println(Timestamp.now() + " > " + token);
            }
        }
    }

    /**
     * Method to return the version
     *
     * @return the version
     */
    private static String version() {
        String value = VALUE_UNKNOWN;

        try (InputStream inputStream = Runner.class.getResourceAsStream(PROPERTIES_RESOURCE)) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                value = properties.getProperty(KEY_VERSION).trim();
            }
        } catch (IOException e) {
            // INTENTIONALLY BLANK
        }

        return value;
    }

    /**
     * Method to log info output
     *
     * @param format format
     * @param objects objects
     */
    private static void info(String format, Object... objects) {
        System.out.printf(Timestamp.now() + " @ " + format + "%n", objects);
    }

    /**
     * Method to log error output
     *
     * @param format format
     * @param objects objects
     */
    private static void error(String format, Object... objects) {
        System.err.printf(Timestamp.now() + " @ " + format + "%n", objects);
    }
}
