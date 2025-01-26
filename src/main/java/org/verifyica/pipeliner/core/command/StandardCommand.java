/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.core.command;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.Environment;
import org.verifyica.pipeliner.core.CaptureType;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.Job;
import org.verifyica.pipeliner.core.Pipeline;
import org.verifyica.pipeliner.core.Shell;
import org.verifyica.pipeliner.core.Step;
import org.verifyica.pipeliner.core.Variable;
import org.verifyica.pipeliner.core.support.Ipc;
import org.verifyica.pipeliner.core.support.Resolver;
import org.verifyica.pipeliner.core.support.UnresolvedException;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.parser.SyntaxException;

/** Class to implement StandardCommand */
public class StandardCommand implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardCommand.class);

    private static final String SCOPE_SEPARATOR = Constants.SCOPE_SEPARATOR;

    private static final String CAPTURE_APPEND_MATCHING_REGEX = ".*>>\\s*\\$[a-zA-Z0-9][a-zA-Z0-9\\-._]*$";

    private static final String CAPTURE_OVERWRITE_MATCHING_REGEX = ".*>\\s*\\$[a-zA-Z0-9][a-zA-Z0-9\\-._]*$";

    private final Pipeline pipeline;
    private final String pipelineId;
    private final Job job;
    private final String jobId;
    private final Step step;
    private final String stepId;
    private final String command;

    /**
     * Constructor
     *
     * @param step the step
     * @param command the command
     */
    public StandardCommand(Step step, String command) {
        this.pipeline = step.getParent(Job.class).getParent(Pipeline.class);
        this.pipelineId = pipeline.getId();
        this.job = step.getParent(Job.class);
        this.jobId = job.getId();
        this.step = step;
        this.stepId = step.getId();
        this.command = command;
    }

    /**
     * Method to execute the command
     *
     * @param context the context
     * @return the exit code
     */
    public int execute(Context context) {
        Console console = context.getConsole();
        int exitCode;
        File ipcOutFile = null;
        File ipcInFile = null;

        try {
            console.emit("$ %s", command);

            // Resolve variables
            Map<String, String> variables = Resolver.resolveVariables(context.getWith());

            // Create the environment variables
            Map<String, String> environmentVariables = new TreeMap<>(Environment.getenv());

            // Resolve environment variables
            environmentVariables.putAll(Resolver.resolveEnvironmentVariables(context.getEnv(), variables));

            String workingCommand = command;

            // If the command starts with PIPELINE_DIRECTIVE_COMMAND_PREFIX, replace it with $PIPELINER
            if (workingCommand.startsWith(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX)) {
                workingCommand = "$" + Constants.PIPELINER + " "
                        + workingCommand.substring(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX.length());
            }

            // Resolve the working directory
            File workingDirectory = resolveWorkingDirectory(environmentVariables, variables);

            // Validate the working directory file exists
            if (!workingDirectory.exists()) {
                throw new IllegalStateException(format("working-directory=[%s] doesn't exit", workingDirectory));
            }

            // Validate the working directory file is accessible
            if (!workingDirectory.canRead()) {
                throw new IllegalStateException(format("working-directory=[%s] can't be read", workingDirectory));
            }

            // Validate the working directory file is a directory
            if (!workingDirectory.isDirectory()) {
                throw new IllegalStateException(format("working-directory=[%s] isn't a directory", workingDirectory));
            }

            // Get the capture type
            CaptureType captureType = getCaptureType(workingCommand);

            // Get the capture variable, which may be null depending on the capture type
            String captureVariable = getCaptureVariable(captureType, workingCommand);

            // Based on the capture type, validate the capture variable
            switch (captureType) {
                case APPEND: {
                    if (Variable.isInvalid(captureVariable)) {
                        throw new IllegalStateException(format("invalid capture variable [%s]", captureVariable));
                    }

                    // Remove the capture variable from the command
                    workingCommand = workingCommand
                            .substring(0, workingCommand.lastIndexOf(">>"))
                            .trim();

                    break;
                }
                case OVERWRITE: {
                    if (Variable.isInvalid(captureVariable)) {
                        throw new IllegalStateException(format("invalid capture variable [%s]", captureVariable));
                    }

                    // Remove the capture variable from the command
                    workingCommand = workingCommand
                            .substring(0, workingCommand.lastIndexOf(">"))
                            .trim();

                    break;
                }
                default: {
                    break;
                }
            }

            // Resolve variables in the command
            String resolvedCommand = Resolver.resolveVariables(variables, workingCommand);

            // Decode the shell
            Shell shell = Shell.decode(step.getShell());

            // Get the list of process builder command arguments
            String[] processCommands = Shell.getProcessBuilderCommandArguments(shell, resolvedCommand);

            // Create the IPC in file
            ipcInFile = Ipc.createFile();

            // Create the IPC out file
            ipcOutFile = Ipc.createFile();

            // Set the IPC environment variable
            environmentVariables.put(Constants.PIPELINER_IPC_IN, ipcInFile.getAbsolutePath());

            // Set the IPC environment variable
            environmentVariables.put(Constants.PIPELINER_IPC_OUT, ipcOutFile.getAbsolutePath());

            // Write the variables to the IPC in file
            Ipc.write(ipcInFile, variables);

            if (LOGGER.isTraceEnabled()) {
                environmentVariables.forEach(
                        (name, value) -> LOGGER.trace("environment variable [%s] = [%s]", name, value));

                variables.forEach((name, value) -> LOGGER.trace("variable [%s] = [%s]", name, value));

                LOGGER.trace("working directory [%s]", workingDirectory);
                LOGGER.trace("working command [%s]", workingCommand);
                LOGGER.trace("working command (without capture) [%s]", workingCommand);
                LOGGER.trace("resolved command [%s]", resolvedCommand);
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
                        // Get the existing value of the capture variable
                        String existingValue = context.getWith().getOrDefault(captureVariable, "");

                        // Append the captured output to the existing value
                        String newValue = existingValue + stringBuilder;

                        // Add the unscoped variable
                        context.getWith().put(captureVariable, newValue);

                        if (stepId != null) {
                            // Add the step scoped variable
                            context.getWith().put(stepId + SCOPE_SEPARATOR + captureVariable, newValue);

                            if (jobId != null) {
                                // Add the job + step scoped variable
                                context.getWith()
                                        .put(
                                                jobId + SCOPE_SEPARATOR + stepId + SCOPE_SEPARATOR + captureVariable,
                                                newValue);

                                if (pipelineId != null) {
                                    // Add the pipeline + job + step scoped variable
                                    context.getWith()
                                            .put(
                                                    pipelineId
                                                            + SCOPE_SEPARATOR
                                                            + jobId
                                                            + SCOPE_SEPARATOR
                                                            + stepId
                                                            + SCOPE_SEPARATOR
                                                            + captureVariable,
                                                    newValue);
                                }
                            }
                        }

                        break;
                    }
                    case OVERWRITE: {
                        // Get the captured output
                        String value = stringBuilder.toString();

                        // Add the unscoped variable
                        context.getWith().put(captureVariable, value);

                        if (stepId != null) {
                            // Add the step scoped variable
                            context.getWith().put(stepId + SCOPE_SEPARATOR + captureVariable, value);

                            if (jobId != null) {
                                // Add the job + step scoped variable
                                context.getWith()
                                        .put(
                                                jobId + SCOPE_SEPARATOR + stepId + SCOPE_SEPARATOR + captureVariable,
                                                value);

                                if (pipelineId != null) {
                                    // Add the pipeline + job + step scoped variable
                                    context.getWith()
                                            .put(
                                                    pipelineId
                                                            + SCOPE_SEPARATOR
                                                            + jobId
                                                            + SCOPE_SEPARATOR
                                                            + stepId
                                                            + SCOPE_SEPARATOR
                                                            + captureVariable,
                                                    value);
                                }
                            }
                        }

                        break;
                    }
                    default: {
                        break;
                    }
                }

                // Read the variables from the IPC out file
                Ipc.read(ipcOutFile).forEach((name, value) -> {
                    LOGGER.trace("ipc in variable [%s] -> [%s]", name, value);

                    // Add the unscoped variable
                    context.getWith().put(name, value);

                    if (stepId != null) {
                        // Add the scoped variable
                        context.getWith().put(stepId + Constants.SCOPE_SEPARATOR + name, value);

                        if (jobId != null) {
                            // Add the scoped variable
                            context.getWith()
                                    .put(
                                            jobId
                                                    + Constants.SCOPE_SEPARATOR
                                                    + stepId
                                                    + Constants.SCOPE_SEPARATOR
                                                    + name,
                                            value);

                            if (pipelineId != null) {
                                // Add the scoped variable
                                context.getWith()
                                        .put(
                                                pipelineId
                                                        + Constants.SCOPE_SEPARATOR
                                                        + jobId
                                                        + Constants.SCOPE_SEPARATOR
                                                        + stepId
                                                        + Constants.SCOPE_SEPARATOR
                                                        + name,
                                                value);
                            }
                        }
                    }
                });
            }
        } catch (Throwable t) {
            // Emit the error
            console.emit("@error %s -> %s", step, t.getMessage());

            if (LOGGER.isTraceEnabled()) {
                t.printStackTrace(System.err);
            }

            exitCode = 1;
        } finally {
            // Proactively cleanup the IPC in file
            Ipc.cleanup(ipcInFile);

            // Proactively cleanup the IPC out file
            Ipc.cleanup(ipcOutFile);
        }

        return exitCode;
    }

    /**
     * Method to resolve the working directory
     *
     * @param environmentVariables the environment variables
     * @param variables the variables
     * @return the working directory
     * @throws SyntaxException If a syntax error occurs
     */
    private File resolveWorkingDirectory(Map<String, String> environmentVariables, Map<String, String> variables)
            throws SyntaxException, UnresolvedException {
        String workingDirectory = step.getWorkingDirectory();
        if (workingDirectory == null) {
            workingDirectory = job.getWorkingDirectory();
            if (workingDirectory == null) {
                workingDirectory = pipeline.getWorkingDirectory();
            }
        }

        if (workingDirectory == null) {
            workingDirectory = ".";

            return new File(workingDirectory).getAbsoluteFile();
        }

        String resolvedWorkingDirectory =
                Resolver.resolveAllVariables(environmentVariables, variables, workingDirectory);

        return new File(resolvedWorkingDirectory).getAbsoluteFile();
    }

    /**
     * Method to get the CaptureType
     *
     * @param command the command
     * @return the CaptureType
     */
    private static CaptureType getCaptureType(String command) {
        CaptureType captureType;

        if (command.matches(CAPTURE_APPEND_MATCHING_REGEX)) {
            captureType = CaptureType.APPEND;
        } else if (command.matches(CAPTURE_OVERWRITE_MATCHING_REGEX)) {
            captureType = CaptureType.OVERWRITE;
        } else {
            captureType = CaptureType.NONE;
        }

        return captureType;
    }

    /**
     * Method to get the capture property
     *
     * @param captureType the capture type
     * @param command the command
     * @return the capture property
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
