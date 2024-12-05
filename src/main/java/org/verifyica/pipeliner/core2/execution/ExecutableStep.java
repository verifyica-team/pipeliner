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
import java.util.LinkedHashMap;
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
        console.log("%s", step);

        if (decodeEnabled(step.getEnabled())) {
            getStopwatch().reset();

            run(console);

            console.log(
                    "%s exit-code=[%d] ms=[%d]",
                    step, getExitCode(), getStopwatch().elapsedTime().toMillis());
        } else {
            console.log("%s", step);
        }
    }

    private void run(Console console) {
        Job job = (Job) step.getParent();
        Pipeline pipeline = (Pipeline) job.getParent();

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

        // Scoped / legacy "with"
        /*
        mergeWithPrefix(pipeline.getWith(), "INPUT_", resolvedWith);
        mergeWithPrefix(job.getWith(), "INPUT_", resolvedWith);
        mergeWithPrefix(step.getWith(), "INPUT_", resolvedWith);
        */

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

        String PROPERTY_MATCHING_REGEX = "(?<!\\\\)\\$\\{\\{\\s*([a-zA-Z0-9_\\-.]+)\\s*\\}\\}";

        String workingDirectory = step.getWorkingDirectory();
        String resolvedWorkingDirectory =
                RecursiveReplacer.replace(resolvedWith, PROPERTY_MATCHING_REGEX, workingDirectory);

        resolvedEnv.forEach((name, value) -> console.trace("%s env [%s] = [%s]", step, name, value));
        resolvedWith.forEach((name, value) -> console.trace("%s with [%s] = [%s]", step, name, value));
        resolvedOpt.forEach((name, value) -> console.trace("%s opt [%s] = [%s]", step, name, value));

        console.trace("%s working directory [%s]", step, resolvedWorkingDirectory);

        String run = step.getRun();

        List<String> commands = mergeLines(Arrays.asList(run.split("\\R")));

        Iterator<String> commandsIterator = commands.iterator();
        while (commandsIterator.hasNext()) {
            Shell shell = Shell.decode(step.getShell());
            String command = commandsIterator.next();
            String resolvedCommand = RecursiveReplacer.replace(resolvedWith, PROPERTY_MATCHING_REGEX, command);

            console.trace("%s shell [%s]", step, shell);

            console.log(step);

            if ("mask".equals(resolvedOpt.get("properties"))) {
                console.log("$ %s", command);
            } else {
                console.log("$ %s", resolvedCommand);
            }

            ProcessExecutor processExecutor =
                    new ProcessExecutor(resolvedEnv, resolvedWorkingDirectory, shell, resolvedCommand, false);
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
