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

package org.verifyica.pipeliner.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.Version;
import org.verifyica.pipeliner.common.RecursiveReplacer;
import org.verifyica.pipeliner.io.NoOpPrintStream;
import org.verifyica.pipeliner.io.StringPrintStream;

/** Class to implement Run */
public class Run implements Action {

    public static final String PROPERTY_MATCHING_REGEX = "(?<!\\\\)\\$\\{\\{\\s*([a-zA-Z0-9_\\-.]+)\\s*\\}\\}";
    public static final String ENVIRONMENT_VARIABLE_MATCHING_REGEX = "(?<!\\\\)\\$(\\w+)";

    /** Capture type */
    private enum CaptureType {
        /** None */
        NONE,
        /** Overwrite */
        OVERWRITE,
        /** Append */
        APPEND
    }

    private final Step step;
    private final String command;
    private int exitCode;

    /**
     * Constructor
     *
     * @param command command
     */
    public Run(Step step, String command) {
        this.step = step;
        this.command = command.trim();
    }

    /**
     * Method to get the step
     *
     * @return the step
     */
    public Step getStep() {
        return step;
    }

    /**
     * Method to get the raw command
     *
     * @return the raw command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Method to set the exit code
     *
     * @param exitCode exitCode
     */
    private void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public void execute(Console console) {
        console.trace("------------------------------------------------------------");
        console.trace("execute %s", this);
        console.trace("------------------------------------------------------------");

        Step step = getStep();
        Job job = step.getJob();
        Pipeline pipeline = job.getPipeline();

        Map<String, String> environmentVariables = merge(
                System.getenv(),
                pipeline.getEnvironmentVariables(),
                job.getEnvironmentVariables(),
                step.getEnvironmentVariables());

        Map<String, String> properties = merge(pipeline.getProperties(), job.getProperties(), step.getProperties());

        String version = Version.getVersion();

        environmentVariables.put("PIPELINER_VERSION", version);
        properties.put("INPUT_PIPELINER_VERSION", version);

        if (console.isTraceEnabled()) {
            environmentVariables.forEach(
                    (name, value) -> console.trace("environment variable [%s] = [%s]", name, value));
        }

        if (console.isTraceEnabled()) {
            properties.forEach((name, value) -> console.trace("property [%s] = [%s]", name, value));
        }

        console.trace("raw command [%s]", command);

        String executableCommand = parseExecutableCommand(command);

        console.trace("executable command [%s]", executableCommand);

        executableCommand = RecursiveReplacer.replace(properties, PROPERTY_MATCHING_REGEX, executableCommand);

        console.trace("executable command [%s] (phase 1)", executableCommand);

        CaptureType captureType = parseCaptureType(command);
        String captureVariable = null;
        if (captureType != null) {
            captureVariable = parseCaptureVariable(command);
        }

        ShellType shellType = step.getShellType();
        String workingDirectory = step.getWorkingDirectory();

        console.trace("working directory [%s]", workingDirectory);

        workingDirectory =
                RecursiveReplacer.replace(environmentVariables, ENVIRONMENT_VARIABLE_MATCHING_REGEX, workingDirectory);

        console.trace("working directory [%s] (phase 1)", workingDirectory);

        workingDirectory = RecursiveReplacer.replace(properties, PROPERTY_MATCHING_REGEX, workingDirectory);

        console.trace("working directory [%s] (phase 2)", workingDirectory);

        // TODO validate working directory exists

        String[] processBuilderCommands = buildProcessBuilderCommands(shellType, executableCommand);

        console.trace("capture type [%s]", captureType);
        console.trace("capture variable [%s]", captureVariable);
        console.trace("shell type [%s]", shellType);

        String traceProcessBuilderCommand = Arrays.stream(processBuilderCommands)
                .map(s -> "\"" + s + "\"") // Add double quotes
                .reduce((s1, s2) -> s1 + " " + s2)
                .orElse("");

        console.trace("process build tokens [%s]", traceProcessBuilderCommand);

        console.log("$ %s", command);

        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.environment().putAll(environmentVariables);
        processBuilder.directory(new File(workingDirectory));
        processBuilder.command(processBuilderCommands);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            StringBuilder outputStringBuilder = new StringBuilder();
            PrintStream capturingPrintStream;

            switch (captureType != null ? captureType : CaptureType.NONE) {
                case APPEND:
                case OVERWRITE: {
                    console.trace("capture variable [%s]", captureVariable);
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

            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                boolean appendCRLF = false;
                while ((line = bufferedReader.readLine()) != null) {
                    tokens = line.split("\\R");
                    for (String token : tokens) {
                        // TODO make configurable?
                        // console.trace("output [%s]", token);

                        if (appendCRLF) {
                            capturingPrintStream.println();
                        }
                        capturingPrintStream.print(token);

                        if (captureType == CaptureType.NONE) {
                            console.log("> %s", token);
                        }

                        appendCRLF = true;
                    }
                }
            }

            capturingPrintStream.close();

            switch (captureType != null ? captureType : CaptureType.NONE) {
                case APPEND: {
                    String capturedOutput = outputStringBuilder.toString();
                    console.trace("captured output [%s]", capturedOutput);
                    pipeline.getProperties().merge("INPUT_" + captureVariable, capturedOutput, (a, b) -> a + b);
                    pipeline.getEnvironmentVariables().merge(captureVariable, capturedOutput, (a, b) -> a + b);
                    break;
                }
                case OVERWRITE: {
                    String capturedOutput = outputStringBuilder.toString();
                    console.trace("captured output [%s]", capturedOutput);
                    pipeline.getProperties().put("INPUT_" + captureVariable, capturedOutput);
                    pipeline.getEnvironmentVariables().put(captureVariable, capturedOutput);
                    break;
                }
            }

            setExitCode(process.waitFor());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.out);
            setExitCode(1);
        }
    }

    @Override
    public void skip(Console console) {
        // INTENTIONALLY BLANK
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return "@run command [" + command + "]";
    }

    private static String parseExecutableCommand(String command) {
        String pattern = ".*>>\\s*\\$\\w+$";
        if (command.matches(pattern)) {
            return command.substring(0, command.lastIndexOf(">>")).trim();
        }

        pattern = ".*>\\s*\\$\\w+$";
        if (command.matches(pattern)) {
            return command.substring(0, command.lastIndexOf(">")).trim();
        }

        return command.trim();
    }

    private static CaptureType parseCaptureType(String command) {
        String pattern = ".*>>\\s*\\$\\w+$";
        if (command.matches(pattern)) {
            return CaptureType.APPEND;
        }

        pattern = ".*>\\s+\\$\\w+$";
        if (command.matches(pattern)) {
            return CaptureType.OVERWRITE;
        }

        return CaptureType.NONE;
    }

    private static String parseCaptureVariable(String command) {
        String pattern = ".*>>\\s*\\$\\w+$";
        if (command.matches(pattern)) {
            return command.substring(command.lastIndexOf("$") + 1);
        }

        pattern = ".*>\\s*\\$\\w+$";
        if (command.matches(pattern)) {
            return command.substring(command.lastIndexOf("$") + 1);
        }

        return null;
    }

    private static String[] buildProcessBuilderCommands(ShellType shellType, String executableCommand) {
        String[] processBuilderCommands;

        switch (shellType) {
            case BASH: {
                processBuilderCommands =
                        new String[] {"bash", "--noprofile", "--norc", "-eo", "pipefail", "-c", executableCommand};
                break;
            }
            case SH: {
                processBuilderCommands = new String[] {"sh", "-e", "-c", executableCommand};
                break;
            }
            default: {
                processBuilderCommands = new String[] {"bash", "-e", "-c", executableCommand};
                break;
            }
        }

        return processBuilderCommands;
    }

    /**
     * Method to merge an array of Maps into a single Map
     *
     * @param maps maps
     * @return a merged Map
     */
    @SafeVarargs
    private static Map<String, String> merge(Map<String, String>... maps) {
        Map<String, String> mergedMap = new TreeMap<>();

        for (Map<String, String> map : maps) {
            mergedMap.putAll(map);
        }

        return mergedMap;
    }
}
