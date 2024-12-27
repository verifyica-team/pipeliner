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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.Environment;
import org.verifyica.pipeliner.common.Ipc;
import org.verifyica.pipeliner.execution.support.CaptureType;
import org.verifyica.pipeliner.execution.support.ProcessExecutor;
import org.verifyica.pipeliner.execution.support.Resolver;
import org.verifyica.pipeliner.execution.support.Shell;
import org.verifyica.pipeliner.execution.support.Status;
import org.verifyica.pipeliner.model.JobModel;
import org.verifyica.pipeliner.model.Model;
import org.verifyica.pipeliner.model.PipelineModel;
import org.verifyica.pipeliner.model.StepModel;
import org.verifyica.pipeliner.model.support.Enabled;

/** Class to implement Step */
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class Step extends Executable {

    private static final String CAPTURE_APPEND_MATCHING_REGEX = ".*>>\\s*\\$[A-Za-z0-9][A-Za-z0-9\\-._]*$";

    private static final String CAPTURE_OVERWRITE_MATCHING_REGEX = ".*>\\s*\\$[A-Za-z0-9][A-Za-z0-9\\-._]*$";

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
        prepare(context);

        if (Boolean.TRUE.equals(Enabled.decode(stepModel.getEnabled()))) {
            getConsole().info("%s status=[%s]", stepModel, status);
        } else {
            getConsole().info("%s status=[%s]", stepModel, Status.DISABLED);
        }
    }

    /**
     * Method to run the step
     */
    private void run() {
        Console console = getConsole();
        boolean isTraceEnabled = console.isTraceEnabled();

        File ipcOutputFile = null;
        File ipcInputFile = null;

        try {
            List<String> commands = mergeLines(Arrays.asList(run.split("\\R")));
            for (String command : commands) {
                if (isTraceEnabled) {
                    console.trace("%s command [%s]", stepModel, command);
                }

                // Decode the shell
                Shell shell = Shell.decode(stepModel.getShell());

                if (isTraceEnabled) {
                    console.trace("%s shell [%s]", stepModel, shell);
                }

                // Get the timeout minutes
                int timeoutMinutes = getTimeoutMinutes();

                if (isTraceEnabled) {
                    console.trace("%s timeout minutes [%d]", stepModel, timeoutMinutes);
                }

                // Get the capture type
                CaptureType captureType = getCaptureType(command);

                if (isTraceEnabled) {
                    console.trace("%s capture type [%s]", stepModel, captureType);
                }

                // Get the capture property
                String captureProperty = getCaptureProperty(command, captureType);

                if (isTraceEnabled) {
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

                if (isTraceEnabled) {
                    console.trace("%s command without capture property [%s]", stepModel, commandWithoutCaptureProperty);
                }

                Map<String, String> environmentVariables = getMergedEnvironmentVariables();
                Map<String, String> properties = getMergedProperties();

                Map<String, String> resolvedEnvironmentVariables =
                        Resolver.resolveEnvironmentVariables(environmentVariables, properties);

                String commandWithPropertiesResolved =
                        Resolver.resolveProperties(properties, commandWithoutCaptureProperty);

                if (isTraceEnabled) {
                    console.trace("%s command with properties resolved [%s]", stepModel, commandWithPropertiesResolved);
                }

                // Get resolved working directory
                String workingDirectory = Resolver.resolveProperties(properties, getWorkingDirectory());

                if (isTraceEnabled) {
                    console.trace("%s working directory [%s]", stepModel, workingDirectory);
                }

                if (Constants.TRUE.equals(properties.get(Constants.PIPELINER_MASK_PROPERTIES))) {
                    console.info("$ %s", command);
                } else {
                    console.info("$ %s", commandWithPropertiesResolved);
                }

                if (isTraceEnabled) {
                    console.trace("%s Ipc creating files ...", stepModel);
                }

                ipcOutputFile = Ipc.createIpcFile();
                ipcInputFile = Ipc.createIpcFile();

                if (isTraceEnabled) {
                    console.trace(
                            "%s Ipc file [%s] = [%s]",
                            stepModel, Constants.PIPELINER_IPC_OUT, ipcOutputFile.getAbsolutePath());
                    console.trace(
                            "%s Ipc file [%s] = [%s]",
                            stepModel, Constants.PIPELINER_IPC_IN, ipcInputFile.getAbsolutePath());
                }

                if (isTraceEnabled) {
                    console.trace("%s Ipc write [%s]", stepModel, ipcOutputFile);
                }

                Ipc.write(ipcOutputFile, properties);

                resolvedEnvironmentVariables.put(Constants.PIPELINER_IPC_IN, ipcOutputFile.getAbsolutePath());
                resolvedEnvironmentVariables.put(Constants.PIPELINER_IPC_OUT, ipcInputFile.getAbsolutePath());
                resolvedEnvironmentVariables.put(Constants.PIPELINER_IPC, ipcInputFile.getAbsolutePath());

                ProcessExecutor processExecutor;

                if (command.startsWith(Constants.PIPELINER_EXTENSION_DIRECTIVE_COMMAND_PREFIX)) {
                    String[] tokens = commandWithPropertiesResolved.split("\\s+");

                    if (tokens.length < 2 || tokens.length > 3) {
                        throw new IllegalArgumentException(format("invalid --extension directive [%s]", command));
                    }

                    String url = Resolver.resolveEnvironmentVariablesAndProperties(
                            resolvedEnvironmentVariables, properties, tokens[1]);

                    if (isTraceEnabled) {
                        console.trace("%s extension url [%s]", stepModel, url);
                    }

                    String sha256Checksum = null;

                    if (tokens.length == 3) {
                        sha256Checksum = Resolver.resolveEnvironmentVariablesAndProperties(
                                resolvedEnvironmentVariables, properties, tokens[2]);
                    }

                    if (isTraceEnabled) {
                        console.trace("%s extension sha256Checksum [%s]", stepModel, sha256Checksum);
                    }

                    String extensionCommand = ExtensionManager.getInstance()
                            .getExtensionShellScript(resolvedEnvironmentVariables, properties, url, sha256Checksum)
                            .toString();

                    if (isTraceEnabled) {
                        console.trace("%s extension command [%s]", stepModel, extensionCommand);
                    }

                    processExecutor = new ProcessExecutor(
                            console,
                            stepModel,
                            resolvedEnvironmentVariables,
                            workingDirectory,
                            shell,
                            extensionCommand,
                            captureType);
                } else if (command.startsWith(Constants.PIPELINER_DIRECTIVE_COMMAND_PREFIX)) {
                    throw new IllegalArgumentException(format("invalid directive [%s]", commandWithPropertiesResolved));
                } else {
                    processExecutor = new ProcessExecutor(
                            console,
                            stepModel,
                            resolvedEnvironmentVariables,
                            workingDirectory,
                            shell,
                            commandWithPropertiesResolved,
                            captureType);
                }

                processExecutor.execute(timeoutMinutes);
                setExitCode(processExecutor.getExitCode());

                if (captureType != CaptureType.NONE) {
                    String processOutput = processExecutor.getProcessOutput();
                    storeCaptureProperty(captureProperty, processOutput, captureType);
                }

                if (isTraceEnabled) {
                    console.trace("%s Ipc read [%s]", stepModel, ipcInputFile);
                }

                Map<String, String> map = Ipc.read(ipcInputFile);

                map.forEach((property, value) -> {
                    if (isTraceEnabled) {
                        console.trace("%s Ipc capture property [%s] = [%s]", stepModel, property, value);
                    }
                    storeCaptureProperty(property, value, CaptureType.OVERWRITE);
                });
            }
        } catch (Throwable t) {
            Ipc.cleanup(ipcInputFile);
            Ipc.cleanup(ipcOutputFile);

            if (console.isTraceEnabled()) {
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
    private Map<String, String> getMergedEnvironmentVariables() {
        Map<String, String> map = new TreeMap<>();

        map.putAll(Environment.getenv());
        map.putAll(pipelineModel.getEnv());
        map.putAll(jobModel.getEnv());
        map.putAll(stepModel.getEnv());

        if (getConsole().isTraceEnabled()) {
            map.put(Constants.PIPELINER_TRACE, Constants.TRUE);
        }

        return map;
    }

    /**
     * Method to get a Map of merge properties
     *
     * @return a Map of merged properties
     */
    private Map<String, String> getMergedProperties() {
        Map<String, String> map = new TreeMap<>();

        // No scope

        map.putAll(pipelineModel.getWith());
        map.putAll(jobModel.getWith());
        map.putAll(stepModel.getWith());

        // Scoped

        if (haveIds(pipelineModel)) {
            pipelineModel.getWith().forEach((key, value) -> map.put(pipelineModel.getId() + "." + key, value));
        }

        jobModel.getWith().forEach((key, value) -> {
            if (haveIds(pipelineModel, jobModel)) {
                map.put(pipelineModel.getId() + "." + jobModel.getId() + "." + key, value);
            }

            if (jobModel.getId() != null) {
                map.put(jobModel.getId() + "." + key, value);
            }
        });

        stepModel.getWith().forEach((key, value) -> {
            if (haveIds(pipelineModel, jobModel, stepModel)) {
                map.put(pipelineModel.getId() + "." + jobModel.getId() + "." + stepModel.getId() + "." + key, value);
            }

            if (haveIds(jobModel, stepModel)) {
                map.put(jobModel.getId() + "." + stepModel.getId() + "." + key, value);
            }

            if (stepModel.getId() != null) {
                map.put(stepModel.getId() + "." + key, value);
            }
        });

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
            properties.put(key, value);

            if (haveIds(pipelineModel, jobModel, stepModel)) {
                properties.put(
                        pipelineModel.getId() + "." + jobModel.getId() + "." + stepModel.getId() + "." + key, value);
            }

            if (haveIds(jobModel, stepModel)) {
                properties.put(jobModel.getId() + "." + stepModel.getId() + "." + key, value);
            }

            if (haveIds(stepModel)) {
                properties.put(stepModel.getId() + "." + key, value);
            }
        } else {
            String newValue = properties.getOrDefault(key, "") + value;
            properties.put(key, newValue);

            if (haveIds(pipelineModel, jobModel, stepModel)) {
                properties.put(
                        pipelineModel.getId() + "." + jobModel.getId() + "." + stepModel.getId() + "." + key, newValue);
            }

            if (haveIds(jobModel, stepModel)) {
                properties.put(jobModel.getId() + "." + stepModel.getId() + "." + key, newValue);
            }

            if (haveIds(stepModel)) {
                properties.put(stepModel.getId() + "." + key, newValue);
            }
        }
    }

    /**
     * Method to resolve the working directory
     *
     * @return the working directory
     */
    private String getWorkingDirectory() {
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

        return workingDirectory;
    }

    /**
     * Method to get the timeout minutes
     *
     * @return the timeout minutes
     */
    private int getTimeoutMinutes() {
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
     * @param command command
     * @param captureType captureType
     * @return the capture property
     */
    private String getCaptureProperty(String command, CaptureType captureType) {
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
     * Method to get the ProcessExecutor command
     *
     * @param command command
     * @param captureType captureType
     * @return the ProcessExecutor command
     */
    private String getProcessExecutorCommand(String command, CaptureType captureType) {
        String processExecutorCommand;

        switch (captureType) {
            case APPEND:
                processExecutorCommand =
                        command.substring(0, command.lastIndexOf(">>")).trim();
                break;
            case OVERWRITE: {
                processExecutorCommand =
                        command.substring(0, command.lastIndexOf(">")).trim();
                break;
            }
            case NONE:
            default: {
                processExecutorCommand = command;
            }
        }

        return processExecutorCommand;
    }

    /**
     * Method to merge a list of lines
     *
     * @param lines lines
     * @return a list of merged lines
     */
    private static List<String> mergeLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String str : lines) {
            if (str.endsWith(" \\")) {
                current.append(str.substring(0, str.length() - 2));
            } else {
                if (current.length() > 0) {
                    current.append(" ");
                    current.append(str.trim());
                    result.add(current.toString().trim());
                    current.setLength(0);
                } else {
                    result.add(str);
                }
            }
        }

        if (current.length() > 0) {
            result.add(current.toString());
        }

        return result;
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
