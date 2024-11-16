/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.pipeline.model;

import static java.lang.String.format;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.verifyica.pipeline.common.Console;
import org.verifyica.pipeline.common.StringConstructor;
import org.verifyica.pipeline.common.YamlConverter;
import org.verifyica.pipeline.common.YamlFormatException;
import org.verifyica.pipeline.common.YamlValueException;
import org.yaml.snakeyaml.Yaml;

/** Class to implement PipelineFactory */
@SuppressWarnings("unchecked")
public class PipelineFactory {

    // private static final String NAME_REGEX = "^[a-zA-Z0-9-_ .]*$";
    private static final String ENVIRONMENT_VARIABLE_REGEX = "^[a-zA-Z_]+$";
    private static final String SANITIZE_ENVIRONMENT_VARIABLE_REGEX = "[^A-Za-z0-9_]";

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
    public Pipeline createPipeline(String filename) throws YamlFormatException {
        console.trace("createPipeline filename [%s]", filename);

        Pipeline pipeline;

        try {
            Yaml yaml = new Yaml(new StringConstructor());

            try (InputStream inputStream = Files.newInputStream(Paths.get(filename))) {
                pipeline = parsePipeline(yaml.load(inputStream));
            }
        } catch (Throwable t) {
            throw new YamlFormatException("YAML format exception", t);
        }

        validatePipeline(pipeline);

        return pipeline;
    }

    private void validatePipeline(Pipeline pipeline) {
        console.trace("validatePipeline");

        if (isNullOrBlank(pipeline.getName())) {
            throw new YamlValueException(format("%s.name is blank", pipeline.getId()));
        }

        if (!isValidName(pipeline.getName())) {
            throw new YamlValueException(format("%s.name [%s] is invalid", pipeline.getId(), pipeline.getName()));
        }

        for (Map.Entry<String, String> entry :
                pipeline.getEnvironmentVariables().entrySet()) {
            if (!isValidEnvironmentVariable(entry.getKey())) {
                throw new YamlValueException(format("%s env/with [%s] is invalid", pipeline.getId(), entry.getKey()));
            }
        }

        for (Job job : pipeline.getJobs()) {
            if (isNullOrBlank(job.getName())) {
                throw new YamlValueException(format("%s.name is blank", job.getId()));
            }

            if (!isValidName(job.getName())) {
                throw new YamlValueException(format("%s.name [%s] is invalid", job.getId(), job.getName()));
            }

            for (Map.Entry<String, String> entry : job.getEnvironmentVariables().entrySet()) {
                if (!isValidEnvironmentVariable(entry.getKey())) {
                    throw new YamlValueException(format("%s env/with [%s] is invalid", job.getId(), entry.getKey()));
                }
            }

            for (Step step : job.getSteps()) {
                if (isNullOrBlank(step.getName())) {
                    throw new YamlValueException(format("%s.name is blank", step.getId()));
                }

                if (!isValidName(step.getName())) {
                    throw new YamlValueException(format("%s.name [%s] is invalid", step.getId(), step.getName()));
                }

                for (Map.Entry<String, String> entry :
                        step.getEnvironmentVariables().entrySet()) {
                    if (!isValidEnvironmentVariable(entry.getKey())) {
                        throw new YamlValueException(
                                format("%s env/with [%s] is invalid", step.getId(), entry.getKey()));
                    }
                }

                if (step.getShellType() == Step.ShellType.INVALID) {
                    throw new YamlValueException(format("%s.shell [%s] is invalid", step.getId(), step.getShellType()));
                }

                if (isNullOrBlank(step.getWorkingDirectory())) {
                    throw new YamlValueException(format("%s.working-directory is blank", step.getId()));
                }

                Run run = step.getRun();

                if (run == null) {
                    throw new YamlValueException(format("%s.run is required", step.getId()));
                }

                if (isNullOrBlank(run.getCommand())) {
                    throw new YamlValueException(format("%s.run is blank", step.getId()));
                }
            }
        }
    }

    private Pipeline parsePipeline(Map<Object, Object> map) {
        console.trace("parsePipeline");

        Map<Object, Object> pipelineMap = (Map<Object, Object>) map.get("pipeline");

        Pipeline pipeline = new Pipeline();
        pipeline.setName(YamlConverter.asString(pipelineMap.get("name")));
        pipeline.setEnabled(YamlConverter.asBoolean(pipelineMap.get("enabled"), true));
        pipeline.setEnvironmentVariables(parseEnv(YamlConverter.asMap(pipelineMap.get("env"))));
        pipeline.setEnvironmentVariables(parseWith(YamlConverter.asMap(pipelineMap.get("with"))));

        // System.out.printf("  name=[%s]%n", pipeline.getName());

        pipeline.setJobs(parseJobs(pipeline, YamlConverter.asList(pipelineMap.get("jobs"))));

        return pipeline;
    }

    private List<Job> parseJobs(Pipeline pipeline, List<Object> objects) {
        console.trace("parseJobs");

        List<Job> jobs = new ArrayList<>();

        for (Object object : objects) {
            jobs.add(parseJob(pipeline, object));
        }

        return jobs;
    }

    private Job parseJob(Pipeline pipeline, Object object) {
        console.trace("parseJob");

        Map<Object, Object> jobMap = YamlConverter.asMap(object);

        Job job = new Job(pipeline, ++jobIndex);
        job.setName(YamlConverter.asString(jobMap.get("name")));
        job.setEnabled(YamlConverter.asBoolean(jobMap.get("enabled"), true));
        job.setEnvironmentVariables(parseEnv(YamlConverter.asMap(jobMap.get("env"))));
        job.setEnvironmentVariables(parseWith(YamlConverter.asMap(jobMap.get("with"))));

        // System.out.printf("  name=[%s]%n", job.getName());

        job.setSteps(parseSteps(job, YamlConverter.asList(jobMap.get("steps"))));

        return job;
    }

    private List<Step> parseSteps(Job job, List<Object> objects) {
        console.trace("parseSteps");

        stepIndex = 0;

        List<Step> steps = new ArrayList<>();

        if (objects != null) {
            for (Object object : objects) {
                steps.add(parseStep(job, object));
            }
        }

        return steps;
    }

    private Step parseStep(Job job, Object object) {
        console.trace("parseStep");

        Map<Object, Object> stepMap = YamlConverter.asMap(object);

        Step step = new Step(job, ++stepIndex);
        step.setName(YamlConverter.asString(stepMap.get("name")));
        step.setEnabled(YamlConverter.asBoolean(stepMap.get("enabled"), true));
        step.setEnvironmentVariables(parseEnv(YamlConverter.asMap(stepMap.get("env"))));
        step.setEnvironmentVariables(parseWith(YamlConverter.asMap(stepMap.get("with"))));
        step.setShellType(parseShellType(YamlConverter.asString(stepMap.get("shell"))));
        step.setWorkingDirectory(YamlConverter.asString(stepMap.get("working-directory"), "."));
        step.setRun(parseRun(YamlConverter.asString(stepMap.get("run"))));

        return step;
    }

    private Map<String, String> parseEnv(Map<Object, Object> map) {
        console.trace("parseEnv");

        Map<String, String> properties = new LinkedHashMap<>();

        if (map != null) {
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                String key = entry.getKey()
                        .toString()
                        .trim()
                        .replaceAll(SANITIZE_ENVIRONMENT_VARIABLE_REGEX, "_")
                        .toUpperCase(Locale.US);
                String value =
                        entry.getValue() != null ? entry.getValue().toString().trim() : null;

                if (value != null) {
                    properties.put(key, value.trim());
                }
            }
        }

        return properties;
    }

    private Map<String, String> parseWith(Map<Object, Object> map) {
        console.trace("parseWith");

        Map<String, String> properties = new LinkedHashMap<>();

        if (map != null) {
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                String key = "INPUT_"
                        + entry.getKey()
                                .toString()
                                .trim()
                                .replaceAll(SANITIZE_ENVIRONMENT_VARIABLE_REGEX, "_")
                                .toUpperCase(Locale.US);
                String value =
                        entry.getValue() != null ? entry.getValue().toString().trim() : null;

                if (value != null) {
                    properties.put(key, value.trim());
                }
            }
        }

        return properties;
    }

    private Step.ShellType parseShellType(String string) {
        console.trace("parseShellType [%s]", string);

        Step.ShellType shellType;

        if (string == null || string.trim().isEmpty()) {
            shellType = Step.ShellType.UNSPECIFIED;
        } else {
            if (string.trim().equals("bash")) {
                shellType = Step.ShellType.BASH;
            } else if (string.trim().equals("sh")) {
                shellType = Step.ShellType.SH;
            } else {
                shellType = Step.ShellType.INVALID;
            }
        }

        return shellType;
    }

    private Run parseRun(String string) {
        return string != null ? new Run(string) : null;
    }

    private boolean isNullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    private boolean isValidName(String string) {
        if (isNullOrBlank(string)) {
            return false;
        }

        return true;
        // return string.matches(NAME_REGEX);
    }

    private boolean isValidEnvironmentVariable(String string) {
        if (isNullOrBlank(string)) {
            return false;
        }

        return string.matches(ENVIRONMENT_VARIABLE_REGEX);
    }
}
