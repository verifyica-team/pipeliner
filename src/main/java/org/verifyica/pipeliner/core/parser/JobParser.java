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

import java.util.List;
import java.util.Map;
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
        String string;
        Job job = new Job(pipeline, index);

        validator.validateIsMap(root, "job must be a map");

        map = converter.toMap(root);

        object = map.get("name");

        validator
                .validateNotNull(object, format("job[%d] name is required", index))
                .validateIsString(object, format("job[%d] name[%s] is not a string", index, object))
                .validateNotBlank((String) object, format("job[%d] name[%s] is blank", index, object));

        job.setName(converter.toString(object));

        object = map.get("id");

        if (object != null) {
            validator.validateIsString(object, format("job[%d] id is not a string", index));

            string = converter.toString(object);

            validator
                    .validateNotBlank(string, format("job[%d] id[] is blank", index))
                    .validateId(string, format("job[%d] id[%s] is invalid", index, string));

            job.setId(string);
        }

        object = map.get("enabled");

        if (object != null) {
            validator.validateIsString(object, format("job[%d] enabled is not a boolean", index));

            string = converter.toString(object);

            validator
                    .validateNotBlank(string, format("job[%d] enabled is blank", index))
                    .validateIsBoolean(string, format("job[%d] enabled is not a boolean", index));

            job.setEnabled(converter.toBoolean(string));
        }

        object = map.get("env");

        if (object != null) {
            validator.validateIsMap(object, format("job[%d] env is not a map", index));

            int subIndex = 1;
            Map<String, Object> envMap = converter.toMap(object);
            for (Map.Entry<String, Object> entry : envMap.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                validator
                        .validateNotBlank(name, format("job[%d] env[%d] name is blank", index, subIndex))
                        .validateNotNull(value, format("job[%d] env[%s] value must be a string", index, name));

                job.getEnvironmentVariables().put(name, converter.toString(value));
                subIndex++;
            }
        }

        object = map.get("with");

        if (object != null) {
            validator.validateIsMap(object, format("job[%d] with is not a map", index));

            int subIndex = 1;
            Map<String, Object> envMap = converter.toMap(object);
            for (Map.Entry<String, Object> entry : envMap.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                validator
                        .validateNotBlank(name, format("job[%d] with[%d] name is blank", index, subIndex))
                        .validateNotNull(value, format("job[%d] env[%s] value must be a string", index, name));

                job.getProperties().put(name, converter.toString(value));
                subIndex++;
            }
        }

        pipeline.getJobs().add(job);

        object = map.get("steps");

        validator
                .validateNotNull(object, format("job[%d] steps is required", index))
                .validateIsList(object, format("job[%d] steps must be an array", index));

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

        validator.validateCondition(!roots.isEmpty(), "jobs must be a non-empty array");

        int index = 1;
        for (Object root : roots) {
            Step step = stepParser.parseStep(job, root, index);
            job.getSteps().add(step);
            index++;
        }
    }
}
