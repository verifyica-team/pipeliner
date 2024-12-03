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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.ValidatorException;
import org.verifyica.pipeliner.core.Job;
import org.verifyica.pipeliner.core.Pipeline;
import org.verifyica.pipeliner.core.Step;

/** Class to implement JobParser */
public class JobParser extends Parser {

    private final StepParser stepParser;

    /**
     * Constructor
     *
     * @param console console
     */
    public JobParser(Console console) {
        super(console);

        stepParser = new StepParser(console);
    }

    /**
     * Method to parse a job
     *
     * @param pipeline pipeline
     * @param root root
     * @param index index
     * @return a Job
     * @throws ValidatorException ValidatorException
     */
    public Job parseJob(Pipeline pipeline, Object root, int index) throws ValidatorException {
        console.trace("parsing job[%d] ...", index);

        Map<String, Object> map;
        Object object;
        Job job = new Job(pipeline, index);

        validator.isMap(root, "job must be a map");

        map = converter.toMap(root);
        object = map.get("name");

        validator
                .notNull(object, format("job[%d] name is required", index))
                .isString(object, format("job[%d] name[%s] is not a string", index, object))
                .notBlank(converter.toString(object), format("job[%d] name[%s] is blank", index, object));

        job.setName(converter.toString(object));

        object = map.get("id");
        if (object != null) {
            validator
                    .isString(object, format("job[%d] id is not a string", index))
                    .notBlank(converter.toString(object), format("job[%d] id is blank", index))
                    .isValidId(converter.toString(object), format("job[%d] id[%s] is invalid", index, object));

            job.setId(converter.toString(object));
        }

        object = map.get("enabled");
        if (object != null) {
            validator
                    .isString(object, format("job[%d] enabled is not a boolean", index))
                    .notBlank(converter.toString(object), format("job[%d] enabled is blank", index))
                    .isBoolean(object, format("job[%d] enabled is not a boolean", index));

            job.setEnabled(converter.toBoolean(object));
        }

        object = map.get("env");
        if (object != null) {
            validator.isMap(object, "pipeline env is not a map");

            for (Map.Entry<String, Object> entry : converter.toMap(object).entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                validator
                        .notNull(name, format("job[%d] with[%s] must be a string", index, name))
                        .notBlank(name, format("job[%d] with is blank", index))
                        .isValidEnvironmentVariable(name, format("job[%d] with[%s] is invalid", index, name))
                        .isString(value, format("job[%d] with[%s] value must be a string", index, name));

                console.trace("job[%d] environment variable [%s] = [%s]", index, name, value);

                job.getEnvironmentVariables().put(name, converter.toString(value));
            }
        }

        object = map.get("with");
        if (object != null) {
            validator.isMap(object, "job with is not a map");

            for (Map.Entry<String, Object> entry : converter.toMap(object).entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                validator
                        .notNull(name, format("job[%d] with[%s] must be a string", index, name))
                        .notBlank(name, format("job[%d] with is blank", index))
                        .isValidProperty(name, format("job[%d] with[%s] is invalid", index, name))
                        .isString(value, format("job[%d] with[%s] value must be a string", index, name));

                console.trace("job[%d] property [%s] = [%s]", index, name, value);

                job.getProperties().put(name, converter.toString(value));
                job.getProperties().put("INPUT_" + name, converter.toString(value));
            }
        }

        object = map.get("opt");
        if (object != null) {
            validator.isMap(object, "job opt is not a map");

            for (Map.Entry<String, Object> entry : converter.toMap(object).entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                validator
                        .notNull(name, format("job[%d] opt[%s] must be a string", index, name))
                        .notBlank(name, format("job[%d] opt is blank", index))
                        .isString(value, format("job[%d] opt[%s] value must be a string", index, name));

                name = name.trim();

                console.trace("job[%d] option [%s] = [%s]", index, name, value);

                job.getOptions().put(name, converter.toString(value));
            }
        }

        object = map.get("steps");

        validator
                .notNull(object, format("job[%d] steps is required", index))
                .isList(object, format("job[%d] steps must be an array", index));

        parseSteps(console, job, converter.toList(object));

        return job;
    }

    /**
     * Method to parse Steps
     *
     * @param console console
     * @param job job
     * @param roots roots
     * @throws ValidatorException ValidatorException
     */
    private void parseSteps(Console console, Job job, List<Object> roots) throws ValidatorException {
        console.trace("parsing steps ...");

        validator.condition(!roots.isEmpty(), "jobs must be a non-empty array");

        Set<String> currentIds = new HashSet<>();
        int index = 1;

        for (Object root : roots) {
            Step step = stepParser.parseStep(job, root, index);

            if (!currentIds.add(step.getId())) {
                throw new ValidatorException(format("duplicate step id[%s]", step.getId()));
            }

            job.getSteps().add(step);
            currentIds.add(step.getId());
            index++;
        }
    }
}
