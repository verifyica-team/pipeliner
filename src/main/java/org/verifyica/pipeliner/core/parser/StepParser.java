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

import java.util.Map;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.ValidatorException;
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

        validator.validateIsMap(root, "step must be a map");

        map = converter.toMap(root);
        object = map.get("name");

        validator
                .validateNotNull(object, format("step[%d] name is required", index))
                .validateIsString(object, format("step[%d] name[%s] is not a string", index, object))
                .validateNotBlank((String) object, format("step[%d] name[%s] is blank", index, object));

        step.setName(converter.toString(object));

        object = map.get("id");
        if (object != null) {
            validator
                    .validateIsString(object, format("step[%d] id is not a string", index))
                    .validateNotBlank((String) object, format("step[%d] id is blank", index))
                    .validateId((String) object, format("step[%d] id[%s] is invalid", index, object));

            step.setId(converter.toString(object));
        }

        object = map.get("enabled");
        if (object != null) {
            validator
                    .validateIsString(object, format("step[%d] enabled is not a boolean", index))
                    .validateNotBlank((String) object, format("step[%d] enabled is blank", index))
                    .validateIsBoolean(object, format("step[%d] enabled is not a boolean", index));

            step.setEnabled(converter.toBoolean(object));
        }

        object = map.get("env");
        if (object != null) {
            validator.validateIsMap(object, format("step[%d] env is not a map", index));

            int subIndex = 1;
            Map<String, Object> envMap = converter.toMap(object);
            for (Map.Entry<String, Object> entry : envMap.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                validator
                        .validateNotBlank(name, format("step[%d] env[%d] is blank", index, subIndex))
                        .validateNotNull(name, format("step[%d] env[%s] must be a string", index, name))
                        .validateEnvironmentVariable(name, format("step[%d] env[%s] is invalid", index, name));

                step.getEnvironmentVariables().put(name, converter.toString(value));
                subIndex++;
            }
        }

        object = map.get("with");
        if (object != null) {
            validator.validateIsMap(object, format("step[%d] with is not a map", index));

            int subIndex = 1;
            Map<String, Object> envMap = converter.toMap(object);
            for (Map.Entry<String, Object> entry : envMap.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                validator
                        .validateNotBlank(name, format("step[%d] with[%d] is blank", index, subIndex))
                        .validateNotNull(name, format("step[%d] with[%s] must be a string", index, name))
                        .validateProperty(name, format("step[%d] with[%s] is invalid", index, name));

                step.getProperties().put("INPUT_" + name, converter.toString(value));
                subIndex++;
            }
        }

        object = map.get("shell");
        if (object != null) {
            validator
                    .validateNotNull(object, format("step[%d] shell is null", index))
                    .validateIsString(object, format("step[%d] shell must be a string", index))
                    .validateNotBlank((String) object, format("step[%d] shell is blank", index));

            step.setShellType(parseShellType(converter.toString(object), index));
        }

        object = map.get("working-directory");
        if (object != null) {
            validator
                    .validateNotNull(object, format("step[%d] working-directory is null", index))
                    .validateIsString(object, format("step[%d] working-directory must be a string", index))
                    .validateNotBlank((String) object, format("step[%d] working-directory is blank", index));

            step.setWorkingDirectory(converter.toString(object));
        } else {
            step.setWorkingDirectory(".");
        }

        object = map.get("run");

        validator
                .validateNotNull(object, format("step[%d] run is required", index))
                .validateIsString(object, format("step[%d] run must be a string", index))
                .validateNotBlank((String) object, format("step[%d] run is blank", index));

        String run = converter.toString(object);

        int subIndex = 1;
        String[] commands = run.split("\\R");
        for (String command : commands) {
            validator
                    .validateNotNull(command, format("step[%d] run[%d] is null", index, subIndex))
                    .validateNotBlank(command, format("step[%d] run[%d] is blank", index, subIndex));

            step.getRuns().add(new Run(step, command.trim()));
            subIndex++;
        }

        return step;
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
}
