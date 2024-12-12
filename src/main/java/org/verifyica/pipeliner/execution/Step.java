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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.verifyica.pipeliner.execution.support.CaptureType;
import org.verifyica.pipeliner.execution.support.Constants;
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
        jobModel = (JobModel) stepModel.getParent();
        pipelineModel = (PipelineModel) jobModel.getParent();
        run = stepModel.getRun();

        if (Boolean.TRUE.equals(Enabled.decode(stepModel.getEnabled()))) {
            getStopwatch().reset();

            context.getConsole().log("%s status=[%s]", stepModel, Status.RUNNING);

            run(context);

            Status status = getExitCode() == 0 ? Status.SUCCESS : Status.FAILURE;

            context.getConsole()
                    .log(
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
        context.getConsole().log("%s status=[%s]", stepModel, status);
    }

    /**
     * Method to run
     *
     * @param context context
     */
    private void run(Context context) {
        List<String> commands = mergeLines(Arrays.asList(run.split("\\R")));
        for (String command : commands) {
            Map<String, String> environmentVariables = getEnvironVariables();
            Map<String, String> properties = getProperties(context);
            String workingDirectory = getWorkingDirectory(properties);
            Shell shell = Shell.decode(stepModel.getShell());
            String resolvedCommand = resolveProperty(properties, command);
            CaptureType captureType = getCaptureType(resolvedCommand);
            String captureProperty = getCaptureProperty(resolvedCommand, captureType);
            String processExecutorCommand = getProcessExecutorCommand(resolvedCommand, captureType);

            if (context.getConsole().isTraceEnabled()) {
                environmentVariables.forEach(
                        (key, value) -> context.getConsole().trace("environment variable [%s] = [%s]", key, value));
                properties.forEach((key, value) -> context.getConsole().trace("property [%s] = [%s]", key, value));
                context.getConsole().trace("%s working directory [%s]", stepModel, workingDirectory);
                context.getConsole().trace("%s shell [%s]", stepModel, shell);
                context.getConsole().trace("%s capture type [%s]", stepModel, captureType);
                context.getConsole().trace("%s capture variable [%s]", stepModel, captureProperty);
                context.getConsole().trace("%s process executor command [%s]", stepModel, processExecutorCommand);
            }

            if (Constants.MASK.equals(properties.get(Constants.PIPELINER_PROPERTIES))) {
                context.getConsole().log("$ %s", command);
            } else {
                context.getConsole().log("$ %s", resolvedCommand);
            }

            Matcher matcher = Pattern.compile(PROPERTY_MATCHING_REGEX).matcher(processExecutorCommand);
            if (matcher.find()) {
                context.getConsole().error("%s references unresolved property [%s]", stepModel, matcher.group());
                setExitCode(1);
                return;
            }

            ProcessExecutor processExecutor = new ProcessExecutor(
                    environmentVariables, workingDirectory, shell, processExecutorCommand, captureType);
            processExecutor.execute();

            if (captureType != CaptureType.NONE) {
                String processOutput = processExecutor.getProcessOutput();
                captureProperty(captureProperty, processOutput, captureType, context);
            }

            setExitCode(processExecutor.getExitCode());

            if (getExitCode() != 0) {
                break;
            }
        }
    }

    /**
     * Method to get a Map of merged environment variables
     *
     * @return a Map of merged environment variables
     */
    private Map<String, String> getEnvironVariables() {
        Map<String, String> map = new TreeMap<>();

        map.putAll(System.getenv());
        map.putAll(pipelineModel.getEnv());
        map.putAll(jobModel.getEnv());
        map.putAll(stepModel.getEnv());

        return map;
    }

    /**
     * Method to get a Map of merge properties
     *
     * @return a Map of merged properties
     */
    private Map<String, String> getProperties(Context context) {
        Map<String, String> map = new TreeMap<>();

        // No scope

        map.putAll(pipelineModel.getWith());
        map.putAll(jobModel.getWith());
        map.putAll(stepModel.getWith());

        // Scoped

        if (pipelineModel.getId() != null) {
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

        map.putAll(context.getWith());

        return map;
    }

    /**
     * Method to capture a property and store it in the Context
     *
     * @param key key
     * @param value value
     * @param captureType captureType
     * @param context content
     */
    private void captureProperty(String key, String value, CaptureType captureType, Context context) {
        Map<String, String> with = context.getWith();

        if (captureType == CaptureType.OVERWRITE) {
            with.put(key, value);
            with.put(stepModel.getId() + "." + key, value);
            with.put(jobModel.getId() + "." + stepModel.getId() + "." + key, value);
            with.put(pipelineModel.getId() + "." + jobModel.getId() + "." + stepModel.getId() + "." + key, value);
        } else {
            String newValue = with.getOrDefault(key, "") + value;
            with.put(key, newValue);
            with.put(stepModel.getId() + "." + key, newValue);
            with.put(jobModel.getId() + "." + stepModel.getId() + "." + key, newValue);
            with.put(pipelineModel.getId() + "." + jobModel.getId() + "." + stepModel.getId() + "." + key, newValue);
        }
    }

    /**
     * Method to resolve a property
     *
     * @param map map
     * @param string string
     * @return the string with properties resolved
     */
    private String resolveProperty(Map<String, String> map, String string) {
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
                String value = map.get(key);

                if (value == null) {
                    value = matcher.group(0);
                }

                matcher.appendReplacement(result, Matcher.quoteReplacement(value));
            }

            matcher.appendTail(result);
            resolvedString = result.toString();

        } while (!resolvedString.equals(previous));

        return resolvedString;
    }

    /**
     * Method to resolve properties in a map
     *
     * @param map map
     * @return the map with properties resolved
     */
    private Map<String, String> resolveProperties(Map<String, String> map) {
        Map<String, String> resolvedMap = new TreeMap<>();

        map.forEach((key, value) -> resolvedMap.put(key, resolveProperty(map, value)));

        return resolvedMap;
    }

    /**
     * Method to resolve the working directory
     *
     * @return the working directory
     */
    private String getWorkingDirectory(Map<String, String> map) {
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

        return resolveProperty(map, workingDirectory);
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
