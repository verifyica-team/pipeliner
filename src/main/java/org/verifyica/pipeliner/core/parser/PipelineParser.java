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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.ValidatorException;
import org.verifyica.pipeliner.common.yaml.YamlStringConstructor;
import org.verifyica.pipeliner.core.Job;
import org.verifyica.pipeliner.core.Pipeline;
import org.yaml.snakeyaml.Yaml;

/** Class to implement PipelineParser */
public class PipelineParser extends Parser {

    private final JobParser jobParser;

    /**
     * Constructor
     *
     * @param console console
     */
    public PipelineParser(Console console) {
        super(console);

        jobParser = new JobParser(console);
    }

    /**
     * Method to parse a pipeline
     *
     * @param filename filename
     * @return a Pipeline
     * @throws ValidatorException ValidatorException
     * @throws IOException IOException
     */
    public Pipeline parse(String filename) throws ValidatorException, IOException {
        console.trace("parsing pipeline ...");

        Yaml yaml = new Yaml(new YamlStringConstructor());

        try (InputStream inputStream = Files.newInputStream(Paths.get(filename))) {
            Object root = yaml.load(inputStream);

            Pipeline pipeline = new Pipeline();
            Object object;
            String string;

            validator.notNull(root, "pipeline is null").isMap(root, "pipeline is not a map");

            object = converter.toMap(root).get("pipeline");

            validator.notNull(object, "pipeline is required").isMap(object, "pipeline must be a map");

            Map<String, Object> map = converter.toMap(object);
            object = map.get("name");

            validator
                    .notNull(object, "pipeline name is required")
                    .isString(object, "pipeline name is not a string")
                    .notBlank(converter.toString(object), "pipeline name [] is blank");

            pipeline.setName(converter.toString(object));

            object = map.get("id");
            if (object != null) {
                validator.isString(object, "pipeline id is not a string");

                string = converter.toString(object);

                validator
                        .notBlank(string, "pipeline id [] is blank")
                        .isValidId(string, format("pipeline id [%s] is invalid", string));

                pipeline.setId(string);
            }

            object = map.get("env");
            if (object != null) {
                validator.isMap(object, "pipeline env is not a map");

                for (Map.Entry<String, Object> entry : converter.toMap(object).entrySet()) {
                    String name = entry.getKey();
                    Object value = entry.getValue();

                    validator
                            .notBlank(name, "pipeline env is blank")
                            .notNull(name, format("pipeline env[%s] must be a string", name))
                            .isValidEnvironmentVariable(name, format("pipeline env[%s] is invalid", name))
                            .isString(value, format("pipeline with[%s] value must be a string", name));
                    ;

                    console.trace("pipeline environment variable [%s] = [%s]", name, value);

                    pipeline.getEnvironmentVariables().put(name, converter.toString(value));
                }
            }

            object = map.get("with");
            if (object != null) {
                validator.isMap(object, "pipeline with is not a map");

                for (Map.Entry<String, Object> entry : converter.toMap(object).entrySet()) {
                    String name = entry.getKey();
                    Object value = entry.getValue();

                    validator
                            .notBlank(name, "pipeline with is blank")
                            .notNull(name, format("pipeline with[%s] must be a string", name))
                            .isValidProperty(name, format("pipeline with[%s] is invalid", name))
                            .isString(value, format("pipeline with[%s] value must be a string", name));

                    console.trace("pipeline property [%s] = [%s]", name, value);

                    pipeline.getProperties().put("INPUT_" + name, converter.toString(value));
                }
            }

            object = map.get("jobs");

            validator.notNull(object, "jobs are required").isList(object, "jobs must be an array");

            parseJobs(console, pipeline, converter.toList(object));

            return pipeline;
        }
    }

    /**
     * Method to parse Jobs
     *
     * @param pipeline pipeline
     * @param roots roots
     * @throws ValidatorException ValidatorException
     */
    private void parseJobs(Console console, Pipeline pipeline, List<Object> roots) throws ValidatorException {
        console.trace("parsing jobs ...");

        validator.condition(!roots.isEmpty(), "jobs must be a non-empty array");

        Set<String> currentIds = new HashSet<>();
        int index = 1;

        for (Object root : roots) {
            Job job = jobParser.parseJob(pipeline, root, index);

            if (!currentIds.add(job.getId())) {
                throw new ValidatorException(format("duplicate job id[%s]", job.getId()));
            }

            pipeline.getJobs().add(job);
            index++;
        }
    }
}
