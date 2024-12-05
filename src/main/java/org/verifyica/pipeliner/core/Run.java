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

package org.verifyica.pipeliner.core;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.RecursiveReplacer;
import org.verifyica.pipeliner.common.Validator;
import org.verifyica.pipeliner.common.ValidatorException;
import org.verifyica.pipeliner.common.Version;
import org.verifyica.pipeliner.common.io.NoOpPrintStream;
import org.verifyica.pipeliner.common.io.StringPrintStream;
import org.verifyica.pipeliner.core2.execution.Shell;

/** Class to implement Run */
public class Run implements Executable {

    private static final String PROPERTY_MATCHING_REGEX = "(?<!\\\\)\\$\\{\\{\\s*([a-zA-Z0-9_\\-.]+)\\s*\\}\\}";

    private static final String ENVIRONMENT_VARIABLE_MATCHING_REGEX = "(?<!\\\\)\\$(\\w+)";

    private Console console;
    private final Validator validator;

    private final Step step;
    private final String command;
    private CaptureType captureType;
    private String captureVariable;
    private int exitCode;

    /**
     * Constructor
     *
     * @param step step
     * @param command command
     */
    public Run(Step step, String command) {
        this.step = step;
        this.command = command;
        this.captureType = CaptureType.NONE;
        this.captureVariable = null;
        this.validator = new Validator();
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
     * Method to get the command
     *
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Method to set capture information
     *
     * @param captureType captureType
     * @param captureVariable captureVariable
     */
    public void setCapture(CaptureType captureType, String captureVariable) {
        this.captureType = captureType;
        this.captureVariable = captureVariable;
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
    public void execute(Mode mode, Console console) {
        if (mode == Mode.ENABLED) {
            this.console = console;

            Step step = getStep();
            Job job = step.getJob();
            Pipeline pipeline = job.getPipeline();

            Map<String, String> environmentVariables = merge(
                    System.getenv(),
                    pipeline.getEnvironmentVariables(),
                    job.getEnvironmentVariables(),
                    step.getEnvironmentVariables());

            Map<String, String> properties = merge(pipeline.getProperties(), job.getProperties(), step.getProperties());

            Map<String, String> options = merge(pipeline.getOptions(), job.getOptions(), step.getOptions());

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

            if (console.isTraceEnabled()) {
                options.forEach((name, value) -> console.trace("option [%s] = [%s]", name, value));
            }

            Shell shell = step.getShellType();
            String processBuilderCommand = parseProcessBuilderCommand(command, captureType, properties);
            String[] processBuilderCommands = createProcessBuilderCommands(processBuilderCommand, shell);
            File workingDirectory = parseWorkingDirectory(step.getWorkingDirectory(), environmentVariables, properties);

            console.trace("command [%s]", command);
            console.trace("process builder command [%s]", processBuilderCommand);
            console.trace("capture type [%s]", captureType);
            console.trace("capture variable [%s]", captureVariable);
            console.trace("shell type [%s]", shell);
            console.trace("working directory [%s]", workingDirectory.getAbsolutePath());

            Matcher matcher = Pattern.compile(PROPERTY_MATCHING_REGEX).matcher(processBuilderCommand);
            while (matcher.find()) {
                String property = matcher.group();
                String propertyKey =
                        "INPUT_" + property.substring(property.lastIndexOf("{") + 1, property.indexOf("}"));

                console.trace("property [%s] propertyKey [%s]", property, propertyKey);

                if (!properties.containsKey(propertyKey)) {
                    String message = format("unresolved command property [%s]", property);
                    console.error("%s %s", getStep(), message);
                    setExitCode(1);
                    return;
                }
            }

            matcher.reset(workingDirectory.getAbsolutePath());
            while (matcher.find()) {
                String property = matcher.group();
                String propertyKey =
                        "INPUT_" + property.substring(property.lastIndexOf("{") + 1, property.indexOf("}"));

                console.trace("property [%s] propertyKey [%s]", property, propertyKey);

                if (!properties.containsKey(propertyKey)) {
                    String message = format("unresolved working-directory property [%s]", property);
                    console.error("%s %s", getStep(), message);
                    setExitCode(1);
                    return;
                }
            }

            /*
            // Commented out since it doesn't work if the command contains pipes to a command such as awk
            matcher = Pattern.compile(ENVIRONMENT_VARIABLE_MATCHING_REGEX).matcher(processBuilderCommand);
            while (matcher.find()) {
                String environmentVariable = matcher.group();
                String environmentVariableKey = environmentVariable.substring(1);

                console.trace(
                        "environmentVariable [%s] environmentVariableKey [%s]",
                        environmentVariable, environmentVariableKey);

                if (!environmentVariables.containsKey(environmentVariableKey)) {
                    String message = format("unresolved environment variable [%s]", environmentVariable);
                    console.error("%s %s", getStep(), message);
                    setExitCode(1);
                    return;
                }
            }
            */

            try {
                validator.isValidDirectory(
                        workingDirectory, "working directory either doesn't exit, not a directory, or not accessible");
            } catch (ValidatorException e) {
                console.error("%s %s", getStep(), e.getMessage());
                setExitCode(1);
                return;
            }

            if ("mask".equals(options.get("properties"))) {
                console.log("$ %s", command);
            } else {
                console.log("$ %s", processBuilderCommand);
            }

            ProcessBuilder processBuilder = new ProcessBuilder();

            processBuilder.environment().putAll(environmentVariables);
            processBuilder.directory(workingDirectory);
            processBuilder.command(processBuilderCommands);
            processBuilder.redirectErrorStream(true);

            try {
                Process process = processBuilder.start();

                StringBuilder outputStringBuilder = new StringBuilder();
                PrintStream capturingPrintStream;

                switch (captureType) {
                    case APPEND:
                    case OVERWRITE: {
                        capturingPrintStream = new StringPrintStream(outputStringBuilder);
                        break;
                    }
                    case NONE:
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

                switch (captureType) {
                    case APPEND: {
                        String capturedOutput = outputStringBuilder.toString();
                        console.trace("captured output [%s]", capturedOutput);
                        job.getProperties().merge("INPUT_" + captureVariable, capturedOutput, (a, b) -> a + b);
                        break;
                    }
                    case OVERWRITE: {
                        String capturedOutput = outputStringBuilder.toString();
                        console.trace("captured output [%s]", capturedOutput);
                        job.getProperties().put("INPUT_" + captureVariable, capturedOutput);
                        break;
                    }
                    case NONE:
                    default: {
                        // INTENTIONALLY BLANK
                        break;
                    }
                }

                setExitCode(process.waitFor());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace(System.out);
                setExitCode(1);
            }
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return "@run command [" + command + "]";
    }

    private String parseProcessBuilderCommand(String command, CaptureType captureType, Map<String, String> properties) {
        console.trace("parseProcessBuilderCommand command [%s] captureType [%s]", command, captureType);

        String processBuilderCommand;

        switch (captureType) {
            case APPEND:
                processBuilderCommand =
                        command.substring(0, command.lastIndexOf(">>")).trim();
                break;
            case OVERWRITE: {
                processBuilderCommand =
                        command.substring(0, command.lastIndexOf(">")).trim();
                break;
            }
            case NONE:
            default: {
                processBuilderCommand = command;
            }
        }

        processBuilderCommand = RecursiveReplacer.replace(properties, PROPERTY_MATCHING_REGEX, processBuilderCommand);

        console.trace("parseProcessBuilderCommand [%s]", processBuilderCommand);

        return processBuilderCommand;
    }

    private static File parseWorkingDirectory(
            String workingDirectory, Map<String, String> environmentVariables, Map<String, String> properties) {
        return new File(RecursiveReplacer.replace(
                properties,
                PROPERTY_MATCHING_REGEX,
                RecursiveReplacer.replace(
                        environmentVariables, ENVIRONMENT_VARIABLE_MATCHING_REGEX, workingDirectory)));
    }

    private static String[] createProcessBuilderCommands(String command, Shell shell) {
        String[] processBuilderCommands;

        switch (shell) {
            case BASH: {
                processBuilderCommands =
                        new String[] {"bash", "--noprofile", "--norc", "-eo", "pipefail", "-c", command};
                break;
            }
            case SH: {
                processBuilderCommands = new String[] {"sh", "-e", "-c", command};
                break;
            }
            default: {
                processBuilderCommands = new String[] {"bash", "-e", "-c", command};
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
