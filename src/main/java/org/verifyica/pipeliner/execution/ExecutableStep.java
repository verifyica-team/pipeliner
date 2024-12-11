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
import org.verifyica.pipeliner.Version;
import org.verifyica.pipeliner.execution.support.CaptureType;
import org.verifyica.pipeliner.execution.support.Constants;
import org.verifyica.pipeliner.execution.support.ProcessExecutor;
import org.verifyica.pipeliner.execution.support.Shell;
import org.verifyica.pipeliner.execution.support.Status;
import org.verifyica.pipeliner.model.Job;
import org.verifyica.pipeliner.model.Pipeline;
import org.verifyica.pipeliner.model.Step;
import org.verifyica.pipeliner.model.support.Enabled;

/** Class to implement ExecutableStep */
public class ExecutableStep extends Executable {

    private static final String PROPERTY_MATCHING_REGEX = "(?<!\\\\)\\$\\{\\{\\s*([a-zA-Z0-9_\\-.]+)\\s*\\}\\}";

    private static final String CAPTURE_APPEND_MATCHING_REGEX = ".*>>\\s*\\$[A-Za-z0-9][A-Za-z0-9\\-._]*$";

    private static final String CAPTURE_OVERWRITE_MATCHING_REGEX = ".*>\\s*\\$[A-Za-z0-9][A-Za-z0-9\\-._]*$";

    private Pipeline pipeline;
    private Job job;
    private final Step step;

    /**
     * Constructor
     *
     * @param step step
     */
    public ExecutableStep(Step step) {
        this.step = step;
    }

    @Override
    public void execute(ExecutableContext executableContext) {
        if (Boolean.TRUE.equals(Enabled.decodeEnabled(step.getEnabled()))) {
            getStopwatch().reset();

            executableContext.getConsole().log("%s status=[%s]", step, Status.RUNNING);

            run(executableContext);

            Status status = getExitCode() == 0 ? Status.SUCCESS : Status.FAILURE;

            executableContext
                    .getConsole()
                    .log(
                            "%s status=[%s] exit-code=[%d] ms=[%d]",
                            step,
                            status,
                            getExitCode(),
                            getStopwatch().elapsedTime().toMillis());
        } else {
            skip(executableContext, Status.DISABLED);
        }
    }

    @Override
    public void skip(ExecutableContext executableContext, Status status) {
        executableContext.getConsole().log("%s status=[%s]", step, status);
    }

    /**
     * Method to run
     */
    private void run(ExecutableContext executableContext) {
        job = (Job) step.getParent();
        pipeline = (Pipeline) job.getParent();

        String run = step.getRun();

        List<String> commands = mergeLines(Arrays.asList(run.split("\\R")));

        for (String command : commands) {
            Map<String, String> mergedEnvironmentVariables = getMergedEnvironmentVariables();
            Map<String, String> mergedProperties = getMergedProperties(executableContext);

            String workingDirectory = getWorkingDirectory();
            String resolvedWorkingDirectory = resolveProperties(mergedProperties, workingDirectory);

            Shell shell = Shell.decode(step.getShell());
            String resolvedCommand = resolveProperties(mergedProperties, command);
            CaptureType captureType = getCaptureType(resolvedCommand);
            String captureProperty = getCaptureProperty(resolvedCommand, captureType);
            String processExecutorCommand = getProcessExecutorCommand(resolvedCommand, captureType);

            if (executableContext.getConsole().isTraceEnabled()) {
                mergedEnvironmentVariables.forEach((key, value) ->
                        executableContext.getConsole().trace("environment variable [%s] = [%s]", key, value));
                mergedProperties.forEach(
                        (key, value) -> executableContext.getConsole().trace("property [%s] = [%s]", key, value));
                executableContext.getConsole().trace("%s working directory [%s]", step, resolvedWorkingDirectory);
                executableContext.getConsole().trace("%s shell [%s]", step, shell);
                executableContext.getConsole().trace("%s capture type [%s]", step, captureType);
                executableContext.getConsole().trace("%s capture variable [%s]", step, captureProperty);
                executableContext.getConsole().trace("%s process executor command [%s]", step, processExecutorCommand);
            }

            if (Constants.MASK.equals(mergedProperties.get(Constants.PIPELINER_PROPERTIES))) {
                executableContext.getConsole().log("$ %s", command);
            } else {
                executableContext.getConsole().log("$ %s", resolvedCommand);
            }

            Matcher matcher = Pattern.compile(PROPERTY_MATCHING_REGEX).matcher(processExecutorCommand);
            if (matcher.find()) {
                executableContext.getConsole().error("%s references unresolved property [%s]", step, matcher.group());
                setExitCode(1);
                return;
            }

            ProcessExecutor processExecutor = new ProcessExecutor(
                    mergedEnvironmentVariables, resolvedWorkingDirectory, shell, processExecutorCommand, captureType);
            processExecutor.execute();

            if (captureType != CaptureType.NONE) {
                String processOutput = processExecutor.getProcessOutput();
                captureProperty(captureProperty, processOutput, captureType, executableContext);
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
    private Map<String, String> getMergedEnvironmentVariables() {
        Map<String, String> map = new TreeMap<>();

        map.putAll(System.getenv());
        map.putAll(pipeline.getEnv());
        map.putAll(job.getEnv());
        map.putAll(step.getEnv());
        map.put("PIPELINER_VERSION", Version.getVersion());

        return map;
    }

    /**
     * Method to get a Map of merge properties
     *
     * @return a Map of merged properties
     */
    private Map<String, String> getMergedProperties(ExecutableContext executableContext) {
        Map<String, String> map = new TreeMap<>();

        // No scope

        map.putAll(pipeline.getWith());
        map.putAll(job.getWith());
        map.putAll(step.getWith());

        // Scoped

        pipeline.getWith().forEach((key, value) -> map.put(pipeline.getId() + "." + key, value));

        job.getWith().forEach((key, value) -> {
            map.put(pipeline.getId() + "." + job.getId() + "." + key, value);
            map.put(job.getId() + "." + key, value);
        });

        step.getWith().forEach((key, value) -> {
            map.put(pipeline.getId() + "." + job.getId() + "." + step.getId() + "." + key, value);
            map.put(job.getId() + "." + step.getId() + "." + key, value);
            map.put(step.getId() + "." + key, value);
        });

        map.putAll(executableContext.getWith());

        return map;
    }

    private void captureProperty(
            String key, String value, CaptureType captureType, ExecutableContext executableContext) {
        Map<String, String> with = executableContext.getWith();

        if (captureType == CaptureType.OVERWRITE) {
            with.put(key, value);
            with.put(step.getId() + "." + key, value);
            with.put(job.getId() + "." + step.getId() + "." + key, value);
            with.put(pipeline.getId() + "." + job.getId() + "." + step.getId() + "." + key, value);
        } else {
            String newValue = with.getOrDefault(key, "") + value;
            with.put(key, newValue);
            with.put(step.getId() + "." + key, newValue);
            with.put(job.getId() + "." + step.getId() + "." + key, newValue);
            with.put(pipeline.getId() + "." + job.getId() + "." + step.getId() + "." + key, newValue);
        }
    }

    private String resolveProperties(Map<String, String> map, String string) {
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

    private String getWorkingDirectory() {
        String workingDirectory = step.getWorkingDirectory();

        if (workingDirectory == null) {
            workingDirectory = job.getWorkingDirectory();
            if (workingDirectory == null) {
                workingDirectory = pipeline.getWorkingDirectory();
                if (workingDirectory == null) {
                    workingDirectory = ".";
                }
            }
        }

        return workingDirectory;
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
}
