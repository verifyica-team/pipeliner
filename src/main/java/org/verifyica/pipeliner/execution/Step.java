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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.Pipeliner;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.Environment;
import org.verifyica.pipeliner.common.MultiLineMerger;
import org.verifyica.pipeliner.execution.support.CaptureType;
import org.verifyica.pipeliner.execution.support.CommandExecutor;
import org.verifyica.pipeliner.execution.support.Ipc;
import org.verifyica.pipeliner.execution.support.Resolver;
import org.verifyica.pipeliner.execution.support.Shell;
import org.verifyica.pipeliner.execution.support.Status;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.model.Enabled;
import org.verifyica.pipeliner.model.JobModel;
import org.verifyica.pipeliner.model.Model;
import org.verifyica.pipeliner.model.PipelineModel;
import org.verifyica.pipeliner.model.Property;
import org.verifyica.pipeliner.model.StepModel;

/** Class to implement Step */
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class Step extends Executable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Step.class);

    private static final String CAPTURE_APPEND_MATCHING_REGEX = ".*>>\\s*\\$[a-zA-Z0-9][a-zA-Z0-9\\-._]*$";

    private static final String CAPTURE_OVERWRITE_MATCHING_REGEX = ".*>\\s*\\$[a-zA-Z0-9][a-zA-Z0-9\\-._]*$";

    private static final String[] SCOPE_SEPARATOR = {".", "/"};

    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    private PipelineModel pipelineModel;
    private JobModel jobModel;
    private final StepModel stepModel;
    private String run;

    /**
     * Constructor
     *
     * @param stepModel stepModel
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
        Console console = getConsole();
        boolean isDebugEnabled = console.isDebugEnabled();

        File ipcOutputFile = null;
        File ipcInputFile = null;

        try {
            // Merge run command lines if run is multiline
            List<String> commands = MultiLineMerger.merge(Arrays.asList(run.split("\\R")));

            // Execute each command
            for (String command : commands) {
                // Get properties (current step, job, pipeline, context) and resolve them
                Map<String, String> properties = Resolver.resolveProperties(getProperties());

                // Get environment variables (current step, job, pipeline, context) and resolve them
                Map<String, String> environmentVariables =
                        Resolver.resolveEnvironmentVariables(getEnvironmentVariables(), properties);

                if (isDebugEnabled) {
                    environmentVariables.forEach((name, value) ->
                            console.trace("%s resolved environment variable [%s] = [%s]", stepModel, name, value));

                    properties.forEach(
                            (name, value) -> console.trace("%s resolved property [%s] = [%s]", stepModel, name, value));
                }

                if (isDebugEnabled) {
                    console.trace("%s command [%s]", stepModel, command);
                }

                // Decode the shell
                Shell shell = Shell.decode(stepModel.getShell());

                if (isDebugEnabled) {
                    console.trace("%s shell [%s]", stepModel, shell);
                }

                // Get the timeout minutes
                int timeoutMinutes = getTimeoutMinutes();

                if (isDebugEnabled) {
                    console.trace("%s timeout minutes [%d]", stepModel, timeoutMinutes);
                }

                // Get the capture type
                CaptureType captureType = getCaptureType(command);

                if (isDebugEnabled) {
                    console.trace("%s capture type [%s]", stepModel, captureType);
                }

                // Get the capture property
                String captureProperty = getCaptureProperty(captureType, command);

                // Validate the capture property
                if (captureType != CaptureType.NONE && !Property.isValid(captureProperty)) {
                    throw new IllegalArgumentException(
                            format("%s invalid capture property [%s]", stepModel, captureProperty));
                }

                if (isDebugEnabled) {
                    console.trace("%s capture property [%s]", stepModel, captureProperty);
                }

                // Get the command without the capture property
                String commandWithoutCaptureProperty;

                switch (captureType) {
                    case APPEND:
                        commandWithoutCaptureProperty =
                                command.substring(0, command.lastIndexOf(">>")).trim();
                        break;
                    case OVERWRITE: {
                        commandWithoutCaptureProperty =
                                command.substring(0, command.lastIndexOf(">")).trim();
                        break;
                    }
                    case NONE:
                    default: {
                        commandWithoutCaptureProperty = command;
                    }
                }

                if (isDebugEnabled) {
                    console.trace("%s command without capture property [%s]", stepModel, commandWithoutCaptureProperty);
                }

                // Resolve properties in the command
                String commandWithPropertiesResolved =
                        Resolver.replaceProperties(properties, commandWithoutCaptureProperty);

                if (isDebugEnabled) {
                    console.trace("%s command with properties resolved [%s]", stepModel, commandWithPropertiesResolved);
                }

                // Get the working directory
                String workingDirectory = getWorkingDirectory(environmentVariables, properties);

                if (isDebugEnabled) {
                    console.trace("%s working directory [%s]", stepModel, workingDirectory);
                }

                // Check if the working directory exists
                Path workingDirectoryPath = Paths.get(workingDirectory);

                if (!workingDirectoryPath.toFile().exists()) {
                    throw new IllegalArgumentException(
                            format("%s -> working-directory=[%s] does not exist", stepModel, workingDirectory));
                }

                if (!workingDirectoryPath.toFile().isDirectory()) {
                    throw new IllegalArgumentException(
                            format("%s -> working-directory=[%s] is not a directory", stepModel, workingDirectory));
                }

                // If configured, mask the properties
                if (Constants.TRUE.equals(properties.get(Constants.PIPELINER_MASK_PROPERTIES))) {
                    console.info("$ %s", command);
                } else {
                    console.info("$ %s", commandWithPropertiesResolved);
                }

                if (isDebugEnabled) {
                    console.trace("%s creating IPC files ...", stepModel);
                }

                // Create the IPC files
                ipcOutputFile = Ipc.createIpcFile();
                ipcInputFile = Ipc.createIpcFile();

                if (isDebugEnabled) {
                    console.trace(
                            "%s Ipc file [%s] = [%s]",
                            stepModel, Constants.PIPELINER_IPC_OUT, ipcOutputFile.getAbsolutePath());
                    console.trace(
                            "%s Ipc file [%s] = [%s]",
                            stepModel, Constants.PIPELINER_IPC_IN, ipcInputFile.getAbsolutePath());
                }

                if (isDebugEnabled) {
                    console.trace("%s writing IPC file [%s]", stepModel, ipcOutputFile);
                }

                // Write to the IPC file
                Ipc.write(ipcOutputFile, properties);

                // Add the IPC environment variables
                environmentVariables.put(Constants.PIPELINER_IPC_IN, ipcOutputFile.getAbsolutePath());
                environmentVariables.put(Constants.PIPELINER_IPC_OUT, ipcInputFile.getAbsolutePath());
                environmentVariables.put(Constants.PIPELINER_IPC, ipcInputFile.getAbsolutePath());

                CommandExecutor commandExecutor;

                if (command.startsWith(Constants.PIPELINER_DIRECTIVE_COMMAND_PREFIX)) {
                    // The command is a directive

                    // Check if the command is an extension directive
                    if (command.startsWith(Constants.PIPELINER_EXTENSION_DIRECTIVE_COMMAND_PREFIX)) {
                        String[] tokens = commandWithPropertiesResolved.split("\\s+");

                        if (tokens.length < 2 || tokens.length > 3) {
                            throw new IllegalArgumentException(format("invalid --extension directive [%s]", command));
                        }

                        // Get the extension url
                        String url = tokens[1];

                        // Resolve properties in the url
                        url = Resolver.replaceProperties(properties, url);

                        // Resolve environment variables in the url
                        url = Resolver.replaceEnvironmentVariables(environmentVariables, url);

                        if (isDebugEnabled) {
                            console.trace("%s extension url [%s]", stepModel, url);
                        }

                        String checksum = null;

                        if (tokens.length == 3) {
                            // Get the extension checksum
                            checksum = tokens[2];

                            // Resolve properties in the checksum
                            checksum = Resolver.replaceProperties(properties, checksum);

                            // Resolve environment variables in the checksum
                            checksum = Resolver.replaceEnvironmentVariables(environmentVariables, checksum);
                        }

                        if (isDebugEnabled) {
                            console.trace("%s extension checksum [%s]", stepModel, checksum);
                        }

                        // Get the extension shell script
                        String extensionShellScript = getExtensionManager()
                                .getExtensionShellScript(
                                        environmentVariables, properties, workingDirectory, url, checksum)
                                .toString();

                        if (isDebugEnabled) {
                            console.trace("%s extension command [%s]", stepModel, extensionShellScript);
                        }

                        // Reset the working directory to the directory of the extension shell script
                        workingDirectory =
                                Paths.get(extensionShellScript).getParent().toString();

                        // Execute the extension shell script
                        commandExecutor = new CommandExecutor(
                                console,
                                environmentVariables,
                                workingDirectory,
                                shell,
                                extensionShellScript,
                                captureType);
                    } else {
                        throw new IllegalArgumentException(
                                format("unknown directive [%s]", commandWithPropertiesResolved));
                    }
                } else {
                    // The command is a regular command

                    // Execute the command
                    commandExecutor = new CommandExecutor(
                            console,
                            environmentVariables,
                            workingDirectory,
                            shell,
                            commandWithPropertiesResolved,
                            captureType);
                }

                // Execute the command and get the exit code
                commandExecutor.execute(timeoutMinutes);

                // Get the exit code
                int exitCode = commandExecutor.getExitCode();

                // Set the exit code
                setExitCode(exitCode);

                // Exit if the exit code is not 0
                if (getExitCode() != 0) {
                    break;
                }

                // If the capture type is not NONE, store the captured property
                if (captureType != CaptureType.NONE) {
                    String processOutput = commandExecutor.getProcessOutput();
                    storeCaptureProperty(captureProperty, processOutput, captureType);
                }

                if (isDebugEnabled) {
                    console.trace("%s reading IPC file [%s]", stepModel, ipcInputFile);
                }

                // Read the IPC file
                Map<String, String> map = Ipc.read(ipcInputFile);

                // Store the captured properties
                map.forEach((property, value) -> {
                    if (isDebugEnabled) {
                        console.trace("%s IPC return property [%s] = [%s]", stepModel, property, value);
                    }
                    storeCaptureProperty(property, value, CaptureType.OVERWRITE);
                });
            }
        } catch (Throwable t) {
            // Cleanup the IPC files
            Ipc.cleanup(ipcInputFile);
            Ipc.cleanup(ipcOutputFile);

            if (console.isDebugEnabled()) {
                t.printStackTrace(System.out);
            }

            console.error("%s -> %s", stepModel, t.getMessage());
            setExitCode(1);
        }
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
        map.put(Constants.PIPELINER_VERSION, Pipeliner.getVersion());
        map.put(Constants.PIPELINER_HOME, Environment.getenv(Constants.PIPELINER_HOME));
        map.put(Constants.PIPELINER, Environment.getenv(Constants.PIPELINER));
        map.put(Constants.PIPELINER_TMP, System.getProperty(JAVA_IO_TMPDIR));

        if (getConsole().isDebugEnabled()) {
            map.put(Constants.PIPELINER_TRACE, Constants.TRUE);
        }

        return map;
    }

    /**
     * Method to get a Map of merge properties
     *
     * @return a Map of merged properties
     */
    private Map<String, String> getProperties() {
        Map<String, String> map = new TreeMap<>();

        // Add all properties
        map.putAll(pipelineModel.getWith());
        map.putAll(jobModel.getWith());
        map.putAll(stepModel.getWith());

        // Add scoped properties
        for (String scopeSeparator : SCOPE_SEPARATOR) {
            if (haveIds(pipelineModel)) {
                // Add pipeline scoped properties
                pipelineModel
                        .getWith()
                        .forEach((key, value) -> map.put(pipelineModel.getId() + scopeSeparator + key, value));
            }

            jobModel.getWith().forEach((key, value) -> {
                if (haveIds(pipelineModel, jobModel)) {
                    // Add pipeline / job scoped properties
                    map.put(pipelineModel.getId() + scopeSeparator + jobModel.getId() + scopeSeparator + key, value);
                }

                if (jobModel.getId() != null) {
                    // Add job scoped properties
                    map.put(jobModel.getId() + scopeSeparator + key, value);
                }
            });

            stepModel.getWith().forEach((key, value) -> {
                if (haveIds(pipelineModel, jobModel, stepModel)) {
                    // Add pipeline / job / step scoped properties
                    map.put(
                            pipelineModel.getId()
                                    + scopeSeparator
                                    + jobModel.getId()
                                    + scopeSeparator
                                    + stepModel.getId()
                                    + scopeSeparator
                                    + key,
                            value);
                }

                if (haveIds(jobModel, stepModel)) {
                    // Added job / step scoped properties
                    map.put(jobModel.getId() + scopeSeparator + stepModel.getId() + scopeSeparator + key, value);
                }

                if (stepModel.getId() != null) {
                    // Add step scoped properties
                    map.put(stepModel.getId() + scopeSeparator + key, value);
                }
            });
        }

        // Add context properties
        map.putAll(getContext().getWith());

        return map;
    }

    /**
     * Method to store a captured property in the Context
     *
     * @param key key
     * @param value value
     * @param captureType captureType
     */
    private void storeCaptureProperty(String key, String value, CaptureType captureType) {
        Map<String, String> properties = getContext().getWith();

        if (captureType == CaptureType.OVERWRITE) {
            // Overwrite the captured property
            properties.put(key, value);

            if (haveIds(pipelineModel, jobModel, stepModel)) {
                // Overwrite pipeline / job / step scoped properties
                properties.put(
                        pipelineModel.getId() + "." + jobModel.getId() + "." + stepModel.getId() + "." + key, value);
            }

            if (haveIds(jobModel, stepModel)) {
                // Overwrite job / step scoped properties
                properties.put(jobModel.getId() + "." + stepModel.getId() + "." + key, value);
            }

            if (haveIds(stepModel)) {
                // Overwrite step scoped properties
                properties.put(stepModel.getId() + "." + key, value);
            }
        } else if (captureType == CaptureType.APPEND) {
            // Append the captured property value to the existing value
            String newValue = properties.getOrDefault(key, "") + value;

            // Append the captured property
            properties.put(key, newValue);

            if (haveIds(pipelineModel, jobModel, stepModel)) {
                // Append pipeline / job / step scoped properties
                properties.put(
                        pipelineModel.getId() + "." + jobModel.getId() + "." + stepModel.getId() + "." + key, newValue);
            }

            if (haveIds(jobModel, stepModel)) {
                // Append job / step scoped properties
                properties.put(jobModel.getId() + "." + stepModel.getId() + "." + key, newValue);
            }

            if (haveIds(stepModel)) {
                // Append step scoped properties
                properties.put(stepModel.getId() + "." + key, newValue);
            }
        }
    }

    /**
     * Method to get the working directory
     *
     * @param environmentVariables environmentVariables
     * @param properties properties
     * @return the working directory
     */
    private String getWorkingDirectory(Map<String, String> environmentVariables, Map<String, String> properties) {
        // Resolve the working directory
        String workingDirectory = stepModel.getWorkingDirectory();
        if (workingDirectory == null) {
            workingDirectory = jobModel.getWorkingDirectory();
            if (workingDirectory == null) {
                workingDirectory = pipelineModel.getWorkingDirectory();
                if (workingDirectory == null) {
                    workingDirectory = ".";
                }
            }
        }

        // Replace properties in the working directory
        workingDirectory = Resolver.replaceProperties(properties, workingDirectory);

        // Replace environment variables in the working directory
        workingDirectory = Resolver.replaceEnvironmentVariables(environmentVariables, workingDirectory);

        return workingDirectory;
    }

    /**
     * Method to get the timeout minutes
     *
     * @return the timeout minutes
     */
    private int getTimeoutMinutes() {
        // Resolve the timeout minutes
        String timeoutMinutes = stepModel.getTimeoutMinutes();
        if (timeoutMinutes == null) {
            timeoutMinutes = jobModel.getTimeoutMinutes();
            if (timeoutMinutes == null) {
                timeoutMinutes = pipelineModel.getTimeoutMinutes();
                if (timeoutMinutes == null) {
                    timeoutMinutes = String.valueOf(Integer.MAX_VALUE);
                }
            }
        }

        return Integer.parseInt(timeoutMinutes);
    }

    /**
     * Method to get the CaptureType
     *
     * @param command command
     * @return the CaptureType
     */
    private CaptureType getCaptureType(String command) {
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
     * @param captureType captureType
     * @param command command
     * @return the capture property
     */
    private String getCaptureProperty(CaptureType captureType, String command) {
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

    /**
     * Method to return if all Models have ids
     *
     * @param models models
     * @return true of all models have ids, else false
     */
    private static boolean haveIds(Model... models) {
        for (Model model : models) {
            if (model.getId() == null) {
                return false;
            }
        }

        return true;
    }
}
