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

package org.verifyica.pipeliner.core.parser;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.ValidatorException;
import org.verifyica.pipeliner.core.CaptureType;
import org.verifyica.pipeliner.core.Job;
import org.verifyica.pipeliner.core.Run;
import org.verifyica.pipeliner.core.ShellType;
import org.verifyica.pipeliner.core.Step;

/** Class to implement StepParser */
public class StepParser extends Parser {

    /**
     * Constructor
     *
     * @param console console
     */
    public StepParser(Console console) {
        super(console);
    }

    /**
     * Method to parse a step
     *
     * @param job job
     * @param root root
     * @param index index
     * @return a Step
     * @throws ValidatorException ValidatorException
     */
    public Step parseStep(Job job, Object root, int index) throws ValidatorException {
        console.trace("parsing step[%d] ...", index);

        Map<String, Object> map;
        Object object;
        Step step = new Step(job, index);

        validator.isMap(root, "step must be a map");

        map = converter.toMap(root);
        object = map.get("name");

        validator
                .notNull(object, format("step[%d] name is required", index))
                .isString(object, format("step[%d] name[%s] is not a string", index, object))
                .notBlank(converter.toString(object), format("step[%d] name[%s] is blank", index, object));

        step.setName(converter.toString(object));

        object = map.get("id");
        if (object != null) {
            validator
                    .isString(object, format("step[%d] id is not a string", index))
                    .notBlank(converter.toString(object), format("step[%d] id is blank", index))
                    .isValidId(converter.toString(object), format("step[%d] id[%s] is invalid", index, object));

            step.setId(converter.toString(object));
        }

        object = map.get("enabled");
        if (object != null) {
            validator
                    .isString(object, format("step[%d] enabled is not a boolean", index))
                    .notBlank(converter.toString(object), format("step[%d] enabled is blank", index))
                    .isBoolean(object, format("step[%d] enabled is not a boolean", index));

            step.setEnabled(converter.toBoolean(object));
        }

        object = map.get("env");
        if (object != null) {
            validator.isMap(object, format("step[%d] env is not a map", index));

            int subIndex = 1;
            Map<String, Object> envMap = converter.toMap(object);
            for (Map.Entry<String, Object> entry : envMap.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                validator
                        .notBlank(name, format("step[%d] env[%d] is blank", index, subIndex))
                        .notNull(name, format("step[%d] env[%s] must be a string", index, name))
                        .isValidEnvironmentVariable(name, format("step[%d] env[%s] is invalid", index, name))
                        .isString(value, format("step[%d] env[%s] must be a string", index, name));

                console.trace("step[%d] environment variable [%s] = [%s]", index, name, value);

                step.getEnvironmentVariables().put(name, converter.toString(value));

                subIndex++;
            }
        }

        object = map.get("with");
        if (object != null) {
            validator.isMap(object, format("step[%d] with is not a map", index));

            int subIndex = 1;
            Map<String, Object> envMap = converter.toMap(object);
            for (Map.Entry<String, Object> entry : envMap.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                validator
                        .notNull(name, format("step[%d] with[%s] must be a string", index, name))
                        .notBlank(name, format("step[%d] with[%d] is blank", index, subIndex))
                        .isValidProperty(name, format("step[%d] with[%s] is invalid", index, name))
                        .isString(value, format("step[%d] with[%s] must be a string", index, name));

                console.trace("step[%d] property [%s] = [%s]", index, name, value);

                step.getProperties().put(name, converter.toString(value));
                step.getProperties().put(step.getId() + "." + name, converter.toString(value));
                step.getProperties().put("INPUT_" + name, converter.toString(value));

                subIndex++;
            }
        }

        object = map.get("opt");
        if (object != null) {
            validator.isMap(object, format("step[%d] opt is not a map", index));

            int subIndex = 1;
            Map<String, Object> envMap = converter.toMap(object);
            for (Map.Entry<String, Object> entry : envMap.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                validator
                        .notNull(name, format("step[%d] opt[%s] must be a string", index, name))
                        .notBlank(name, format("step[%d] opt[%d] is blank", index, subIndex))
                        .isString(value, format("step[%d] opt[%s] must be a string", index, name));

                name = name.trim();

                console.trace("step[%d] option [%s] = [%s]", index, name, value);

                step.getOptions().put(name, converter.toString(value));

                subIndex++;
            }
        }

        object = map.get("shell");
        if (object != null) {
            validator
                    .notNull(object, format("step[%d] shell is null", index))
                    .isString(object, format("step[%d] shell must be a string", index))
                    .notBlank(converter.toString(object), format("step[%d] shell is blank", index));

            step.setShellType(parseShellType(converter.toString(object), index));
        }

        object = map.get("working-directory");
        if (object != null) {
            validator
                    .notNull(object, format("step[%d] working-directory is null", index))
                    .isString(object, format("step[%d] working-directory must be a string", index))
                    .notBlank(converter.toString(object), format("step[%d] working-directory is blank", index));

            step.setWorkingDirectory(converter.toString(object));
        } else {
            step.setWorkingDirectory(".");
        }

        object = map.get("run");

        validator
                .notNull(object, format("step[%d] run is required", index))
                .isString(object, format("step[%d] run must be a string", index))
                .notBlank(converter.toString(object), format("step[%d] run is blank", index));

        String string = converter.toString(object).trim();

        int subIndex = 1;
        List<String> commands = mergeLines(Arrays.asList(string.split("\\R")));

        for (String command : commands) {
            validator
                    .notNull(command, format("step[%d] run[%d] is null", index, subIndex))
                    .notBlank(command, format("step[%d] run[%d] is blank", index, subIndex));

            Run run = new Run(step, command);

            CaptureType captureType = parseCaptureType(command);
            String captureVariable = parseCaptureVariable(command, captureType);

            if (captureType == CaptureType.APPEND || captureType == CaptureType.OVERWRITE) {
                validator
                        .notNull(captureVariable, format("step[%d] run[%d] capture variable is null", index, subIndex))
                        .notBlank(
                                captureVariable, format("step[%d] run[%d] capture variable is blank", index, subIndex))
                        .isValidProperty(
                                captureVariable,
                                format(
                                        "step[%d] run[%d] capture variable [%s] is invalid",
                                        index, subIndex, captureVariable));
            }

            run.setCapture(captureType, captureVariable);

            step.getRuns().add(run);
            subIndex++;
        }

        return step;
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

    /**
     * Method to parse a shell tag
     *
     * @param string string
     * @param index index
     * @return a ShellType
     * @throws ValidatorException ValidatorException
     */
    private ShellType parseShellType(String string, int index) throws ValidatorException {
        // console.trace("parsing shell [%s] ...", string);

        ShellType shellType;

        if (string == null || string.trim().isEmpty()) {
            shellType = ShellType.UNSPECIFIED;
        } else {
            if (string.trim().equals("bash")) {
                shellType = ShellType.BASH;
            } else if (string.trim().equals("sh")) {
                shellType = ShellType.SH;
            } else {
                throw new ValidatorException(format("step[%d] shell[%s] is invalid", index, string.trim()));
            }
        }

        return shellType;
    }

    private CaptureType parseCaptureType(String command) {
        console.trace("parseCaptureType command [%s]", command);

        CaptureType captureType;

        String pattern = ".*>>\\s*\\$[A-Za-z0-9][A-Za-z0-9\\-._]*$";
        if (command.matches(pattern)) {
            captureType = CaptureType.APPEND;
        } else {
            pattern = ".*>\\s*\\$[A-Za-z0-9][A-Za-z0-9\\-._]*$";
            if (command.matches(pattern)) {
                captureType = CaptureType.OVERWRITE;
            } else {
                captureType = CaptureType.NONE;
            }
        }

        console.trace("parseCaptureType command [%s] captureType [%s]", command, captureType);

        return captureType;
    }

    private String parseCaptureVariable(String command, CaptureType captureType) {
        console.trace("parseCaptureVariable command [%s] captureType [%s]", command, captureType);

        String captureVariable;

        switch (captureType) {
            case APPEND:
            case OVERWRITE: {
                captureVariable = command.substring(command.lastIndexOf("$") + 1);
                break;
            }
            case NONE:
            default: {
                captureVariable = null;
            }
        }

        console.trace(
                "parseCaptureVariable command [%s] captureType [%s] captureVariable [%s]",
                command, captureType, captureVariable);

        return captureVariable;
    }
}
