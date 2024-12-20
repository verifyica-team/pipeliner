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
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.verifyica.pipeliner.common.Ipc;
import org.verifyica.pipeliner.common.Sha256ChecksumException;
import org.verifyica.pipeliner.execution.support.CaptureType;
import org.verifyica.pipeliner.execution.support.Constants;
import org.verifyica.pipeliner.execution.support.ExtensionManager;
import org.verifyica.pipeliner.execution.support.ProcessExecutor;
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

    private static final String PROPERTY_MATCHING_REGEX = "(?<!\\\\)\\$\\{\\{\\s*([a-zA-Z0-9_\\-.]+)\\s*\\}\\}";

    private static final String CAPTURE_APPEND_MATCHING_REGEX = ".*>>\\s*\\$[A-Za-z0-9][A-Za-z0-9\\-._]*$";

    private static final String CAPTURE_OVERWRITE_MATCHING_REGEX = ".*>\\s*\\$[A-Za-z0-9][A-Za-z0-9\\-._]*$";

    private static final Set<PosixFilePermission> PERMISSIONS = PosixFilePermissions.fromString("rwx------");

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
        File ipcOutputFile = null;
        File ipcInputFile = null;

        try {
            List<String> commandLines = mergeLines(Arrays.asList(run.split("\\R")));
            for (String commandLine : commandLines) {
                Map<String, String> properties = getProperties();
                Map<String, String> environmentVariables = getEnvironmentVariables(properties);
                String workingDirectory = getWorkingDirectory(environmentVariables, properties);
                Shell shell = Shell.decode(stepModel.getShell());
                String resolvedCommandLine = resolveProperty(environmentVariables, properties, commandLine);
                CaptureType captureType = getCaptureType(resolvedCommandLine);
                String captureProperty = getCaptureProperty(resolvedCommandLine, captureType);
                String processExecutorCommandLine = getProcessExecutorCommand(resolvedCommandLine, captureType);
                int timeoutMinutes = getTimeoutMinutes();

                if (getConsole().isTraceEnabled()) {
                    environmentVariables.forEach(
                            (key, value) -> getConsole().trace("environment variable [%s] = [%s]", key, value));
                    properties.forEach((key, value) -> getConsole().trace("property [%s] = [%s]", key, value));
                    getConsole().trace("%s working directory [%s]", stepModel, workingDirectory);
                    getConsole().trace("%s shell [%s]", stepModel, shell);
                    getConsole().trace("%s capture type [%s]", stepModel, captureType);
                    getConsole().trace("%s capture property [%s]", stepModel, captureProperty);
                    getConsole().trace("%s command [%s]", stepModel, commandLine);
                    getConsole().trace("%s process executor command [%s]", stepModel, processExecutorCommandLine);
                    getConsole().trace("%s process executor timeout minutes [%s]", stepModel, timeoutMinutes);
                }

                if (processExecutorCommandLine.trim().startsWith(Constants.PIPELINER_EXTENSION_PREFIX)) {
                    // Build extension process command line
                    processExecutorCommandLine = buildExtensionProcessCommandLine(
                            processExecutorCommandLine, environmentVariables, properties);
                }

                if (Constants.MASK.equals(properties.get(Constants.PIPELINER_PROPERTIES))) {
                    getConsole().info("$ %s", commandLine);
                } else {
                    getConsole().info("$ %s", resolvedCommandLine);
                }

                Matcher matcher = Pattern.compile(PROPERTY_MATCHING_REGEX).matcher(processExecutorCommandLine);
                if (matcher.find()) {
                    throw new IOException(format("unresolved property [%s]", matcher.group()));
                }

                getConsole().trace("%s Ipc creating files ...", stepModel);

                ipcOutputFile = Ipc.createIpcFile();
                ipcInputFile = Ipc.createIpcFile();

                getConsole()
                        .trace(
                                "%s Ipc file [%s] = [%s]",
                                stepModel, Constants.PIPELINER_IPC_OUT, ipcOutputFile.getAbsolutePath());
                getConsole()
                        .trace(
                                "%s Ipc file [%s] = [%s]",
                                stepModel, Constants.PIPELINER_IPC_IN, ipcInputFile.getAbsolutePath());

                getConsole().trace("%s Ipc write [%s]", stepModel, ipcOutputFile);
                Ipc.write(ipcOutputFile, properties);

                environmentVariables.put(Constants.PIPELINER_IPC_IN, ipcOutputFile.getAbsolutePath());
                environmentVariables.put(Constants.PIPELINER_IPC_OUT, ipcInputFile.getAbsolutePath());
                environmentVariables.put(Constants.PIPELINER_IPC, ipcInputFile.getAbsolutePath());

                ProcessExecutor processExecutor = new ProcessExecutor(
                        getConsole(),
                        stepModel,
                        environmentVariables,
                        workingDirectory,
                        shell,
                        processExecutorCommandLine,
                        captureType);

                processExecutor.execute(timeoutMinutes);
                setExitCode(processExecutor.getExitCode());

                if (captureType != CaptureType.NONE) {
                    String processOutput = processExecutor.getProcessOutput();
                    storeCaptureProperty(captureProperty, processOutput, captureType);
                }

                getConsole().trace("%s Ipc read [%s]", stepModel, ipcInputFile);
                Map<String, String> map = Ipc.read(ipcInputFile);
                map.forEach((property, value) -> {
                    getConsole().trace("%s Ipc capture property [%s] = [%s]", stepModel, property, value);
                    storeCaptureProperty(property, value, CaptureType.OVERWRITE);
                });
            }
        } catch (Throwable t) {
            Ipc.cleanup(ipcInputFile);
            Ipc.cleanup(ipcOutputFile);

            if (getConsole().isTraceEnabled()) {
                t.printStackTrace(System.out);
            }
            getConsole().error("%s -> %s", stepModel, t.getMessage());
            setExitCode(1);
        }
    }

    /**
     * Method to get a Map of merged environment variables
     *
     * @return a Map of merged environment variables
     */
    private Map<String, String> getEnvironmentVariables(Map<String, String> with) {
        Map<String, String> map = new TreeMap<>();

        map.putAll(System.getenv());
        map.putAll(pipelineModel.getEnv());
        map.putAll(jobModel.getEnv());
        map.putAll(stepModel.getEnv());

        if (getConsole().isTraceEnabled()) {
            map.put(Constants.PIPELINER_TRACE, Constants.TRUE);
        }

        map.forEach((key, value) -> map.put(key, resolveProperty(map, with, value)));

        return map;
    }

    /**
     * Method to get a Map of merge properties
     *
     * @return a Map of merged properties
     */
    private Map<String, String> getProperties() {
        Map<String, String> map = new TreeMap<>();

        map.putAll(pipelineModel.getEnv());
        map.putAll(jobModel.getEnv());
        map.putAll(stepModel.getEnv());

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
     * Method to get the extension process command line
     *
     * @param processExecutorCommandLine processExecutorCommandLine
     * @param environmentVariables environmentVariables
     * @param properties properties
     * @return the extension process command line
     * @throws IOException If an error occurs
     * @throws Sha256ChecksumException If the SHA-256 checksum is invalid
     */
    private String buildExtensionProcessCommandLine(
            String processExecutorCommandLine, Map<String, String> environmentVariables, Map<String, String> properties)
            throws IOException, Sha256ChecksumException {
        processExecutorCommandLine = resolveProperty(environmentVariables, properties, processExecutorCommandLine);

        String[] tokens = processExecutorCommandLine.split("\\s+");

        if (tokens.length < 2 || tokens.length > 3) {
            throw new IOException(format("invalid --extension definition [%s]", processExecutorCommandLine));
        }

        String url = environmentVariables.getOrDefault(tokens[1].substring(1), tokens[1]);
        String sha256Checksum = tokens.length == 3 ? tokens[2] : null;

        return ExtensionManager.getInstance()
                .getExtensionShellScript(url, sha256Checksum)
                .toString();
    }

    /**
     * Method to store a captured property in the Context
     *
     * @param key key
     * @param value value
     * @param captureType captureType
     */
    private void storeCaptureProperty(String key, String value, CaptureType captureType) {
        Map<String, String> with = getContext().getWith();

        if (captureType == CaptureType.OVERWRITE) {
            with.put(key, value);

            if (haveIds(pipelineModel, jobModel, stepModel)) {
                with.put(pipelineModel.getId() + "." + jobModel.getId() + "." + stepModel.getId() + "." + key, value);
            }

            if (haveIds(jobModel, stepModel)) {
                with.put(jobModel.getId() + "." + stepModel.getId() + "." + key, value);
            }

            if (haveIds(stepModel)) {
                with.put(stepModel.getId() + "." + key, value);
            }
        } else {
            String newValue = with.getOrDefault(key, "") + value;
            with.put(key, newValue);

            if (haveIds(pipelineModel, jobModel, stepModel)) {
                with.put(
                        pipelineModel.getId() + "." + jobModel.getId() + "." + stepModel.getId() + "." + key, newValue);
            }

            if (haveIds(jobModel, stepModel)) {
                with.put(jobModel.getId() + "." + stepModel.getId() + "." + key, newValue);
            }

            if (haveIds(stepModel)) {
                with.put(stepModel.getId() + "." + key, newValue);
            }
        }
    }

    /**
     * Method to resolve a property
     *
     * @param env env
     * @param with with
     * @param string string
     * @return the string with properties resolved
     */
    private String resolveProperty(Map<String, String> env, Map<String, String> with, String string) {
        if (string == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(PROPERTY_MATCHING_REGEX);
        String resolvedString = string;
        String previous;

        do {
            previous = resolvedString;
            Matcher matcher = pattern.matcher(resolvedString);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String key = matcher.group(1).trim();
                String value = with.get(key);

                if (value == null) {
                    value = env.get(key);
                    if (value == null) {
                        value = matcher.group(0);
                    }
                }

                matcher.appendReplacement(result, Matcher.quoteReplacement(value));
            }

            matcher.appendTail(result);
            resolvedString = result.toString();

        } while (!resolvedString.equals(previous));

        return resolvedString;
    }

    /**
     * Method to resolve the working directory
     *
     * @param env env
     * @param with with
     * @return the working directory
     */
    private String getWorkingDirectory(Map<String, String> env, Map<String, String> with) {
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

        return resolveProperty(env, with, workingDirectory);
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
