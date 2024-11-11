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
import java.util.List;
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

    private boolean noTimestamps = false;

    /**
     * Main method
     *
     * @param args args
     */
    public static void main(String[] args) {}

    /** Constructor */
    public Runner() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to set noTimestamps
     *
     * @param noTimestamps noTimestamps
     */
    public void setNoTimestamps(boolean noTimestamps) {
        this.noTimestamps = noTimestamps;
    }

    /**
     * Method to run a list of pipeline YAML files
     *
     * @param filenames filenames
     * @return the exit code
     */
    public int run(List<String> filenames) {
        if (filenames == null) {
            return 1;
        }

        for (String filename : filenames) {
            if (!filename.trim().isEmpty()) {
                int exitCode = run(filename.trim());
                if (exitCode != 0) {
                    return exitCode;
                }
            }
        }

        return 0;
    }

    /**
     * Method to run a list of pipeline YAML file
     *
     * @param filename filename
     * @return the exit code
     */
    private int run(String filename) {
        Stopwatch runnerStopwatch = new Stopwatch();
        Stopwatch jobStopwatch = new Stopwatch();
        Stopwatch stepStopwatch = new Stopwatch();

        Pipeline pipeline = null;
        int pipelineExitCode = 0;

        info("info Verifyica Pipeline " + version());
        info("info filename=[%s]", new File(filename).getAbsoluteFile());

        try {
            pipeline = PipelineFactory.createPipeline(filename);
        } catch (Throwable e) {
            error("YAML format error, filename=[%s]", filename);
            e.printStackTrace(System.err);
            System.exit(1);
        }

        info("pipeline name=[%s]", pipeline.getName());

        for (Job job : pipeline.getJob()) {
            if (job.isEnabled()) {
                jobStopwatch.reset();
                int jobExitCode = 0;
                info("job name=[%s]", job.getName());

                for (Step step : job.getStep()) {
                    if (step.isEnabled()) {
                        info("step name=[%s]", step.getName());
                        stepStopwatch.reset();
                        run(pipeline, job, step, System.out, System.err);
                        info(
                                "step name=[%s] exit.code=[%d] ms=[%d]",
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
                        "job name=[%s] exit.code=[%d] ms=[%d]",
                        job.getName(), jobExitCode, jobStopwatch.elapsedTime().toMillis());
            }
        }

        info(
                "pipeline name=[%s] exit.code=[%d] ms=[%d]",
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
    private void run(Pipeline pipeline, Job job, Step step, PrintStream outPrintStream, PrintStream errorPrintStream) {
        Map<String, String> properties = merge(pipeline.getProperties(), job.getProperties(), step.getProperties());

        String command = Replacer.replace(properties, true, step.getRun());

        if (noTimestamps) {
            outPrintStream.println("$ " + command);
        } else {
            outPrintStream.println(Timestamp.now() + " $ " + command);
        }

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
                pipe(outputBufferedReader, outPrintStream);
                pipe(errorBufferedReader, errorPrintStream);
            }

            step.setExitCode(process.waitFor());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(errorPrintStream);
            step.setExitCode(1);
        }
    }

    /**
     * Method to read and log output
     *
     * @param bufferedReader bufferedReader
     * @param printStream printStream
     */
    private void pipe(BufferedReader bufferedReader, PrintStream printStream) throws IOException {
        String line;
        String[] tokens;

        while ((line = bufferedReader.readLine()) != null) {
            tokens = line.split("\\R");
            for (String token : tokens) {
                if (noTimestamps) {
                    printStream.println("> " + token);
                } else {
                    printStream.println(Timestamp.now() + " > " + token);
                }
            }
        }
    }

    /**
     * Method to log info output
     *
     * @param format format
     * @param objects objects
     */
    private void info(String format, Object... objects) {
        if (noTimestamps) {
            System.out.printf("@" + format + "%n", objects);
        } else {
            System.out.printf(Timestamp.now() + " @" + format + "%n", objects);
        }
    }

    /**
     * Method to log error output
     *
     * @param format format
     * @param objects objects
     */
    private void error(String format, Object... objects) {
        if (noTimestamps) {
            System.err.printf("@" + format + "%n", objects);
        } else {
            System.err.printf(Timestamp.now() + " @" + format + "%n", objects);
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
}
