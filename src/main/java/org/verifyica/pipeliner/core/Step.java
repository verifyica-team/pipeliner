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
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.common.LineParser;
import org.verifyica.pipeliner.common.io.StringPrintStream;
import org.verifyica.pipeliner.core.support.Resolver;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement Step */
public class Step extends Node {

    private static final Logger LOGGER = LoggerFactory.getLogger(Step.class);

    private static final String CAPTURE_APPEND_MATCHING_REGEX = ".*>>\\s*\\$[a-zA-Z0-9][a-zA-Z0-9\\-._]*$";

    private static final String CAPTURE_OVERWRITE_MATCHING_REGEX = ".*>\\s*\\$[a-zA-Z0-9][a-zA-Z0-9\\-._]*$";

    private String shell;
    private String run;

    /** Constructor */
    public Step() {
        super();

        this.shell = "default";
    }

    /**
     * Method to set the shell
     *
     * @param shell the shell
     */
    public void setShell(String shell) {
        if (shell != null) {
            this.shell = shell;
        }
    }

    /**
     * Method to get the shell
     *
     * @return the shell
     */
    public String getShell() {
        return shell;
    }

    /**
     * Method to set the run
     *
     * @param run the run
     */
    public void setRun(String run) {
        if (run != null) {
            this.run = run.trim();
        }
    }

    /**
     * Method to get the run
     *
     * @return the run
     */
    public String getRun() {
        return run;
    }

    @Override
    public int execute(Context context) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("executing step [%s] ...", this);
        }

        validate();

        Console console = context.getConsole();
        int exitCode = 0;

        if (Boolean.TRUE.equals(Enabled.decode(getEnabled()))) {
            getStopwatch().reset();

            console.info("%s status=[%s]", this, Status.RUNNING);

            try {
                exitCode = run(context);
            } catch (Throwable t) {
                console.error("%s message=[%s]", t.getMessage());
                exitCode = 1;
            }

            Status status = exitCode == 0 ? Status.SUCCESS : Status.FAILURE;

            console.info(
                    "%s status=[%s] exit-code=[%d] ms=[%d]",
                    this, status, exitCode, getStopwatch().elapsedTime().toMillis());

            return exitCode;
        } else {
            skip(context, Status.DISABLED);

            return 1;
        }
    }

    @Override
    public void skip(Context context, Status status) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("skipping step [%s] ...", this);
        }

        Console console = context.getConsole();

        Status effectiveStatus = Boolean.TRUE.equals(Enabled.decode(getEnabled())) ? status : Status.DISABLED;

        console.info("%s status=[%s]", this, effectiveStatus);
    }

    @Override
    public String toString() {
        return "@step " + super.toString();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    /**
     * Method to validate the step
     */
    private void validate() {
        validateId();
        validateEnabled();
        validateEnv();
        validateWith();
        validateWorkingDirectory();
        validateTimeoutMinutes();
        validateShell();
        validateRun();
    }

    /**
     * Method to validate the shell value
     */
    private void validateShell() {
        if (shell == null) {
            throw new PipelineDefinitionException(format("%s -> shell is null", this));
        }

        if (shell.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> shell is blank", this));
        }

        if (Shell.decode(shell) == Shell.INVALID) {
            throw new PipelineDefinitionException(format(
                    "%s -> shell=[%s] is invalid. If defined, must be \"bash\", \"sh\", \"zsh\", or \"none\"",
                    this, shell));
        }
    }

    /**
     * Method to validate the run value
     */
    private void validateRun() {
        if (run == null) {
            throw new PipelineDefinitionException(format("%s -> run is null", this));
        }

        if (run.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> run is blank", this));
        }

        List<String> lines = LineParser.parse(run);
        if (lines.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> run is blank", this));
        }

        for (String line : lines) {
            // If the line is a directive, validate it is a known directive
            if (line.startsWith(Constants.DIRECTIVE_COMMAND_PREFIX)
                    && (!(line.startsWith(Constants.EXTENSION_DIRECTIVE_COMMAND_PREFIX + " ")
                            || line.startsWith(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX + " ")))) {
                throw new PipelineDefinitionException(format("%s -> unknown directive [%s]", this, line));
            }
        }
    }

    /**
     * Method to run the step
     *
     * @param context the context
     * @return the exit code
     * @throws Throwable if an exception occurs
     */
    private int run(Context context) throws Throwable {
        int exitCode = 0;

        // Pare the run content into commands
        List<String> commands = LineParser.parse(run);

        // Process each command
        for (String command : commands) {
            if (command.startsWith(Constants.EXTENSION_DIRECTIVE_COMMAND_PREFIX)) {
                exitCode = executeExtension(context, command);
            } else {
                // Execute the command
                exitCode = executeCommand(context, command);
            }

            // Validate the exit code
            if (exitCode != 0) {
                break;
            }
        }

        return exitCode;
    }

    /**
     * Method to execute a command
     *
     * @param context the context
     * @param command the command
     * @return the exit code
     * @throws Throwable is an exception occurs
     */
    private int executeCommand(Context context, String command) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("execute command [%s]", command);
        }

        Console console = context.getConsole();

        // Emit the command
        console.info("$ %s", command);

        // If the command starts with PIPELINE_DIRECTIVE_COMMAND_PREFIX, replace it with $PIPELINER
        if (command.startsWith(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX)) {
            command = "$" + Constants.PIPELINER + " "
                    + command.substring(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX.length());
        }

        // Get the Job
        Job job = getParent(Job.class);

        // Get the pipeline
        Pipeline pipeline = job.getParent(Pipeline.class);

        // Get the ids
        String pipelineId = pipeline.getId();
        String jobId = job.getId();
        String stepId = getId();

        // Get the working directory, less specific to more specific
        String workingDirectory = pipeline.getWorkingDirectory();
        if (workingDirectory == null) {
            workingDirectory = job.getWorkingDirectory();
            if (workingDirectory == null) {
                workingDirectory = getWorkingDirectory();
                if (workingDirectory == null) {
                    workingDirectory = ".";
                }
            }
        }

        // Build the map of environment variables
        Map<String, String> environmentVariables = new TreeMap<>(pipeline.getEnv());
        environmentVariables.putAll(job.getEnv());
        environmentVariables.putAll(getEnv());

        // Build the map of variables
        Map<String, String> variables = new TreeMap<>(pipeline.getWith());
        variables.putAll(job.getWith());
        variables.putAll(getWith());
        variables.putAll(context.getWith());

        // Resolve variables
        variables = Resolver.resolveVariables(variables);

        // Resolve environment variables
        environmentVariables = Resolver.resolveEnvironmentVariables(environmentVariables, variables);

        // Get the capture type
        CaptureType captureType = getCaptureType(command);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("capture type [%s]", captureType);
        }

        // Get the capture variable, which may be null depending on the capture type
        String captureVariable = getCaptureVariable(captureType, command);

        // Based on the capture type, validate the capture variable
        switch (captureType) {
            case APPEND: {
                if (Variable.isInvalid(captureVariable)) {
                    throw new IllegalStateException(format("invalid capture variable [%s]", captureVariable));
                }

                LOGGER.trace("capture variable [%s]", captureVariable);
                command = command.substring(0, command.lastIndexOf(">>")).trim();
                break;
            }
            case OVERWRITE: {
                if (Variable.isInvalid(captureVariable)) {
                    throw new IllegalStateException(format("invalid capture variable [%s]", captureVariable));
                }

                LOGGER.trace("capture variable [%s]", captureVariable);
                command = command.substring(0, command.lastIndexOf(">")).trim();
                break;
            }
            default: {
                break;
            }
        }

        // Resolve variables in the working directory
        workingDirectory = Resolver.resolveVariables(variables, workingDirectory);

        // Resolve environment variables in the working directory
        workingDirectory = Resolver.resolvedEnvironmentVariables(environmentVariables, workingDirectory);

        // Create a working directory file
        File workingDirectoryFile = new File(workingDirectory).getAbsoluteFile().getCanonicalFile();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("process working directory [%s]", workingDirectoryFile);
        }

        // Validate the working directory file exists
        if (!workingDirectoryFile.exists()) {
            throw new IllegalStateException(format("working-directory=[%s] doesn't exit", workingDirectory));
        }

        // Validate the working directory file is accessible
        if (!workingDirectoryFile.canRead()) {
            throw new IllegalStateException(format("working-directory=[%s] can't be read", workingDirectory));
        }

        // Validate the working directory file is a directory
        if (!workingDirectoryFile.isDirectory()) {
            throw new IllegalStateException(format("working-directory=[%s] isn't a directory", workingDirectory));
        }

        // Resolve variables in the command
        String resolvedCommand = Resolver.resolveVariables(variables, command);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("resolved command [%s]", resolvedCommand);
        }

        // Get the list of process builder command arguments
        String[] commandArguments = Shell.getProcessBuilderCommandArguments(Shell.decode(shell), resolvedCommand);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "process command arguments %s",
                    String.join(
                                    " ",
                                    Arrays.stream(commandArguments)
                                            .map(part -> "[" + part + "]")
                                            .toArray(String[]::new))
                            .trim());
        }

        // Create a process builder
        ProcessBuilder processBuilder = new ProcessBuilder(commandArguments);

        // Set working directory
        processBuilder.directory(workingDirectoryFile);

        // Set environment variables
        processBuilder.environment().putAll(environmentVariables);

        // Redirect stderr to stdout
        processBuilder.redirectErrorStream(true);

        // Start the process
        Process process = processBuilder.start();

        PrintStream printStream;

        // Create the correct print stream type based on the capture type
        switch (captureType) {
            case APPEND:
            case OVERWRITE: {
                printStream = new StringPrintStream();
                break;
            }
            default:
                printStream = System.out;
        }

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
                            printStream.println();
                        }
                        printStream.print(line);
                        addNewLine = true;
                        break;
                    }
                    default:
                        printStream.println("> " + line);
                        break;
                }
            }
        }

        // Based on the capture type, process the process builder output
        switch (captureType) {
            case APPEND: {
                String existingValue = variables.getOrDefault(captureVariable, "");
                String newValue = ((StringPrintStream) printStream).getString();

                variables.put(captureVariable, existingValue + newValue);
                variables.put(pipelineId + "." + jobId + "." + stepId + "." + captureVariable, newValue);
                variables.put(jobId + "." + stepId + "." + captureVariable, newValue);
                variables.put(stepId + "." + captureVariable, newValue);

                break;
            }
            case OVERWRITE: {
                String value = ((StringPrintStream) printStream).getString();

                variables.put(captureVariable, value);
                variables.put(pipelineId + "." + jobId + "." + stepId + "." + captureVariable, value);
                variables.put(jobId + "." + stepId + "." + captureVariable, value);
                variables.put(stepId + "." + captureVariable, value);

                break;
            }
            default: {
                break;
            }
        }

        // Return the process exit code
        return process.waitFor();
    }

    /**
     * Method to execute an extension directive
     *
     * @param context the context
     * @param command the command
     * @return the exit code
     * @throws Throwable if an exception occurs
     */
    private int executeExtension(Context context, String command) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("execute extension [%s]", command);
        }

        Console console = context.getConsole();

        // Emit the command
        console.info("$ %s", command);

        return 0;
    }

    /**
     * Method to get the CaptureType
     *
     * @param command the command
     * @return the CaptureType
     */
    private static CaptureType getCaptureType(String command) {
        String pattern = CAPTURE_APPEND_MATCHING_REGEX;
        if (command.matches(pattern)) {
            return CaptureType.APPEND;
        }

        pattern = CAPTURE_OVERWRITE_MATCHING_REGEX;
        if (command.matches(pattern)) {
            return CaptureType.OVERWRITE;
        }

        return CaptureType.NONE;
    }

    /**
     * Method to get the capture property
     *
     * @param captureType the capture type
     * @param command the command
     * @return the capture property
     */
    private static String getCaptureVariable(CaptureType captureType, String command) {
        switch (captureType) {
            case APPEND:
            case OVERWRITE: {
                return command.substring(command.lastIndexOf("$") + 1);
            }
            case NONE:
            default: {
                return null;
            }
        }
    }
}
