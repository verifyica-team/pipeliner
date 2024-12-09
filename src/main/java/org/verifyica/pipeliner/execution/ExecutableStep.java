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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.RecursiveReplacer;
import org.verifyica.pipeliner.common.Version;
import org.verifyica.pipeliner.execution.support.CaptureType;
import org.verifyica.pipeliner.execution.support.ProcessExecutor;
import org.verifyica.pipeliner.execution.support.Shell;
import org.verifyica.pipeliner.execution.support.Status;
import org.verifyica.pipeliner.model.Job;
import org.verifyica.pipeliner.model.Pipeline;
import org.verifyica.pipeliner.model.Step;

/** Class to implement ExecutableStep */
public class ExecutableStep extends Executable {

    private static final String PROPERTY_MATCHING_REGEX = "(?<!\\\\)\\$\\{\\{\\s*([a-zA-Z0-9_\\-.]+)\\s*\\}\\}";

    private static final String CAPTURE_APPEND_MATCHING_REGEX = ".*>>\\s*\\$[A-Za-z0-9][A-Za-z0-9\\-._]*$";

    private static final String CAPTURE_OVERWRITE_MATCHING_REGEX = ".*>\\s*\\$[A-Za-z0-9][A-Za-z0-9\\-._]*$";

    private final Step step;
    private Console console;

    /**
     * Constructor
     *
     * @param step step
     */
    public ExecutableStep(Step step) {
        this.step = step;
        this.console = Console.getInstance();
    }

    @Override
    public void execute() {
        if (decodeEnabled(step.getEnabled())) {
            getStopwatch().reset();

            console.log("%s status=[%s]", step, Status.RUNNING);

            run();

            Status status = getExitCode() == 0 ? Status.SUCCESS : Status.FAILURE;

            console.log(
                    "%s status=[%s] exit-code=[%d] ms=[%d]",
                    step, status, getExitCode(), getStopwatch().elapsedTime().toMillis());
        } else {
            skip(Status.DISABLED);
        }
    }

    @Override
    public void skip(Status status) {
        console.log("%s status=[%s]", step, status);
    }

    /**
     * Method to run
     */
    private void run() {
        Job job = (Job) step.getParent();
        Pipeline pipeline = (Pipeline) job.getParent();

        String run = step.getRun();

        List<String> commands = mergeLines(Arrays.asList(run.split("\\R")));

        for (String command : commands) {
            Map<String, String> resolvedWith = new TreeMap<>();

            mergeWithPrefix(pipeline.getWith(), pipeline.getId() + ".", resolvedWith);
            mergeWithPrefix(job.getWith(), job.getId() + ".", resolvedWith);
            mergeWithPrefix(step.getWith(), step.getId() + ".", resolvedWith);

            resolvedWith.putAll(pipeline.getWith());
            resolvedWith.putAll(job.getWith());
            resolvedWith.putAll(step.getWith());

            // Legacy "with" names
            Map<String, String> inputMap = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : resolvedWith.entrySet()) {
                inputMap.put("INPUT_" + entry.getKey(), entry.getValue());
            }
            resolvedWith.putAll(inputMap);

            Map<String, String> resolvedEnv = new TreeMap<>();

            resolvedEnv.putAll(System.getenv());
            resolvedEnv.putAll(pipeline.getEnv());
            resolvedEnv.putAll(job.getEnv());
            resolvedEnv.putAll(step.getEnv());

            resolvedEnv.put("PIPELINER_VERSION", Version.getVersion());
            resolvedEnv.put("INPUT_PIPELINER_VERSION", Version.getVersion());

            Map<String, String> resolvedOpt = new TreeMap<>();

            resolvedOpt.putAll(pipeline.getOpt());
            resolvedOpt.putAll(job.getOpt());
            resolvedOpt.putAll(step.getOpt());

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

            String resolvedWorkingDirectory =
                    RecursiveReplacer.replace(resolvedWith, PROPERTY_MATCHING_REGEX, workingDirectory);

            resolvedEnv.forEach((name, value) -> console.trace("%s env [%s] = [%s]", step, name, value));
            resolvedWith.forEach((name, value) -> console.trace("%s with [%s] = [%s]", step, name, value));
            resolvedOpt.forEach((name, value) -> console.trace("%s opt [%s] = [%s]", step, name, value));

            console.trace("%s working directory [%s]", step, resolvedWorkingDirectory);

            Shell shell = Shell.decode(step.getShell());
            String resolvedCommand = RecursiveReplacer.replace(resolvedWith, PROPERTY_MATCHING_REGEX, command);
            CaptureType captureType = getCaptureType(resolvedCommand);
            String captureVariable = getCaptureProperty(resolvedCommand, captureType);
            String processExecutorCommand = buildProcessExecutorCommand(resolvedCommand, captureType, resolvedWith);

            console.trace("%s shell [%s]", step, shell);
            console.trace("%s capture type [%s]", step, captureType);
            console.trace("%s capture variable [%s]", step, captureVariable);
            console.trace("%s process executor command [%s]", step, processExecutorCommand);

            if ("mask".equals(resolvedOpt.get("properties"))) {
                console.log("$ %s", command);
            } else {
                console.log("$ %s", resolvedCommand);
            }

            ProcessExecutor processExecutor = new ProcessExecutor(
                    resolvedEnv, resolvedWorkingDirectory, shell, processExecutorCommand, captureType);
            processExecutor.execute();

            if (captureType != CaptureType.NONE) {
                String output = processExecutor.getOutput();
                if (captureType == CaptureType.OVERWRITE) {
                    pipeline.getWith().put(captureVariable, output);
                    pipeline.getWith().put(step.getId() + "." + captureVariable, output);

                    pipeline.getWith().put("INPUT_" + captureVariable, output);
                    pipeline.getWith().put("INPUT_" + step.getId() + "." + captureVariable, output);
                } else {
                    String value = pipeline.getWith().getOrDefault(captureVariable, "");
                    value = value + output;
                    pipeline.getWith().put(captureVariable, value);
                    pipeline.getWith().put(step.getId() + "." + captureVariable, value);

                    value = pipeline.getWith().getOrDefault("INPUT_" + captureVariable, "");
                    pipeline.getWith().put("INPUT_" + captureVariable, value);
                    pipeline.getWith().put("INPUT_" + step.getId() + "." + captureVariable, value);
                }
            }

            setExitCode(processExecutor.getExitCode());

            if (getExitCode() != 0) {
                break;
            }
        }
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
     * Method to build the ProcessExecutor command
     *
     * @param command command
     * @param captureType captureType
     * @param properties properties
     * @return the ProcessExecutor command
     */
    private String buildProcessExecutorCommand(
            String command, CaptureType captureType, Map<String, String> properties) {
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

        return RecursiveReplacer.replace(properties, PROPERTY_MATCHING_REGEX, processExecutorCommand);
    }

    /**
     * Method to merge with Maps with a prefix
     *
     * @param source source
     * @param prefix prefix
     * @param target target
     */
    private static void mergeWithPrefix(Map<String, String> source, String prefix, Map<String, String> target) {
        if (source != null && prefix != null) {
            for (Map.Entry<String, String> entry : source.entrySet()) {
                target.put(prefix + entry.getKey(), entry.getValue());
            }
        }
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
