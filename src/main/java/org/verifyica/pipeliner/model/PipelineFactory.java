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

package org.verifyica.pipeliner.model;

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
import java.util.Map;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.common.Validator;
import org.verifyica.pipeliner.common.ValidatorException;
import org.verifyica.pipeliner.yaml.YamlConverter;
import org.verifyica.pipeliner.yaml.YamlFormatException;
import org.verifyica.pipeliner.yaml.YamlStringConstructor;
import org.verifyica.pipeliner.yaml.YamlValueException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;

/** Class to implement PipelineFactory */
@SuppressWarnings({"PMD.EmptyCatchBlock", "unchecked"})
public class PipelineFactory {

    private static final String ID_REGEX = "^[a-zA-Z0-9-_]*$";

    private final Console console;
    private int jobIndex;
    private int stepIndex;

    /**
     * Constructor
     *
     * @param console console
     */
    public PipelineFactory(Console console) {
        this.console = console;
    }

    /**
     * Method to create a pipeline
     *
     * @param filename filename
     * @return a Pipeline
     */
    public Pipeline createPipeline(String filename)
            throws ValidatorException, YamlFormatException, MarkedYAMLException, IOException {
        console.trace("creating pipeline ...");
        console.trace("filename [%s]", filename);
        console.trace("loading YAML ...");

        Yaml yaml = new Yaml(new YamlStringConstructor());

        try (InputStream inputStream = Files.newInputStream(Paths.get(filename))) {
            Map<Object, Object> pipelineMap = yaml.load(inputStream);
            Pipeline pipeline = parsePipeline(pipelineMap);
            validatePipeline(pipeline);
            return pipeline;
        }
    }

    /**
     * Method to validate a Pipeline
     *
     * @param pipeline pipeline
     */
    private void validatePipeline(Pipeline pipeline) {
        console.trace("validating pipeline ...");

        if (isNullOrBlank(pipeline.getName())) {
            throw new YamlValueException(format("%s ... name=[] is blank", errorMessage(pipeline)));
        }

        if (isInvalidName(pipeline.getName())) {
            throw new YamlValueException(
                    format("%s ... name=[%s] is invalid", errorMessage(pipeline), pipeline.getName()));
        }

        if (isInvalidId(pipeline.getId())) {
            throw new YamlValueException(format("%s ... id=[%s] is invalid", errorMessage(pipeline), pipeline.getId()));
        }

        for (Job job : pipeline.getJobs()) {
            // console.trace("validating job [%s]", job.getId());

            if (isNullOrBlank(job.getName())) {
                throw new YamlValueException(format("%s ... name=[] is blank", errorMessage(job)));
            }

            if (isInvalidName(job.getName())) {
                throw new YamlValueException(format("%s ... name=[%s] is invalid", errorMessage(job), job.getName()));
            }

            if (isInvalidId(job.getId())) {
                throw new YamlValueException(format("%s ... id=[%s] is invalid", errorMessage(job), job.getId()));
            }

            for (Step step : job.getSteps()) {
                // console.trace("validating step [%s]", step.getId());

                if (isNullOrBlank(step.getName())) {
                    throw new YamlValueException(format("%s ... name=[] is blank", errorMessage(step)));
                }

                if (isInvalidName(step.getName())) {
                    throw new YamlValueException(
                            format("%s ... name=[%s] is invalid", errorMessage(step), step.getName()));
                }

                if (isInvalidId(step.getId())) {
                    throw new YamlValueException(format("%s ... id=[%s] is invalid", errorMessage(step), step.getId()));
                }

                if (isNullOrBlank(step.getWorkingDirectory())) {
                    throw new YamlValueException(format("%s ... working-directory=[] is blank", errorMessage(step)));
                }

                List<Run> runs = step.getRuns();

                if (runs.isEmpty()) {
                    throw new YamlValueException(format("%s ... run=[] is blank", errorMessage(step)));
                }

                for (Run run : runs) {
                    if (isNullOrBlank(run.getCommand())) {
                        throw new YamlValueException(format("%s ... run=[] is blank", errorMessage(step)));
                    }
                }
            }
        }
    }

    /**
     * Method to parse a pipeline
     *
     * @param map map
     * @return a Pipeline
     * @throws ValidatorException ValidatorException
     */
    private Pipeline parsePipeline(Map<Object, Object> map) throws ValidatorException {
        console.trace("parsing pipeline ...");

        Map<Object, Object> pipelineMap = (Map<Object, Object>) map.get("pipeline");

        if (pipelineMap == null) {
            throw new YamlFormatException("Pipeline definition must start with [pipeline:]");
        }

        Pipeline pipeline = new Pipeline();

        try {
            pipeline.setName(YamlConverter.asString(pipelineMap.get("name")));
            pipeline.setId(YamlConverter.asString(pipelineMap.get("id")));
            pipeline.setEnabled(YamlConverter.asBoolean(pipelineMap.get("enabled"), true));
            pipeline.addEnvironmentVariables(parseEnv(YamlConverter.asMap(pipelineMap.get("env"))));
            pipeline.addProperties(parseWith(YamlConverter.asMap(pipelineMap.get("with"))));
        } catch (ValidatorException e) {
            ValidatorException.propagate("%s %s", pipeline, e.getMessage());
        }

        // System.out.printf("  name=[%s]%n", pipeline.getName());

        pipeline.addJobs(parseJobs(pipeline, YamlConverter.asList(pipelineMap.get("jobs"))));

        return pipeline;
    }

    /**
     * Method to parse Jobs
     *
     * @param pipeline pipeline
     * @param objects objects
     * @return a list of Jobs
     * @throws ValidatorException ValidatorException
     */
    private List<Job> parseJobs(Pipeline pipeline, List<Object> objects) throws ValidatorException {
        // console.trace("parsing jobs ...");

        List<Job> jobs = new ArrayList<>();

        for (Object object : objects) {
            jobs.add(parseJob(pipeline, object));
        }

        return jobs;
    }

    /**
     * Method to parse a Job
     *
     * @param pipeline pipeline
     * @param object object
     * @return a Job
     * @throws ValidatorException ValidatorException
     */
    private Job parseJob(Pipeline pipeline, Object object) throws ValidatorException {
        // console.trace("parse steps ...");

        Map<Object, Object> jobMap = YamlConverter.asMap(object);

        Job job = new Job(pipeline, ++jobIndex);

        try {
            job.setName(YamlConverter.asString(jobMap.get("name")));
            job.setId(YamlConverter.asString(jobMap.get("id")));
            job.setEnabled(YamlConverter.asBoolean(jobMap.get("enabled"), true));
            job.addEnvironmentVariables(parseEnv(YamlConverter.asMap(jobMap.get("env"))));
            job.addProperties(parseWith(YamlConverter.asMap(jobMap.get("with"))));
        } catch (ValidatorException e) {
            ValidatorException.propagate("%s %s", job, e.getMessage());
        }

        // System.out.printf("  name=[%s]%n", job.getName());

        job.addSteps(parseSteps(job, YamlConverter.asList(jobMap.get("steps"))));

        return job;
    }

    /**
     * Method to parse Steps
     *
     * @param job job
     * @param objects object
     * @return a list of Steps
     * @throws ValidatorException ValidatorException
     */
    private List<Step> parseSteps(Job job, List<Object> objects) throws ValidatorException {
        // console.trace("parse step ...");

        stepIndex = 0;

        List<Step> steps = new ArrayList<>();

        if (objects != null) {
            for (Object object : objects) {
                steps.add(parseStep(job, object));
            }
        }

        return steps;
    }

    /**
     * Method to parse a Step
     *
     * @param job job
     * @param object object
     * @return a Step
     * @throws ValidatorException ValidatorException
     */
    private Step parseStep(Job job, Object object) throws ValidatorException {
        // console.trace("parsing step ...");

        Map<Object, Object> stepMap = YamlConverter.asMap(object);

        Step step = new Step(job, ++stepIndex);

        try {
            step.setName(YamlConverter.asString(stepMap.get("name")));
            step.setId(YamlConverter.asString(stepMap.get("id")));
            step.setEnabled(YamlConverter.asBoolean(stepMap.get("enabled"), true));
            step.addEnvironmentVariables(parseEnv(YamlConverter.asMap(stepMap.get("env"))));
            step.addProperties(parseWith(YamlConverter.asMap(stepMap.get("with"))));
        } catch (ValidatorException e) {
            ValidatorException.propagate("%s %s", step, e.getMessage());
        }

        step.setShellType(parseShellType(step, YamlConverter.asString(stepMap.get("shell"))));
        step.setWorkingDirectory(YamlConverter.asString(stepMap.get("working-directory"), "."));
        step.addRuns(parseRun(step, YamlConverter.asString(stepMap.get("run"))));

        return step;
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

                Validator.validateEnvironmentVariable(key);
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

                Validator.validateProperty(key);
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
                ValidatorException.propagate("%s shell=[%s] is invalid", step, string.trim());
            }
        }

        return shellType;
    }

    /**
     * Method to parse a run tag
     *
     * @param step step
     * @param string string
     * @return a list of Runs
     * @throws ValidatorException ValidatorException
     */
    private List<Run> parseRun(Step step, String string) throws ValidatorException {
        // console.trace("parsing run [%s]", string);

        List<Run> runs = new ArrayList<>();

        if (string == null) {
            throw new ValidatorException("run is null");
        }

        if (string.trim().isEmpty()) {
            throw new ValidatorException("run is empty");
        }

        List<String> values = splitOnCRLF(string);
        for (String command : values) {
            console.trace("command [%s]", command.trim());

            if (!command.trim().isEmpty()) {
                runs.add(new Run(step, command.trim()));
            }
        }

        if (runs.isEmpty()) {
            throw new ValidatorException("run is empty");
        }

        return runs;
    }

    /**
     * Method to get a pipeline error message
     *
     * @param pipeline pipeline
     * @return a pipeline error message
     */
    private String errorMessage(Pipeline pipeline) {
        return format(
                "@pipeline name=[%s] id=[%s] ref=[%s]",
                pipeline.getName() == null ? "" : pipeline.getName(), pipeline.getId(), pipeline.getReference());
    }

    /**
     * Method to get a job error message
     *
     * @param job job
     * @return a pipeline error message
     */
    private String errorMessage(Job job) {
        return format(
                "@job name=[%s] id=[%s] ref=[%s]",
                job.getName() == null ? "" : job.getName(), job.getId(), job.getReference());
    }

    /**
     * Method to get a step error message
     *
     * @param step step
     * @return a pipeline error message
     */
    private String errorMessage(Step step) {
        return format(
                "@step name=[%s] id=[%s] ref=[%s]",
                step.getName() == null ? "" : step.getName(), step.getId(), step.getReference());
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

    /**
     * Method to check if a String is null or blank
     *
     * @param string string
     * @return true if the String is null or blank, else false
     */
    private boolean isNullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    /**
     * Method to check if a String is an invalid name
     *
     * @param string string
     * @return true of the String is an invalid name, else false
     */
    private boolean isInvalidName(String string) {
        return isNullOrBlank(string);
    }

    /**
     * Method to check if a String is an invalid id
     *
     * @param string string
     * @return true of the String is an invalid id, else false
     */
    private boolean isInvalidId(String string) {
        if (isNullOrBlank(string)) {
            return true;
        }

        return !string.matches(ID_REGEX);
    }
}
