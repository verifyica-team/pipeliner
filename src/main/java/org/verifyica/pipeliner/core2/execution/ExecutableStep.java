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

package org.verifyica.pipeliner.core2.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.RecursiveReplacer;
import org.verifyica.pipeliner.common.Version;
import org.verifyica.pipeliner.core2.model.Job;
import org.verifyica.pipeliner.core2.model.Pipeline;
import org.verifyica.pipeliner.core2.model.Step;

public class ExecutableStep extends Executable {

    private final Step step;

    public ExecutableStep(Step step) {
        this.step = step;
    }

    @Override
    public void execute(Console console) {
        getStopwatch().reset();

        console.log("%s", step);

        run(console);

        console.log(
                "%s exit-code=[%d] ms=[%d]",
                step, getExitCode(), getStopwatch().elapsedTime().toMillis());
    }

    private void run(Console console) {
        Job job = (Job) step.getParent();
        Pipeline pipeline = (Pipeline) job.getParent();

        Map<String, String> mergedWith = new TreeMap<>();

        mergeWithPrefix(pipeline.getWith(), pipeline.getId() + ".", mergedWith);
        mergeWithPrefix(job.getWith(), job.getId() + ".", mergedWith);
        mergeWithPrefix(step.getWith(), step.getId() + ".", mergedWith);

        mergedWith.putAll(pipeline.getWith());
        mergedWith.putAll(job.getWith());
        mergedWith.putAll(step.getWith());

        mergeWithPrefix(pipeline.getWith(), "INPUT_", mergedWith);
        mergeWithPrefix(job.getWith(), "INPUT_", mergedWith);
        mergeWithPrefix(step.getWith(), "INPUT_", mergedWith);

        Map<String, String> mergedEnv = new TreeMap<>();

        mergedEnv.putAll(System.getenv());
        mergedEnv.putAll(pipeline.getEnv());
        mergedEnv.putAll(job.getEnv());
        mergedEnv.putAll(step.getEnv());

        mergedEnv.put("PIPELINER_VERSION", Version.getVersion());
        mergedEnv.put("INPUT_PIPELINER_VERSION", Version.getVersion());

        Map<String, String> mergedOpt = new TreeMap<>();

        mergedOpt.putAll(pipeline.getOpt());
        mergedOpt.putAll(job.getOpt());
        mergedOpt.putAll(step.getOpt());

        String PROPERTY_MATCHING_REGEX = "(?<!\\\\)\\$\\{\\{\\s*([a-zA-Z0-9_\\-.]+)\\s*\\}\\}";

        String workingDirectory = step.getWorkingDirectory();
        String mergedWorkingDirectory =
                RecursiveReplacer.replace(mergedWith, PROPERTY_MATCHING_REGEX, workingDirectory);

        mergedEnv.forEach((name, value) -> console.trace("%s env [%s] = [%s]", step, name, value));
        mergedWith.forEach((name, value) -> console.trace("%s with [%s] = [%s]", step, name, value));
        mergedOpt.forEach((name, value) -> console.trace("%s opt [%s] = [%s]", step, name, value));

        console.trace("%s working directory [%s]", step, mergedWorkingDirectory);

        String run = step.getRun();

        List<String> commands = mergeLines(Arrays.asList(run.split("\\R")));

        Iterator<String> commandsIterator = commands.iterator();
        while (commandsIterator.hasNext()) {
            Shell shell = Shell.decode(step.getShell());
            String command = commandsIterator.next();
            String mergedCommand = RecursiveReplacer.replace(mergedWith, PROPERTY_MATCHING_REGEX, command);

            console.trace("%s shell [%s]", step, shell);

            console.log(step);

            if ("mask".equals(mergedOpt.get("properties"))) {
                console.log("$ %s", command);
            } else {
                console.log("$ %s", mergedCommand);
            }

            ProcessExecutor processExecutor =
                    new ProcessExecutor(mergedEnv, workingDirectory, shell, mergedCommand, false);
            processExecutor.execute(console);
            setExitCode(processExecutor.getExitCode());

            if (getExitCode() != 0) {
                break;
            }
        }
    }

    private static void mergeWithPrefix(Map<String, String> source, String prefix, Map<String, String> target) {
        if (source != null && prefix != null) {
            for (Map.Entry<String, String> entry : source.entrySet()) {
                target.put(prefix + entry.getKey(), entry.getValue());
            }
        }
    }

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
