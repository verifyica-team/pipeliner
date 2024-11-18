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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.yaml.YamlConverter;
import org.verifyica.pipeliner.yaml.YamlFormatException;
import org.verifyica.pipeliner.yaml.YamlStringConstructor;
import org.verifyica.pipeliner.yaml.YamlValueException;
import org.yaml.snakeyaml.Yaml;

/** Class to implement PipelineFactory */
@SuppressWarnings("unchecked")
public class PipelineFactory {

    private static final String ID_REGEX = "^[a-zA-Z0-9-_]*$";
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
        console.trace("creating pipeline ...");
        console.trace("filename [%s]", filename);

        Pipeline pipeline;

        try {
            console.trace("loading YAML ...");

            Yaml yaml = new Yaml(new YamlStringConstructor());

            try (InputStream inputStream = Files.newInputStream(Paths.get(filename))) {
                Map<Object, Object> pipelineMap = yaml.load(inputStream);
                pipeline = parsePipeline(pipelineMap);
            }
        } catch (YamlFormatException e) {
            throw e;
        } catch (Throwable t) {
            throw new YamlFormatException("filename [%s] contains invalid YAML / YAML tags", t);
        }

        validatePipeline(pipeline);

        return pipeline;
    }

    private void validatePipeline(Pipeline pipeline) {
        console.trace("validating pipeline ...");

        if (isNullOrBlank(pipeline.getName())) {
            throw new YamlValueException(format("%s ... name[] is blank", errorMessage(pipeline)));
        }

        if (!isValidName(pipeline.getName())) {
            throw new YamlValueException(
                    format("%s ... name[%s] is invalid", errorMessage(pipeline), pipeline.getName()));
        }

        if (!isValidId(pipeline.getId())) {
            throw new YamlValueException(format("%s ... id[%s] is invalid", errorMessage(pipeline), pipeline.getId()));
        }

        for (Map.Entry<String, String> entry :
                pipeline.getEnvironmentVariables().entrySet()) {
            if (!isValidEnvironmentVariable(entry.getKey())) {
                throw new YamlValueException(
                        format("%s ... env/with=[%s] is invalid", errorMessage(pipeline), entry.getKey()));
            }
        }

        for (Job job : pipeline.getJobs()) {
            // console.trace("validating job [%s]", job.getId());

            if (isNullOrBlank(job.getName())) {
                throw new YamlValueException(format("%s ... name[] is blank", errorMessage(job)));
            }

            if (!isValidName(job.getName())) {
                throw new YamlValueException(format("%s ... name[%s] is invalid", errorMessage(job), job.getName()));
            }

            if (!isValidId(job.getId())) {
                throw new YamlValueException(format("%s ... id[%s] is invalid", errorMessage(job), job.getId()));
            }

            for (Map.Entry<String, String> entry : job.getEnvironmentVariables().entrySet()) {
                if (!isValidEnvironmentVariable(entry.getKey())) {
                    throw new YamlValueException(
                            format("%s ... env/with=[%s] is invalid", errorMessage(job), entry.getKey()));
                }
            }

            for (Step step : job.getSteps()) {
                // console.trace("validating step [%s]", step.getId());

                if (isNullOrBlank(step.getName())) {
                    throw new YamlValueException(format("%s ... name[] is blank", errorMessage(step)));
                }

                if (!isValidName(step.getName())) {
                    throw new YamlValueException(
                            format("%s ... name[%s] is invalid", errorMessage(step), step.getName()));
                }

                if (!isValidId(step.getId())) {
                    throw new YamlValueException(format("%s ... id[%s] is invalid", errorMessage(step), step.getId()));
                }

                for (Map.Entry<String, String> entry :
                        step.getEnvironmentVariables().entrySet()) {
                    if (!isValidEnvironmentVariable(entry.getKey())) {
                        throw new YamlValueException(
                                format("%s ... env/with=[%s] is invalid", errorMessage(step), entry.getKey()));
                    }
                }

                if (step.getShellType() == Step.ShellType.INVALID) {
                    throw new YamlValueException(
                            format("%s ... shell=[%s] is invalid", errorMessage(step), step.getShellType()));
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

    private Pipeline parsePipeline(Map<Object, Object> map) {
        console.trace("parsing pipeline ...");

        Map<Object, Object> pipelineMap = (Map<Object, Object>) map.get("pipeline");

        if (pipelineMap == null) {
            throw new YamlFormatException("YAML file root element \"pipeline:\" is required");
        }

        Pipeline pipeline = new Pipeline();
        pipeline.setName(YamlConverter.asString(pipelineMap.get("name")));
        pipeline.setId(YamlConverter.asString(pipelineMap.get("id")));
        pipeline.setEnabled(YamlConverter.asBoolean(pipelineMap.get("enabled"), true));
        pipeline.setEnvironmentVariables(parseEnv(YamlConverter.asMap(pipelineMap.get("env"))));
        pipeline.setEnvironmentVariables(parseWith(YamlConverter.asMap(pipelineMap.get("with"))));

        // System.out.printf("  name[%s]%n", pipeline.getName());

        pipeline.setJobs(parseJobs(pipeline, YamlConverter.asList(pipelineMap.get("jobs"))));

        return pipeline;
    }

    private List<Job> parseJobs(Pipeline pipeline, List<Object> objects) {
        // console.trace("parsing jobs ...");

        List<Job> jobs = new ArrayList<>();

        for (Object object : objects) {
            jobs.add(parseJob(pipeline, object));
        }

        return jobs;
    }

    private Job parseJob(Pipeline pipeline, Object object) {
        // console.trace("parse steps ...");

        Map<Object, Object> jobMap = YamlConverter.asMap(object);

        Job job = new Job(pipeline, ++jobIndex);
        job.setName(YamlConverter.asString(jobMap.get("name")));
        job.setId(YamlConverter.asString(jobMap.get("id")));
        job.setEnabled(YamlConverter.asBoolean(jobMap.get("enabled"), true));
        job.setEnvironmentVariables(parseEnv(YamlConverter.asMap(jobMap.get("env"))));
        job.setEnvironmentVariables(parseWith(YamlConverter.asMap(jobMap.get("with"))));

        // System.out.printf("  name[%s]%n", job.getName());

        job.setSteps(parseSteps(job, YamlConverter.asList(jobMap.get("steps"))));

        return job;
    }

    private List<Step> parseSteps(Job job, List<Object> objects) {
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

    private Step parseStep(Job job, Object object) {
        // console.trace("parsing step ...");

        Map<Object, Object> stepMap = YamlConverter.asMap(object);

        Step step = new Step(job, ++stepIndex);
        step.setName(YamlConverter.asString(stepMap.get("name")));
        step.setId(YamlConverter.asString(stepMap.get("id")));
        step.setEnabled(YamlConverter.asBoolean(stepMap.get("enabled"), true));
        step.setEnvironmentVariables(parseEnv(YamlConverter.asMap(stepMap.get("env"))));
        step.setEnvironmentVariables(parseWith(YamlConverter.asMap(stepMap.get("with"))));
        step.setShellType(parseShellType(YamlConverter.asString(stepMap.get("shell"))));
        step.setWorkingDirectory(YamlConverter.asString(stepMap.get("working-directory"), "."));
        step.setRuns(parseRun(YamlConverter.asString(stepMap.get("run"))));

        return step;
    }

    private Map<String, String> parseEnv(Map<Object, Object> map) {
        // console.trace("parsing env ...");

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
        // console.trace("parsing with ...");

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
        // console.trace("parsing shell [%s] ...", string);

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

    private List<Run> parseRun(String string) {
        // console.trace("parsing run [%s]", string);

        List<Run> runs = new ArrayList<>();

        if (string == null) {
            return runs;
        }

        List<String> values = splitOnCRLF(string);
        if (values.isEmpty()) {
            return runs;
        }

        for (String command : values) {
            if (!command.trim().isEmpty()) {
                runs.add(new Run(command.trim()));
            }
        }

        return runs;
    }

    private String errorMessage(Pipeline pipeline) {
        return format(
                "@pipeline name[%s] id[%s] location[%s]",
                pipeline.getName() == null ? "" : pipeline.getName(), pipeline.getId(), pipeline.getLocation());
    }

    private String errorMessage(Job job) {
        return format(
                "@job name[%s] id[%s] location[%s]",
                job.getName() == null ? "" : job.getName(), job.getId(), job.getLocation());
    }

    private String errorMessage(Step step) {
        return format(
                "@step name[%s] id[%s] location[%s]",
                step.getName() == null ? "" : step.getName(), step.getId(), step.getLocation());
    }

    private List<String> splitOnCRLF(String string) {
        String regex = "(?<=\r\n|\n)(?=(?:(?:[^\"]*\"){2})*[^\"]*$)(?=(?:(?:[^\']*\'){2})*[^\']*$)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);

        List<String> tokens = new ArrayList<>();
        int start = 0;

        while (matcher.find()) {
            tokens.add(string.substring(start, matcher.start()).trim());
            start = matcher.end();
        }

        tokens.add(string.substring(start));

        return tokens;
    }

    private boolean isNullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    private boolean isValidName(String string) {
        return !isNullOrBlank(string);
    }

    private boolean isValidId(String string) {
        if (isNullOrBlank(string)) {
            return false;
        }

        return string.matches(ID_REGEX);
    }

    private boolean isValidEnvironmentVariable(String string) {
        if (isNullOrBlank(string)) {
            return false;
        }

        return string.matches(ENVIRONMENT_VARIABLE_REGEX);
    }
}
