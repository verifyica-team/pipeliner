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

package org.verifyica.pipeliner.core;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.Converter;
import org.verifyica.pipeliner.common.MessageSupplier;
import org.verifyica.pipeliner.common.Validator;
import org.verifyica.pipeliner.common.ValidatorException;
import org.verifyica.pipeliner.common.yaml.YamlConverter;
import org.verifyica.pipeliner.common.yaml.YamlFormatException;
import org.verifyica.pipeliner.common.yaml.YamlStringConstructor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;

/** Class to implement PipelineFactory */
@SuppressWarnings({"PMD.EmptyCatchBlock", "unchecked"})
public class PipelineFactory {

    private final Console console;
    private final Validator validator;
    private final Converter converter;

    /**
     * Constructor
     *
     * @param console console
     */
    public PipelineFactory(Console console) {
        this.console = console;
        this.validator = new Validator();
        this.converter = new Converter();
    }

    /**
     * Method to create a pipeline
     *
     * @param filename filename
     * @return a Pipeline
     * @throws ValidatorException ValidatorException
     * @throws YamlFormatException YamlFormatException
     * @throws MarkedYAMLException MarkedYAMLException
     * @throws IOException IOException
     */
    public Pipeline createPipeline(String filename)
            throws ValidatorException, YamlFormatException, MarkedYAMLException, IOException {
        console.trace("creating pipeline ...");
        console.trace("filename [%s]", filename);
        console.trace("loading YAML ...");

        Yaml yaml = new Yaml(new YamlStringConstructor());

        try (InputStream inputStream = Files.newInputStream(Paths.get(filename))) {
            Object object = yaml.load(inputStream);
            return parsePipeline(object);
        }
    }

    /**
     * Method to parse a pipeline
     *
     * @param root root
     * @return a Pipeline
     * @throws ValidatorException ValidatorException
     */
    private Pipeline parsePipeline(Object root) throws ValidatorException {
        console.trace("parsing pipeline ...");

        Pipeline pipeline = new Pipeline();
        Object object;
        String string;

        validator.validateNotNull(root, "pipeline is null").validateIsMap(root, "pipeline is not a map");

        object = converter.toMap(root).get("pipeline");

        validator.validateNotNull(object, "pipeline is required").validateIsMap(object, "pipeline must be a map");

        Map<String, Object> map = converter.toMap(object);
        object = map.get("name");

        validator
                .validateNotNull(object, "pipeline name is required")
                .validateIsString(object, "pipeline name is not a string")
                .validateNotBlank((String) object, "pipeline name [] is blank");

        pipeline.setName(converter.toString(object));

        object = map.get("id");

        if (object != null) {
            validator.validateIsString(object, "pipeline name is not a string");

            string = converter.toString(object);

            validator
                    .validateNotBlank(string, "pipeline id [] is blank")
                    .validateId(string, format("pipeline id [%s] is invalid", string));

            pipeline.setId(string);
        }

        object = map.get("enabled");

        if (object != null) {
            validator.validateIsString(object, "pipeline enabled is not a boolean");

            string = converter.toString(object);

            validator
                    .validateNotBlank(string, format("pipeline enabled [%s] is not a boolean", string))
                    .validateIsBoolean(string, format("pipeline enabled [%s] is not a boolean", string));

            pipeline.setEnabled(converter.toBoolean(string));
        } else {
            pipeline.setEnabled(true);
        }

        object = map.get("env");

        if (object != null) {
            validator.validateIsMap(object, "pipeline env is not a map");

            Map<String, Object> envMap = converter.toMap(object);
            for (Map.Entry<String, Object> entry : envMap.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                validator
                        .validateNotBlank(name, "pipeline env name is blank")
                        .validateNotNull(value, format("pipeline env [%s] value must be a string", name));

                pipeline.getEnvironmentVariables().put(name, converter.toString(value));
            }
        }

        object = map.get("with");

        if (object != null) {
            validator.validateIsMap(object, "pipeline with is not a map");

            Map<String, Object> envMap = converter.toMap(object);
            for (Map.Entry<String, Object> entry : envMap.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                validator
                        .validateNotBlank(name, "pipeline with name is blank")
                        .validateNotNull(value, format("pipeline with [%s] value must be a string", name));

                pipeline.getProperties().put(name, converter.toString(value));
            }
        }

        object = map.get("jobs");

        validator.validateNotNull(object, "jobs are required").validateIsList(object, "jobs must be an array");

        parseJobs(pipeline, converter.toList(object));

        return pipeline;
    }

    /**
     * Method to parse Jobs
     *
     * @param pipeline pipeline
     * @param objects objects
     * @throws ValidatorException ValidatorException
     */
    private void parseJobs(Pipeline pipeline, List<Object> objects) throws ValidatorException {
        console.trace("parsing jobs ...");

        validator.validateCondition(!objects.isEmpty(), "jobs must be a non-empty array");

        int index = 1;
        for (Object o : objects) {
            parseJob(pipeline, o, index);
            index++;
        }
    }

    /**
     * Method to parse a Job
     *
     * @param pipeline pipeline
     * @param root root
     * @param index index
     * @throws ValidatorException ValidatorException
     */
    private void parseJob(Pipeline pipeline, Object root, int index) throws ValidatorException {
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

        parseSteps(job, converter.toList(object));
    }

    /**
     * Method to parse Steps
     *
     * @param job job
     * @param objects object
     * @throws ValidatorException ValidatorException
     */
    private void parseSteps(Job job, List<Object> objects) throws ValidatorException {
        console.trace("parsing steps ...");

        validator.validateCondition(!objects.isEmpty(), "steps must be a non-empty array");

        int index = 1;
        for (Object o : objects) {
            parseStep(job, o, index);
            index++;
        }
    }

    /**
     * Method to parse a Step
     *
     * @param job job
     * @param root root
     * @param index index
     * @throws ValidatorException ValidatorException
     */
    private void parseStep(Job job, Object root, int index) throws ValidatorException {
        console.trace("parsing step[%d] ...", index);

        Map<Object, Object> stepMap = YamlConverter.asMap(root);

        Step step = new Step(job, index);

        try {
            step.setName(YamlConverter.asString(stepMap.get("name")));
            step.setId(YamlConverter.asString(stepMap.get("id")));
            step.setEnabled(parseEnabled(stepMap.get("enabled")));
            step.addEnvironmentVariables(parseEnv(YamlConverter.asMap(stepMap.get("env"))));
            step.addProperties(parseWith(YamlConverter.asMap(stepMap.get("with"))));
        } catch (ValidatorException e) {
            throw new ValidatorException(format("%s %s", step, e.getMessage()));
        }

        step.setShellType(parseShellType(step, YamlConverter.asString(stepMap.get("shell"))));
        step.setWorkingDirectory(YamlConverter.asString(stepMap.get("working-directory"), "."));
        step.getRuns().addAll(parseRun(step, stepMap.get("run")));

        job.getSteps().add(step);
    }

    /**
     * Method to parse env tag
     *
     * @param map map
     * @return a map of variables
     * @throws ValidatorException ValidatorException
     */
    private Map<String, String> parseEnv(Map<Object, Object> map) throws ValidatorException {
        // console.trace("parsing env ...");

        Map<String, String> environmentVariables = new LinkedHashMap<>();

        if (map != null) {
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                String key = entry.getKey().toString().trim();
                String value =
                        entry.getValue() != null ? entry.getValue().toString().trim() : null;

                validator
                        .validateNotBlank(key, MessageSupplier.of("environment variable is blank"))
                        .validateEnvironmentVariable(
                                key, MessageSupplier.of("environment variable [%s] is invalid", key));

                environmentVariables.put(key, value);
            }
        }

        return environmentVariables;
    }

    /**
     * Method to parse a with tag
     *
     * @param map map
     * @return a map of variables
     * @throws ValidatorException ValidatorException
     */
    private Map<String, String> parseWith(Map<Object, Object> map) throws ValidatorException {
        // console.trace("parsing with ...");

        Map<String, String> properties = new LinkedHashMap<>();

        if (map != null) {
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                String key = entry.getKey().toString().trim();
                String value = entry.getValue().toString().trim();

                validator
                        .validateNotBlank(key, MessageSupplier.of("property is blank"))
                        .validateProperty(key, MessageSupplier.of("property [%s] is invalid", key));

                properties.put("INPUT_" + key, value);
            }
        }

        return properties;
    }

    /**
     * Method to parse a shell tag
     *
     * @param step step
     * @param string string
     * @return a ShellType
     * @throws ValidatorException ValidatorException
     */
    private ShellType parseShellType(Step step, String string) throws ValidatorException {
        // console.trace("parsing shell [%s] ...", string);

        ShellType shellType = null;

        if (string == null || string.trim().isEmpty()) {
            shellType = ShellType.UNSPECIFIED;
        } else {
            if (string.trim().equals("bash")) {
                shellType = ShellType.BASH;
            } else if (string.trim().equals("sh")) {
                shellType = ShellType.SH;
            } else {
                throw new ValidatorException(format("shell [%s] is invalid", string.trim()));
            }
        }

        return shellType;
    }

    /**
     * Method to parse a run tag
     *
     * @param step step
     * @param object object
     * @return a list of Runs
     * @throws ValidatorException PipelineValidationException
     */
    private List<Run> parseRun(Step step, Object object) throws ValidatorException {
        List<Run> runs = new ArrayList<>();

        if (object instanceof String) {
            String string = ((String) object).trim();
            List<String> lines = splitOnCRLF(string);

            for (int i = 0; i < lines.size(); i++) {
                String command = lines.get(i);
                validator.validateNotNull(command, MessageSupplier.of("run[%d] is null", i));
                command = command.trim();
                validator.validateNotBlank(command, MessageSupplier.of("run[%d] is blank", i));
                runs.add(new Run(step, command));
            }
        } else if (object == null) {
            throw new ValidatorException("run is null");
        } else {
            throw new ValidatorException("run is not a string");
        }

        return runs;
    }

    private boolean parseEnabled(Object object) throws ValidatorException {
        if (object == null) {
            return true;
        }

        try {
            String string = ((String) object).trim().toLowerCase(Locale.US);

            switch (string) {
                case "true":
                case "yes":
                case "y":
                case "on":
                case "1":
                    return true;
                default:
                    return false;
            }
        } catch (ClassCastException e) {
            throw new ValidatorException("enabled is not a string");
        }
    }

    /**
     * Method to split a String on CRLF
     *
     * @param string string
     * @return a list of Strings
     */
    private List<String> splitOnCRLF(String string) {
        List<String> tokens = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new StringReader(string))) {
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }

                line = line.trim();
                if (!line.isEmpty()) {
                    tokens.add(line);
                }
            }
        } catch (IOException e) {
            // INTENTIONALLY BLANK
        }

        for (String token : tokens) {
            console.trace("token [%s]", token);
        }

        return mergeTokens(tokens);
    }

    /**
     * Method to merge a list of Strings based no the presence of and ending substring of " \"
     *
     * @param tokens tokens
     * @return a list of merged tokens
     */
    private static List<String> mergeTokens(List<String> tokens) {
        List<String> mergedTokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if (token.endsWith(" \\") && i < tokens.size() - 1) {
                currentToken.append(token, 0, token.length() - 1);
            } else {
                currentToken.append(token);
                mergedTokens.add(currentToken.toString());
                currentToken.setLength(0);
            }
        }

        return mergedTokens;
    }
}
