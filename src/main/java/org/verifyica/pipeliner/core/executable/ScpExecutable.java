/*
 * Copyright (C) Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.core.executable;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.Environment;
import org.verifyica.pipeliner.common.QuotedStringTokenizer;
import org.verifyica.pipeliner.core.CaptureType;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.Step;
import org.verifyica.pipeliner.core.support.Resolver;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement ScpExecutable */
public class ScpExecutable extends AbstractExecutable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScpExecutable.class);

    private static final String CAPTURE_APPEND_REGEX = ".*>>\\s*\\$[a-zA-Z0-9][a-zA-Z0-9\\-._]*$";

    private static final Pattern CAPTURE_APPEND_PATTERN = Pattern.compile(CAPTURE_APPEND_REGEX);

    private static final Matcher CAPTURE_APPEND_MATCHER = CAPTURE_APPEND_PATTERN.matcher("");

    private static final String CAPTURE_OVERWRITE_REGEX = ".*>\\s*\\$[a-zA-Z0-9][a-zA-Z0-9\\-._]*$";

    private static final Pattern CAPTURE_OVERWRITE_PATTERN = Pattern.compile(CAPTURE_OVERWRITE_REGEX);

    private static final Matcher CAPTURE_OVERWRITE_MATCHER = CAPTURE_OVERWRITE_PATTERN.matcher("");

    /**
     * Constructor
     *
     * @param step        the step
     * @param commandLine the command line
     */
    public ScpExecutable(Step step, String commandLine) {
        super(step, commandLine);
    }

    @Override
    public int execute(Context context) throws Throwable {
        LOGGER.trace("executing %s %s %s command line [%s]", getPipeline(), getJob(), getStep(), getCommandLine());

        int exitCode;

        // Get the console
        Console console = context.getConsole();

        try {
            console.emit("$ %s", getCommandLine());

            // Resolve variables
            Map<String, String> variables = Resolver.resolveVariables(context.getVariables());

            // Create the environment variables
            Map<String, String> environmentVariables = new TreeMap<>(Environment.getenv());

            // Resolve environment variables
            environmentVariables.putAll(
                    Resolver.resolveEnvironmentVariables(context.getEnvironmentVariables(), variables));

            String workingCommandLine = getCommandLine();

            // If the command line starts with PIPELINE_DIRECTIVE_COMMAND_PREFIX, replace it with $PIPELINER
            if (workingCommandLine.startsWith(ExecutableFactory.PIPELINE_DIRECTIVE)) {
                workingCommandLine = "$" + Constants.PIPELINER + " "
                        + workingCommandLine.substring(ExecutableFactory.PIPELINE_DIRECTIVE.length());
            }

            // Resolve the working directory
            File workingDirectory = resolveWorkingDirectory(environmentVariables, variables);

            // Validate the working directory exists
            if (!workingDirectory.exists()) {
                throw new IllegalStateException(format("working-directory=[%s] doesn't exit", workingDirectory));
            }

            // Validate the working directory is accessible
            if (!workingDirectory.canRead()) {
                throw new IllegalStateException(format("working-directory=[%s] can't be read", workingDirectory));
            }

            // Validate the working directory is a directory
            if (!workingDirectory.isDirectory()) {
                throw new IllegalStateException(format("working-directory=[%s] isn't a directory", workingDirectory));
            }

            // Get the capture type
            CaptureType captureType = getCaptureType(workingCommandLine);

            // Get the capture variable, which may be null depending on the capture type
            String captureVariable = getCaptureVariable(captureType, workingCommandLine);

            // Based on the capture type, validate the capture variable
            switch (captureType) {
                case APPEND: {
                    if (org.verifyica.pipeliner.core.Variable.isInvalid(captureVariable)) {
                        throw new IllegalStateException(format("invalid capture variable [%s]", captureVariable));
                    }

                    // Remove the capture variable from the command
                    workingCommandLine = workingCommandLine
                            .substring(0, workingCommandLine.lastIndexOf(">>"))
                            .trim();

                    break;
                }
                case OVERWRITE: {
                    if (org.verifyica.pipeliner.core.Variable.isInvalid(captureVariable)) {
                        throw new IllegalStateException(format("invalid capture variable [%s]", captureVariable));
                    }

                    // Remove the capture variable from the command
                    workingCommandLine = workingCommandLine
                            .substring(0, workingCommandLine.lastIndexOf(">"))
                            .trim();

                    break;
                }
                default: {
                    break;
                }
            }

            // Resolve variables in the command line
            String resolvedCommandLine = Resolver.resolveVariables(variables, workingCommandLine);

            // Split the command line into tokens
            String[] tokens =
                    QuotedStringTokenizer.tokenize(resolvedCommandLine).toArray(new String[0]);

            // Get the user/host
            String userHost = tokens[1];

            // Get the local file
            String localFile = tokens[2];

            // Get the remote file
            String remoteFile = tokens[3];

            // Build the process command tokens
            String[] processCommands = new String[] {"scp", localFile, userHost + ":" + remoteFile};

            if (LOGGER.isTraceEnabled()) {
                environmentVariables.forEach(
                        (name, value) -> LOGGER.trace("environment variable [%s] = [%s]", name, value));

                variables.forEach((name, value) -> LOGGER.trace("variable [%s] = [%s]", name, value));

                LOGGER.trace("working directory [%s]", workingDirectory);
                LOGGER.trace("working command line [%s]", workingCommandLine);
                LOGGER.trace("working command line (without capture variable) [%s]", workingCommandLine);
                LOGGER.trace("resolved command line [%s]", resolvedCommandLine);
                LOGGER.trace("capture type [%s]", captureType);
                LOGGER.trace("capture variable [%s]", captureVariable);
                LOGGER.trace(
                        "process command arguments %s",
                        String.join(
                                        " ",
                                        Arrays.stream(processCommands)
                                                .map(part -> "[" + part + "]")
                                                .toArray(String[]::new))
                                .trim());
            }

            // Create a process builder
            ProcessBuilder processBuilder = new ProcessBuilder(processCommands);

            // Set working directory
            processBuilder.directory(workingDirectory);

            // Set environment variables
            processBuilder.environment().putAll(environmentVariables);

            // Redirect stderr to stdout
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            StringBuilder stringBuilder = new StringBuilder();
            boolean addNewLine = false;

            // Read and print each line of combined output
            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    switch (captureType) {
                        case APPEND:
                        case OVERWRITE: {
                            if (addNewLine) {
                                stringBuilder.append(System.lineSeparator());
                            }

                            stringBuilder.append(line);
                            addNewLine = true;

                            console.emit("> " + line);

                            break;
                        }
                        default:
                            // Emit the output
                            console.emit("> " + line);
                            break;
                    }
                }
            }

            // Get the exit code
            exitCode = process.waitFor();

            LOGGER.trace("exit code [%d]", exitCode);

            if (exitCode == 0) {
                switch (captureType) {
                    case APPEND: {
                        // Get the existing value of the capture variable and append the captured output
                        String value = context.getVariables().getOrDefault(captureVariable, "") + stringBuilder;

                        // Add the capture variable with optional scopes
                        context.setVariable(captureVariable, value, getPipelineId(), getJobId(), getStepId());

                        break;
                    }
                    case OVERWRITE: {
                        // Get the captured output
                        String value = stringBuilder.toString();

                        // Add the capture variable with optional scopes
                        context.setVariable(captureVariable, value, getPipelineId(), getJobId(), getStepId());

                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            // Emit the error
            console.emit("@error %s -> %s", getStep(), t.getMessage());

            if (LOGGER.isTraceEnabled()) {
                t.printStackTrace(System.err);
            }

            // Set the exit code to 1 to indicate an error
            exitCode = 1;
        }

        return exitCode;
    }

    /**
     * Method to get the CaptureType
     *
     * @param command the command
     * @return the CaptureType
     */
    private static CaptureType getCaptureType(String command) {
        CaptureType captureType;

        if (CAPTURE_APPEND_MATCHER.reset(command).matches()) {
            captureType = CaptureType.APPEND;
        } else if (CAPTURE_OVERWRITE_MATCHER.reset(command).matches()) {
            captureType = CaptureType.OVERWRITE;
        } else {
            captureType = CaptureType.NONE;
        }

        return captureType;
    }

    /**
     * Method to get the capture variable
     *
     * @param captureType the capture type
     * @param command the command
     * @return the capture variable
     */
    private static String getCaptureVariable(CaptureType captureType, String command) {
        String captureVariable = null;

        switch (captureType) {
            case APPEND:
            case OVERWRITE: {
                captureVariable = command.substring(command.lastIndexOf("$") + 1);
            }
            case NONE:
            default: {
                break;
            }
        }

        return captureVariable;
    }
}
