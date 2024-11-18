/*
 * Copyright (C) 2024-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.verifyica.pipeliner.common.Stopwatch;
import org.verifyica.pipeliner.io.NoOpPrintStream;
import org.verifyica.pipeliner.io.StringPrintStream;
import org.verifyica.pipeliner.model.Job;
import org.verifyica.pipeliner.model.Pipeline;
import org.verifyica.pipeliner.model.Run;
import org.verifyica.pipeliner.model.Step;

/** Class to implement Runner */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Runner {

    private static final String PIPELINER_VERSION = "PIPELINER_VERSION";

    private final Console console;
    private final Pipeline pipeline;

    /**
     * Constructor
     *
     * @param console console
     */
    public Runner(Console console, Pipeline pipeline) {
        this.console = console;
        this.pipeline = pipeline;
    }

    /**
     * Method to run a pipeline
     */
    public void run() {
        Stopwatch runnerStopwatch = new Stopwatch();
        Stopwatch jobStopwatch = new Stopwatch();
        Stopwatch stepStopwatch = new Stopwatch();

        console.trace("running pipeline ...");

        console.log(
                "@pipeline name=[%s] id=[%s] location=[%s]",
                pipeline.getName(), pipeline.getId(), pipeline.getLocation());

        for (Job job : pipeline.getJobs()) {
            if (job.isEnabled()) {
                jobStopwatch.reset();

                console.log("@job name=[%s] id=[%s] location=[%s]", job.getName(), job.getId(), job.getLocation());

                for (Step step : job.getSteps()) {
                    if (step.isEnabled()) {
                        console.log(
                                "@step name=[%s] id=[%s] location=[%s]",
                                step.getName(), step.getId(), step.getLocation());

                        stepStopwatch.reset();

                        run(pipeline, job, step);

                        console.log(
                                "@step name=[%s] id=[%s] location=[%s] exit-code=[%d] ms=[%d]",
                                step.getName(),
                                step.getId(),
                                step.getLocation(),
                                step.getExitCode(),
                                stepStopwatch.elapsedTime().toMillis());

                        if (step.getExitCode() != 0) {
                            job.setExitCode(step.getExitCode());
                            pipeline.setExitCode(job.getExitCode());
                            break;
                        }
                    }
                }

                console.log(
                        "@job name=[%s] id=[%s] location=[%s] exit-code=[%d] ms=[%d]",
                        job.getName(),
                        job.getId(),
                        job.getLocation(),
                        job.getExitCode(),
                        jobStopwatch.elapsedTime().toMillis());
            }
        }

        console.log(
                "@pipeline name=[%s] id=[%s] location=[%s] exit-code=[%d] ms=[%d]",
                pipeline.getName(),
                pipeline.getId(),
                pipeline.getLocation(),
                pipeline.getExitCode(),
                runnerStopwatch.elapsedTime().toMillis());
    }

    /**
     * Method to run the step
     *
     * @param pipeline pipeline
     * @param job job
     * @param step step
     */
    private void run(Pipeline pipeline, Job job, Step step) {
        List<Run> runs = step.getRuns();

        for (Run run : runs) {
            console.trace("running %s", step.getLocation());

            Map<String, String> environmentVariables = merge(
                    pipeline.getEnvironmentVariables(), job.getEnvironmentVariables(), step.getEnvironmentVariables());

            environmentVariables.putAll(System.getenv());
            environmentVariables.put(PIPELINER_VERSION, Version.getVersion());

            environmentVariables = new TreeMap<>(environmentVariables);
            environmentVariables.forEach((key, value) -> console.trace("env [%s] = [%s]", key, value));

            String workingDirectory = replaceVariables(environmentVariables, false, step.getWorkingDirectory());
            workingDirectory = replaceEnvironmentVariables(environmentVariables, false, workingDirectory);

            File workingDirectoryFile = new File(workingDirectory);
            if (!workingDirectoryFile.exists()) {
                step.setExitCode(1);
                console.log("@error working directory [%s] doesn't exist", workingDirectory);
                return;
            }

            if (!workingDirectoryFile.isDirectory()) {
                step.setExitCode(1);
                console.log("@error working directory [%s] is a file", workingDirectory);
                return;
            }

            if (!workingDirectoryFile.canRead()) {
                step.setExitCode(1);
                console.log("@error working directory [%s] is not readable", workingDirectory);
                return;
            }

            String command = replaceVariables(environmentVariables, false, run.getCommand());
            String executableCommand = replaceVariables(environmentVariables, false, run.getExecutableCommand());

            String[] shellCommandTokens;

            switch (step.getShellType()) {
                case BASH: {
                    shellCommandTokens =
                            new String[] {"bash", "--noprofile", "--norc", "-eo", "pipefail", "-c", executableCommand};
                    break;
                }
                case SH: {
                    shellCommandTokens = new String[] {"sh", "-e", "-c", executableCommand};
                    break;
                }
                default: {
                    shellCommandTokens = new String[] {"bash", "-e", "-c", executableCommand};
                    break;
                }
            }

            StringBuilder flattenShellCommand = new StringBuilder();
            for (int i = 0; i < shellCommandTokens.length; i++) {
                if (i > 0) {
                    flattenShellCommand.append(" ");
                }
                flattenShellCommand.append(shellCommandTokens[i]);
            }

            if (workingDirectory.contains("$")) {
                console.log(
                        "@error %s.working-directory [%s] has unresolved variables",
                        step.getLocation(), workingDirectory);
                step.setExitCode(1);
                return;
            }

            console.trace("working directory [%s]", workingDirectory);
            console.trace("process command [%s]", flattenShellCommand);
            console.trace("capture type [%s]", run.getCaptureType());
            console.log("$ %s", command);

            ProcessBuilder processBuilder = new ProcessBuilder();

            processBuilder.environment().putAll(environmentVariables);
            processBuilder.directory(new File(workingDirectory));
            processBuilder.command(shellCommandTokens);
            processBuilder.redirectErrorStream(true);

            try {
                Process process = processBuilder.start();

                StringBuilder outputStringBuilder = new StringBuilder();
                PrintStream capturingPrintStream;

                switch (run.getCaptureType()) {
                    case APPEND:
                    case OVERWRITE: {
                        console.trace("capture env [$%s]", run.getCaptureVariable());
                        capturingPrintStream = new StringPrintStream(outputStringBuilder);
                        break;
                    }
                    default: {
                        capturingPrintStream = new NoOpPrintStream();
                        break;
                    }
                }

                String line;
                String[] tokens;

                try (BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    boolean appendCRLF = false;
                    while ((line = bufferedReader.readLine()) != null) {
                        tokens = line.split("\\R");
                        for (String token : tokens) {
                            console.trace("output [%s]", token);

                            if (appendCRLF) {
                                capturingPrintStream.println();
                            }
                            capturingPrintStream.print(token);

                            if (run.getCaptureType() == Run.CaptureType.NONE) {
                                console.log("> %s", token);
                            }

                            appendCRLF = true;
                        }
                    }
                }

                capturingPrintStream.close();

                switch (run.getCaptureType()) {
                    case APPEND: {
                        console.trace("captured output [%s]", outputStringBuilder);
                        pipeline.getEnvironmentVariables()
                                .merge(run.getCaptureVariable(), outputStringBuilder.toString(), (a, b) -> a + b);
                        pipeline.getEnvironmentVariables()
                                .merge(
                                        "INPUT_" + run.getCaptureVariable(),
                                        outputStringBuilder.toString(),
                                        (a, b) -> a + b);
                        break;
                    }
                    case OVERWRITE: {
                        console.trace("captured output [%s]", outputStringBuilder);
                        pipeline.getEnvironmentVariables()
                                .put(run.getCaptureVariable(), outputStringBuilder.toString());
                        pipeline.getEnvironmentVariables()
                                .put("INPUT_" + run.getCaptureVariable(), outputStringBuilder.toString());
                        break;
                    }
                }

                int exitCode = process.waitFor();
                step.setExitCode(exitCode);
                if (exitCode != 0) {
                    return;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace(System.out);
                step.setExitCode(1);
                break;
            }
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
     * Method to replace variables in a string
     *
     * @param properties properties
     * @param escapeDoubleQuotes escapeDoubleQuotes
     * @param string string
     * @return the string with variables replaced
     */
    private static String replaceVariables(Map<String, String> properties, boolean escapeDoubleQuotes, String string) {
        Pattern pattern = Pattern.compile("(?<!\\\\)\\$\\{\\{\\s*(.*?)\\s*}}");
        String previousResult;

        do {
            previousResult = string;
            Matcher matcher = pattern.matcher(string);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String variableName = matcher.group(1).trim();
                String replacement = properties.get(variableName);

                if (replacement == null) {
                    replacement = matcher.group(0);
                }

                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }

            matcher.appendTail(result);
            string = result.toString();

        } while (!string.equals(previousResult));

        return escapeDoubleQuotes ? escapeDoubleQuotes(string) : string;
    }

    /**
     * Method to replace environment variables
     *
     * @param properties properties
     * @param string string
     * @return the string with environment variables replaced
     */
    private static String replaceEnvironmentVariables(
            Map<String, String> properties, boolean escapeDoubleQuotes, String string) {
        Pattern pattern = Pattern.compile("(?<!\\\\)\\$(\\w+)");
        String previousResult;

        do {
            previousResult = string;
            Matcher matcher = pattern.matcher(string);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String variableName = matcher.group(1).trim();
                String replacement = properties.get(variableName);

                if (replacement == null) {
                    replacement = matcher.group(0);
                }

                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }

            matcher.appendTail(result);
            string = result.toString();

        } while (!string.equals(previousResult));

        return escapeDoubleQuotes ? escapeDoubleQuotes(string) : string;
    }

    /**
     * Method to escape double quotes
     *
     * @param string string
     * @return the string with double quotes escaped
     */
    private static String escapeDoubleQuotes(String string) {
        return string.replace("\"", "\\\"");
    }
}
