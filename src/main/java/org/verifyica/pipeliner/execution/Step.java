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

package org.verifyica.pipeliner.execution;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.Version;
import org.verifyica.pipeliner.common.ChecksumException;
import org.verifyica.pipeliner.common.Environment;
import org.verifyica.pipeliner.common.LineParser;
import org.verifyica.pipeliner.common.ShutdownHook;
import org.verifyica.pipeliner.execution.support.CaptureType;
import org.verifyica.pipeliner.execution.support.ExecutableCommand;
import org.verifyica.pipeliner.execution.support.Ipc;
import org.verifyica.pipeliner.execution.support.ProcessExecutableCommand;
import org.verifyica.pipeliner.execution.support.Resolver;
import org.verifyica.pipeliner.execution.support.Shell;
import org.verifyica.pipeliner.execution.support.Status;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.model.Enabled;
import org.verifyica.pipeliner.model.JobModel;
import org.verifyica.pipeliner.model.PipelineModel;
import org.verifyica.pipeliner.model.StepModel;
import org.verifyica.pipeliner.model.Variable;
import org.verifyica.pipeliner.parser.SyntaxException;

/** Class to implement Step */
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class Step extends Executable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Step.class);

    private static final String CAPTURE_APPEND_MATCHING_REGEX = ".*>>\\s*\\$[a-zA-Z0-9][a-zA-Z0-9\\-._]*$";

    private static final String CAPTURE_OVERWRITE_MATCHING_REGEX = ".*>\\s*\\$[a-zA-Z0-9][a-zA-Z0-9\\-._]*$";

    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    private PipelineModel pipelineModel;
    private JobModel jobModel;
    private final StepModel stepModel;
    private String run;

    /**
     * Constructor
     *
     * @param stepModel the step model
     */
    public Step(StepModel stepModel) {
        this.stepModel = stepModel;
    }

    @Override
    public void execute(Context context) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("executing step [%s] ...", stepModel);
        }

        prepare(context);

        jobModel = (JobModel) stepModel.getParent();
        pipelineModel = (PipelineModel) jobModel.getParent();
        run = stepModel.getRun();

        if (Boolean.TRUE.equals(Enabled.decode(stepModel.getEnabled()))) {
            getStopwatch().reset();

            getConsole().info("%s status=[%s]", stepModel, Status.RUNNING);

            run();

            Status status = getExitCode() == 0 ? Status.SUCCESS : Status.FAILURE;

            getConsole()
                    .info(
                            "%s status=[%s] exit-code=[%d] ms=[%d]",
                            stepModel,
                            status,
                            getExitCode(),
                            getStopwatch().elapsedTime().toMillis());
        } else {
            skip(context, Status.DISABLED);
        }
    }

    @Override
    public void skip(Context context, Status status) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("skipping step [%s] ...", stepModel);
        }

        prepare(context);

        Status effectiveStatus = Boolean.TRUE.equals(Enabled.decode(stepModel.getEnabled())) ? status : Status.DISABLED;
        getConsole().info("%s status=[%s]", stepModel, effectiveStatus);
    }

    /**
     * Method to run the step
     */
    private void run() {
        File ipcOutputFile = null;
        File ipcInputFile = null;

        try {
            // Parse the run command into a list of command
            // lines, merging any continuation lines
            List<String> commands = LineParser.parse(run);

            // Execute each command
            for (String command : commands) {
                // Get properties (current step, job, pipeline, context) and resolve them
                Map<String, String> properties = Resolver.resolveVariables(getVariables());

                // Get environment variables (current step, job, pipeline, context) and resolve them
                Map<String, String> environmentVariables =
                        Resolver.resolveEnvironmentVariables(getEnvironmentVariables(), properties);

                if (getConsole().isTraceEnabled()) {
                    environmentVariables.forEach((name, value) ->
                            getConsole().trace("%s resolved environment variable [%s] = [%s]", stepModel, name, value));

                    properties.forEach((name, value) ->
                            getConsole().trace("%s resolved variable [%s] = [%s]", stepModel, name, value));
                }

                if (getConsole().isTraceEnabled()) {
                    getConsole().trace("%s command [%s]", stepModel, command);
                }

                // Decode the shell
                Shell shell = Shell.decode(stepModel.getShell());

                if (getConsole().isTraceEnabled()) {
                    getConsole().trace("%s shell [%s]", stepModel, shell);
                }

                // Get the timeout minutes
                int timeoutMinutes = getTimeoutMinutes();

                if (getConsole().isTraceEnabled()) {
                    getConsole().trace("%s timeout minutes [%d]", stepModel, timeoutMinutes);
                }

                // Get the capture type
                CaptureType captureType = getCaptureType(command);

                if (getConsole().isTraceEnabled()) {
                    getConsole().trace("%s capture type [%s]", stepModel, captureType);
                }

                // Get the capture variable
                String captureVariable = getCaptureVariable(captureType, command);

                // Validate the capture variable
                if (captureType != CaptureType.NONE && Variable.isInvalid(captureVariable)) {
                    throw new IllegalArgumentException(
                            format("%s invalid capture variable [%s]", stepModel, captureVariable));
                }

                if (getConsole().isTraceEnabled()) {
                    getConsole().trace("%s capture variable [%s]", stepModel, captureVariable);
                }

                // Get the command without the capture variable
                String commandWithoutCaptureVariable;

                switch (captureType) {
                    case APPEND:
                        commandWithoutCaptureVariable =
                                command.substring(0, command.lastIndexOf(">>")).trim();
                        break;
                    case OVERWRITE: {
                        commandWithoutCaptureVariable =
                                command.substring(0, command.lastIndexOf(">")).trim();
                        break;
                    }
                    case NONE:
                    default: {
                        commandWithoutCaptureVariable = command;
                    }
                }

                if (getConsole().isTraceEnabled()) {
                    getConsole()
                            .trace(
                                    "%s command without capture variable [%s]",
                                    stepModel, commandWithoutCaptureVariable);
                }

                // Resolve variables in the command
                String commandWithVariablesResolved =
                        Resolver.replaceVariables(properties, commandWithoutCaptureVariable);

                if (getConsole().isTraceEnabled()) {
                    getConsole()
                            .trace("%s command with variables resolved [%s]", stepModel, commandWithVariablesResolved);
                }

                // Get the working directory
                String workingDirectory = getWorkingDirectory(environmentVariables, properties);

                if (getConsole().isTraceEnabled()) {
                    getConsole().trace("%s working-directory=[%s]", stepModel, workingDirectory);
                }

                // If configured, mask the command
                if (!Constants.TRUE.equals(properties.get(Constants.PIPELINER_MASK_COMMANDS))) {
                    // If configured, mask the properties
                    if (Constants.TRUE.equals(properties.get(Constants.PIPELINER_MASK_VARIABLES))) {
                        getConsole().info("$ %s", command);
                    } else {
                        getConsole().info("$ %s", commandWithVariablesResolved);
                    }
                }

                if (getConsole().isTraceEnabled()) {
                    getConsole().trace("%s creating IPC files ...", stepModel);
                }

                // Create the IPC files
                ipcOutputFile = Ipc.createIpcFile();
                ipcInputFile = Ipc.createIpcFile();

                if (getConsole().isTraceEnabled()) {
                    getConsole()
                            .trace(
                                    "%s Ipc file [%s] = [%s]",
                                    stepModel, Constants.PIPELINER_IPC_OUT, ipcOutputFile.getAbsolutePath());

                    getConsole()
                            .trace(
                                    "%s Ipc file [%s] = [%s]",
                                    stepModel, Constants.PIPELINER_IPC_IN, ipcInputFile.getAbsolutePath());

                    getConsole().trace("%s writing IPC file [%s]", stepModel, ipcOutputFile);
                }

                // Write the properties to the outbound IPC file
                Ipc.write(ipcOutputFile, properties);

                // Add the IPC environment variables
                environmentVariables.put(Constants.PIPELINER_IPC_IN, ipcOutputFile.getAbsolutePath());
                environmentVariables.put(Constants.PIPELINER_IPC_OUT, ipcInputFile.getAbsolutePath());
                environmentVariables.put(Constants.PIPELINER_IPC, ipcInputFile.getAbsolutePath());

                ExecutableCommand executableCommand;

                // Determine the command executor based on the command type
                if (command.startsWith(Constants.DIRECTIVE_COMMAND_PREFIX)
                        && !command.startsWith(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX)) {
                    // Create the directive executable command
                    executableCommand = createDirectiveExecutableCommand(
                            environmentVariables,
                            workingDirectory,
                            shell,
                            command,
                            commandWithVariablesResolved,
                            properties,
                            captureType);
                } else {
                    // Process a regular command or a --pipeline directive

                    // Replace the --pipeline directive with the $PIPELINER environment variable
                    if (commandWithVariablesResolved.startsWith(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX + " ")) {
                        commandWithVariablesResolved = commandWithVariablesResolved.replaceFirst(
                                Pattern.quote(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX),
                                Environment.getenv(Constants.PIPELINER) + " ");
                    }

                    // Create a process executable command
                    executableCommand = new ProcessExecutableCommand(
                            getConsole(),
                            environmentVariables,
                            workingDirectory,
                            shell,
                            commandWithVariablesResolved,
                            captureType);
                }

                // Execute the command and get the result
                executableCommand.execute(timeoutMinutes);

                // Get the exit code
                int exitCode = executableCommand.getExitCode();

                if (getConsole().isTraceEnabled()) {
                    getConsole().trace("executable command returned exit code [%d]", exitCode);
                }

                // Set the step exit code
                setExitCode(exitCode);

                // Exit if the exit code is not 0
                if (getExitCode() != 0) {
                    break;
                }

                // If the capture type is not NONE, store the captured variable
                if (captureType != CaptureType.NONE) {
                    String processOutput = executableCommand.getProcessOutput();
                    storeCaptureVariable(captureVariable, processOutput, captureType);
                }

                if (getConsole().isTraceEnabled()) {
                    getConsole().trace("%s reading IPC file [%s]", stepModel, ipcInputFile);
                }

                // Read the properties from the inbound IPC file
                Map<String, String> map = Ipc.read(ipcInputFile);

                // Store the captured IPC properties
                map.forEach((key, value) -> {
                    if (getConsole().isTraceEnabled()) {
                        getConsole().trace("%s IPC return variable [%s] = [%s]", stepModel, key, value);
                    }
                    storeCaptureVariable(key, value, CaptureType.OVERWRITE);
                });
            }

            // If shutdown hooks are enabled, cleanup the IPC files proactively
            if (ShutdownHook.isEnabled()) {
                // Cleanup the IPC files
                Ipc.cleanup(ipcInputFile);
                Ipc.cleanup(ipcOutputFile);
            }
        } catch (Throwable t) {
            // If shutdown hooks are enabled, cleanup the IPC files proactively
            if (ShutdownHook.isEnabled()) {
                // Cleanup the IPC files
                Ipc.cleanup(ipcInputFile);
                Ipc.cleanup(ipcOutputFile);
            }

            if (getConsole().isTraceEnabled()) {
                t.printStackTrace(System.out);
            }

            getConsole().error("%s -> %s", stepModel, t.getMessage());

            setExitCode(1);
        }
    }

    /**
     * Method to build a directive executable command
     *
     * @param environmentVariables the environment variables
     * @param workingDirectory the workingDirectory
     * @param shell the shell
     * @param command the command
     * @param commandWithPropertiesResolved the command with properties resolved
     * @param properties the properties map
     * @param captureType the capture type
     * @return a CommandExecutor
     * @throws IOException if an I/O error occurs
     * @throws ChecksumException If the checksum is invalid
     */
    private ExecutableCommand createDirectiveExecutableCommand(
            Map<String, String> environmentVariables,
            String workingDirectory,
            Shell shell,
            String command,
            String commandWithPropertiesResolved,
            Map<String, String> properties,
            CaptureType captureType)
            throws IOException, ChecksumException, SyntaxException {
        // Check if the command is an extension directive
        if (command.startsWith(Constants.EXTENSION_DIRECTIVE_COMMAND_PREFIX + " ")) {
            // Build the extension directive command executor
            return createExtensionExecutableCommand(
                    environmentVariables,
                    workingDirectory,
                    shell,
                    command,
                    commandWithPropertiesResolved,
                    properties,
                    captureType);
        } else {
            // Unknown directive
            throw new IllegalArgumentException(format("unknown directive [%s]", command));
        }
    }

    /**
     * Method to create an extension executable command
     *
     * @param environmentVariables the environment variables
     * @param workingDirectory the working directory
     * @param shell the shell
     * @param command the command
     * @param commandWithPropertiesResolved the command with properties resolved
     * @param properties the properties map
     * @param captureType the capture type
     * @return a CommandExecutor
     * @throws IOException if an I/O error occurs
     * @throws ChecksumException If the checksum is invalid
     */
    private ProcessExecutableCommand createExtensionExecutableCommand(
            Map<String, String> environmentVariables,
            String workingDirectory,
            Shell shell,
            String command,
            String commandWithPropertiesResolved,
            Map<String, String> properties,
            CaptureType captureType)
            throws IOException, ChecksumException, SyntaxException {
        String[] tokens = commandWithPropertiesResolved.split("\\s+");

        if (tokens.length < 2 || tokens.length > 3) {
            throw new IllegalArgumentException(format("invalid --extension directive [%s]", command));
        }

        // Get the extension url
        String url = tokens[1];

        // Resolve properties in the url
        url = Resolver.replaceVariables(properties, url);

        // Resolve environment variables in the url
        url = Resolver.replaceEnvironmentVariables(environmentVariables, url);

        if (getConsole().isTraceEnabled()) {
            getConsole().trace("%s extension url [%s]", stepModel, url);
        }

        String checksum = null;

        if (tokens.length == 3) {
            // Get the extension checksum
            checksum = tokens[2];

            // Resolve properties in the checksum
            checksum = Resolver.replaceVariables(properties, checksum);

            // Resolve environment variables in the checksum
            checksum = Resolver.replaceEnvironmentVariables(environmentVariables, checksum);
        }

        if (getConsole().isTraceEnabled()) {
            getConsole().trace("%s extension checksum [%s]", stepModel, checksum);
        }

        // Get the extension shell script
        String shellScript = getExtensionManager()
                .getShellScript(environmentVariables, properties, workingDirectory, url, checksum)
                .toString();

        if (getConsole().isTraceEnabled()) {
            getConsole().trace("%s extension shell script [%s]", stepModel, shellScript);
        }

        // Get the parent working directory of the extension shell script
        String parentWorkingDirectory = Paths.get(shellScript).getParent().toString();

        // Create the command executor for the extension shell script
        return new ProcessExecutableCommand(
                getConsole(), environmentVariables, parentWorkingDirectory, shell, shellScript, captureType);
    }

    /**
     * Method to get a Map of merged environment variables
     *
     * @return a Map of merged environment variables
     */
    private Map<String, String> getEnvironmentVariables() {
        Map<String, String> map = new TreeMap<>();

        // Add all environment variables
        map.putAll(Environment.getenv());
        map.putAll(pipelineModel.getEnv());
        map.putAll(jobModel.getEnv());
        map.putAll(stepModel.getEnv());

        // Reset environment variables that shouldn't be overwritten
        map.put(Constants.PWD, Environment.getenv(Constants.PWD));
        map.put(Constants.PIPELINER_VERSION, Version.getVersion());
        map.put(Constants.PIPELINER_HOME, Environment.getenv(Constants.PIPELINER_HOME));
        map.put(Constants.PIPELINER, Environment.getenv(Constants.PIPELINER));
        map.put(Constants.PIPELINER_TMP, System.getProperty(JAVA_IO_TMPDIR));

        if (getConsole().isTraceEnabled()) {
            map.put(Constants.PIPELINER_TRACE, Constants.TRUE);
        }

        return map;
    }

    /**
     * Method to get a Map of merge variables
     *
     * @return a Map of merged variables
     */
    private Map<String, String> getVariables() {
        Map<String, String> map = new TreeMap<>();

        // Add pipeline defined properties
        map.putAll(pipelineModel.getWith());

        // Add job defined properties
        map.putAll(jobModel.getWith());

        // Add step defined properties
        map.putAll(stepModel.getWith());

        // Add context properties
        map.putAll(getContext().getWith());

        // Create a new TreeMap with keys converted to upper case
        Map<String, String> upperCaseMap = new TreeMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            upperCaseMap.put(entry.getKey().toUpperCase(Locale.ROOT), entry.getValue());
        }

        return upperCaseMap;
    }

    /**
     * Method to store a captured variable in the context
     *
     * @param key the key
     * @param value the value
     * @param captureType the capture type
     */
    private void storeCaptureVariable(String key, String value, CaptureType captureType) {
        // Resolve the capture prefix
        String capturePrefix = stepModel.getCapturePrefix();

        if (capturePrefix == null) {
            // Set the default capture prefix
            capturePrefix = "";
        }

        String upperCaseCaptureVariable = capturePrefix + key.toUpperCase(Locale.ROOT);

        switch (captureType) {
            case APPEND: {
                // Get the existing variable value
                String existingValue = getContext().getWith().getOrDefault(upperCaseCaptureVariable, "");

                // Store the append the value to the existing value
                getContext().getWith().put(upperCaseCaptureVariable, existingValue + value);

                break;
            }
            case OVERWRITE: {
                // Store the variable
                getContext().getWith().put(upperCaseCaptureVariable, value);

                break;
            }
            case NONE:
            default: {
                break;
            }
        }
    }

    /**
     * Method to get the working directory
     *
     * @param environmentVariables the environment variables
     * @param properties the properties
     * @return the working directory
     */
    private String getWorkingDirectory(Map<String, String> environmentVariables, Map<String, String> properties)
            throws SyntaxException {
        // Resolve the working directory, more specific to less specific

        String workingDirectory = stepModel.getWorkingDirectory();

        if (workingDirectory == null) {
            workingDirectory = jobModel.getWorkingDirectory();
        }

        if (workingDirectory == null) {
            workingDirectory = pipelineModel.getWorkingDirectory();
        }

        if (workingDirectory == null) {
            workingDirectory = ".";
        }

        // Replace properties in the working directory
        workingDirectory = Resolver.replaceVariables(properties, workingDirectory);

        // Replace environment variables in the working directory
        workingDirectory = Resolver.replaceEnvironmentVariables(environmentVariables, workingDirectory);

        // Check if the working directory exists
        Path workingDirectoryPath = Paths.get(workingDirectory);

        // Check if the working directory exists
        if (!workingDirectoryPath.toFile().exists()) {
            throw new IllegalArgumentException(
                    format("%s -> working-directory=[%s] does not exist", stepModel, workingDirectory));
        }

        // Check if the working directory is a directory
        if (!workingDirectoryPath.toFile().isDirectory()) {
            throw new IllegalArgumentException(
                    format("%s -> working-directory=[%s] is not a directory", stepModel, workingDirectory));
        }

        return workingDirectory;
    }

    /**
     * Method to get the timeout minutes
     *
     * @return the timeout minutes
     */
    private int getTimeoutMinutes() {
        // Resolve the timeout minutes, more specific to less specific

        String timeoutMinutes = stepModel.getTimeoutMinutes();

        if (timeoutMinutes == null) {
            timeoutMinutes = jobModel.getTimeoutMinutes();
        }

        if (timeoutMinutes == null) {
            timeoutMinutes = pipelineModel.getTimeoutMinutes();
        }

        if (timeoutMinutes == null) {
            timeoutMinutes = String.valueOf(Integer.MAX_VALUE);
        }

        return Integer.parseInt(timeoutMinutes);
    }

    /**
     * Method to get the CaptureType
     *
     * @param command the command
     * @return the CaptureType
     */
    private CaptureType getCaptureType(String command) {
        if (command.matches(CAPTURE_APPEND_MATCHING_REGEX)) {
            return CaptureType.APPEND;
        }

        if (command.matches(CAPTURE_OVERWRITE_MATCHING_REGEX)) {
            return CaptureType.OVERWRITE;
        }

        return CaptureType.NONE;
    }

    /**
     * Method to get the capture variable
     *
     * @param captureType the capture type
     * @param command the command
     * @return the capture variable
     */
    private String getCaptureVariable(CaptureType captureType, String command) {
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
