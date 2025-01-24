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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.common.Environment;
import org.verifyica.pipeliner.common.LineParser;
import org.verifyica.pipeliner.common.io.StringPrintStream;
import org.verifyica.pipeliner.core.support.Ipc;
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
    private List<String> commands;

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
    public void validate() {
        validateId();
        validateEnabled();
        validateEnv();
        validateWith();
        validateWorkingDirectory();
        validateTimeoutMinutes();
        validateShell();
        validateRun();
        parseCommands();
    }

    @Override
    public int execute(Context context) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("executing step [%s] ...", this);
        }

        // Get the console
        Console console = context.getConsole();

        // Declare the exit code
        int exitCode = 0;

        // If the step is enabled, execute it
        if (Boolean.TRUE.equals(Enabled.decode(getEnabled()))) {
            // Reset the stopwatch
            getStopwatch().reset();

            // Emit the status
            console.emit("%s status=[%s]", this, Status.RUNNING);

            try {
                // Run the step
                exitCode = run(context);
            } catch (Throwable t) {
                // Emit the error
                console.emit("@error %s message=[%s]", this, t.getMessage());

                if (LOGGER.isTraceEnabled()) {
                    t.printStackTrace(System.out);
                }

                exitCode = 1;
            }

            // Get the status based on the exit code
            Status status = exitCode == 0 ? Status.SUCCESS : Status.FAILURE;

            // Emit the status
            console.emit(
                    "%s status=[%s] exit-code=[%d] ms=[%d]",
                    this, status, exitCode, getStopwatch().elapsedTime().toMillis());

            return exitCode;
        } else {
            skip(context, Status.DISABLED);

            return 0;
        }
    }

    @Override
    public void skip(Context context, Status status) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("skipping step [%s] ...", this);
        }

        // Get the console
        Console console = context.getConsole();

        // Get the status based on whether the job is enabled
        Status effectiveStatus = Boolean.TRUE.equals(Enabled.decode(getEnabled())) ? status : Status.DISABLED;

        // Emit the status
        console.emit("%s status=[%s]", this, effectiveStatus);
    }

    @Override
    public String toString() {
        return "@step" + super.toString();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
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

        // Parse the commands
        List<String> commands = LineParser.parse(run);

        // Validate the step has at least one command
        if (commands.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> run is blank", this));
        }

        // Loop through the commands
        for (String command : commands) {
            // If the line is a directive, validate it is a known directive
            if (command.startsWith(Constants.DIRECTIVE_COMMAND_PREFIX)
                    && (!(command.startsWith(Constants.EXTENSION_DIRECTIVE_COMMAND_PREFIX + " ")
                            || command.startsWith(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX + " ")))) {
                throw new PipelineDefinitionException(format("%s -> unknown directive [%s]", this, command));
            }
        }
    }

    /**
     * Method to parse the run content into commands
     */
    private void parseCommands() {
        // Pare the run content into commands
        commands = LineParser.parse(run);
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

        // Process each command
        for (String command : commands) {
            if (command.startsWith(Constants.EXTENSION_DIRECTIVE_COMMAND_PREFIX)) {
                // Run the extension command
                exitCode = runExtensionCommand(context, command);
            } else {
                // Run the command
                exitCode = runCommand(context, command);
            }

            // Break if the command was not successful
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
    private int runCommand(Context context, String command) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("execute command [%s]", command);
        }

        // Get the console
        Console console = context.getConsole();

        // Declare the IPC files so we can clean them up in a finally block
        File pipelinerToProcessIpcFile = null;
        File processToPipelinerIpcFile = null;

        try {
            // Emit the command
            console.emit("$ %s", command);

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
            environmentVariables.putAll(Environment.getenv());

            // Set the PIPELINER_TMP environment variable
            environmentVariables.put(Constants.PIPELINER_TMP, System.getProperty("java.io.tmpdir"));

            // Build the map of scoped variables
            Map<String, String> variables = buildScopedVariables(
                    context.getWith(), pipeline.getWith(), job.getWith(), getWith(), pipelineId, jobId, stepId);

            // Resolve variables
            variables = Resolver.resolveVariables(variables);

            // Resolve environment variables
            environmentVariables = Resolver.resolveEnvironmentVariables(environmentVariables, variables);

            // Create the IPC out file
            pipelinerToProcessIpcFile = Ipc.createIpcFile();

            // Write the variables to the IPC out file
            Ipc.write(pipelinerToProcessIpcFile, variables);

            // Add the IPC out file as an environment variable
            environmentVariables.put(
                    Constants.PIPELINER_IPC_IN,
                    pipelinerToProcessIpcFile.getAbsoluteFile().getCanonicalPath());

            // Create the IPC in file
            processToPipelinerIpcFile = Ipc.createIpcFile();

            // Add the IPC in file as an environment variable
            environmentVariables.put(
                    Constants.PIPELINER_IPC_OUT,
                    processToPipelinerIpcFile.getAbsoluteFile().getCanonicalPath());

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

                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("append capture variable [%s]", captureVariable);
                    }

                    // Remove the capture variable from the command
                    command = command.substring(0, command.lastIndexOf(">>")).trim();

                    break;
                }
                case OVERWRITE: {
                    if (Variable.isInvalid(captureVariable)) {
                        throw new IllegalStateException(format("invalid capture variable [%s]", captureVariable));
                    }

                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("overwrite capture variable [%s]", captureVariable);
                    }

                    // Remove the capture variable from the command
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
            File workingDirectoryFile =
                    new File(workingDirectory).getAbsoluteFile().getCanonicalFile();

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

            // Declare the print stream
            StringPrintStream printStream = null;

            // Create the correct print stream type based on the capture type
            switch (captureType) {
                case APPEND:
                case OVERWRITE: {
                    // Create a print stream to capture the output
                    printStream = new StringPrintStream();
                    break;
                }
                default: {
                    // The code will use the console
                    break;
                }
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
                            // Emit the output
                            console.emit("> " + line);
                            break;
                    }
                }
            }

            // Based on the capture type, process the process builder output
            switch (captureType) {
                case APPEND: {
                    // Get the existing capture variable value
                    String existingValue = variables.getOrDefault(captureVariable, "");

                    // Get the captured output
                    String capturedOutput = printStream.getString();

                    // Create the new value
                    String newValue = existingValue + capturedOutput;

                    // Add the new capture variable value (existing value + captured output)
                    variables.put(captureVariable, newValue);

                    if (stepId != null) {
                        variables.put(stepId + "." + captureVariable, newValue);
                    }

                    if (jobId != null && stepId != null) {
                        variables.put(jobId + "." + stepId + "." + captureVariable, newValue);
                    }

                    if (pipelineId != null && jobId != null && stepId != null) {
                        variables.put(pipelineId + "." + jobId + "." + stepId + "." + captureVariable, newValue);
                    }

                    context.getWith().putAll(variables);

                    break;
                }
                case OVERWRITE: {
                    // Get the captured output
                    String capturedOutput = printStream.getString();

                    // Add the captured variable
                    variables.put(captureVariable, capturedOutput);

                    if (stepId != null) {
                        variables.put(stepId + "." + captureVariable, capturedOutput);
                    }

                    if (jobId != null && stepId != null) {
                        variables.put(jobId + "." + stepId + "." + captureVariable, capturedOutput);
                    }

                    if (pipelineId != null && jobId != null && stepId != null) {
                        variables.put(pipelineId + "." + jobId + "." + stepId + "." + captureVariable, capturedOutput);
                    }

                    context.getWith().putAll(variables);

                    break;
                }
                default: {
                    break;
                }
            }

            // Get the exit code
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // Read the IPC in file
                Map<String, String> outputVariables = Ipc.read(processToPipelinerIpcFile);

                // Get the context variable map
                Map<String, String> with = context.getWith();

                for (Map.Entry<String, String> entry : outputVariables.entrySet()) {
                    // Get the variable name
                    String name = entry.getKey();

                    // Get the variable value
                    String value = entry.getValue();

                    // Add the IPC variables
                    with.put(name, value);

                    if (stepId != null) {
                        with.put(stepId + "." + name, value);

                        if (jobId != null) {
                            with.put(jobId + "." + stepId + "." + name, value);

                            if (pipelineId != null) {
                                with.put(pipelineId + "." + jobId + "." + stepId + "." + name, value);
                            }
                        }
                    }
                }
            }

            return exitCode;
        } finally {
            // Clean up IPC files proactively
            Ipc.cleanup(pipelinerToProcessIpcFile);
            Ipc.cleanup(processToPipelinerIpcFile);
        }
    }

    /**
     * Method to execute an extension directive
     *
     * @param context the context
     * @param command the command
     * @return the exit code
     * @throws Throwable if an exception occurs
     */
    private int runExtensionCommand(Context context, String command) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("execute extension [%s]", command);
        }

        Console console = context.getConsole();

        // Emit the command
        console.emit("$ %s", command);

        return 0;
    }

    /**
     * Method to build the scoped variables
     *
     * @param contextVariables the context variables
     * @param pipelineVariables the pipeline variables
     * @param jobVariables the job variables
     * @param stepVariables the step variables
     * @param pipelineId the pipeline id
     * @param jobId the job id
     * @param stepId the step id
     * @return a map of scoped variables
     */
    public static Map<String, String> buildScopedVariables(
            Map<String, String> contextVariables,
            Map<String, String> pipelineVariables,
            Map<String, String> jobVariables,
            Map<String, String> stepVariables,
            String pipelineId,
            String jobId,
            String stepId) {

        // Initialize the map of variables
        Map<String, String> variables = new TreeMap<>();

        // Add unscoped variables from each level
        variables.putAll(pipelineVariables);
        variables.putAll(jobVariables);
        variables.putAll(stepVariables);
        variables.putAll(contextVariables);

        // Inline method for adding scoped variables
        BiConsumer<Map<String, String>, String> addScopedVariables = (source, scopePrefix) -> {
            for (Map.Entry<String, String> entry : source.entrySet()) {
                variables.put(scopePrefix + "." + entry.getKey(), entry.getValue());
            }
        };

        // Add scoped variables for pipeline level
        if (pipelineId != null) {
            // Scope: pipelineId.key
            addScopedVariables.accept(pipelineVariables, pipelineId);

            // Add scoped variables for job level
            if (jobId != null) {
                // Scope: jobId.key
                addScopedVariables.accept(jobVariables, jobId);

                // Scope: pipelineId.jobId.key
                addScopedVariables.accept(jobVariables, pipelineId + "." + jobId);

                // Add scoped variables for step level
                if (stepId != null) {
                    // Scope: stepId.key
                    addScopedVariables.accept(stepVariables, stepId);

                    // Scope: jobId.stepId.key
                    addScopedVariables.accept(stepVariables, jobId + "." + stepId);

                    // Scope: pipelineId.jobId.stepId.key
                    addScopedVariables.accept(stepVariables, pipelineId + "." + jobId + "." + stepId);
                }
            } else if (stepId != null) {
                // Scope: stepId.key
                addScopedVariables.accept(stepVariables, stepId);

                // Scope: pipelineId.stepId.key
                addScopedVariables.accept(stepVariables, pipelineId + "." + stepId);
            }
        } else if (jobId != null) {
            // Scope: jobId.key
            addScopedVariables.accept(jobVariables, jobId);

            if (stepId != null) {
                // Scope: stepId.key
                addScopedVariables.accept(stepVariables, stepId);

                // Scope: jobId.stepId.key
                addScopedVariables.accept(stepVariables, jobId + "." + stepId);
            }
        } else if (stepId != null) {
            // Scope: stepId.key
            addScopedVariables.accept(stepVariables, stepId);
        }

        return variables;
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
