/*
 * Copyright (C) Pipeliner project authors and contributors
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.verifyica.pipeliner.model.support.EnvironmentVariable;
import org.verifyica.pipeliner.model.support.Variable;
import org.verifyica.pipeliner.support.MapSupport;
import org.verifyica.pipeliner.support.SetOf;

/**
 * Factory class to create Pipeline objects from a YAML file.
 */
@SuppressWarnings("unchecked")
public class PipelineFactory {

    /**
     * Constant for YAML true boolean value.
     */
    private static final Set<String> YAML_TRUE_VALUES = SetOf.of("true", "yes", "1", "on");

    /**
     * Constant for YAML false boolean value.
     */
    private static final Set<String> YAML_FALSE_VALUES = SetOf.of("false", "no", "0", "off");

    /**
     * Constructor
     */
    public PipelineFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to parse a Pipeline
     *
     * @param filename the filename
     * @return a pipeline
     * @throws IOException if an I/O error occurs
     */
    public Pipeline createPipeline(String filename) throws IOException {
        return createPipeline(new File(filename));
    }

    /**
     * Method to parse a Pipeline
     *
     * @param file  this file
     * @return a pipeline
     * @throws IOException if an I/O error occurs
     */
    public Pipeline createPipeline(File file) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
            return createPipeline(bufferedReader);
        }
    }

    /**
     * Method to parse a Pipeline
     *
     * @param reader the reader
     * @return a pipeline
     */
    public Pipeline createPipeline(Reader reader) {
        Pipeline pipeline = new Pipeline();

        LoadSettings loadSettings = LoadSettings.builder().build();
        AllStringsConstructor allStringsConstructor = new AllStringsConstructor(loadSettings);
        Load load = new Load(loadSettings, allStringsConstructor);
        Iterable<Object> iterable = load.loadAllFromReader(reader);
        Iterator<Object> iterator = iterable.iterator();

        if (iterable.iterator().hasNext()) {
            Map<String, Object> rootMap = (Map<String, Object>) iterator.next();
            Map<String, Object> pipelineMap = (Map<String, Object>) rootMap.get("pipeline");
            buildPipeline(pipelineMap, pipeline);
        }

        return pipeline;
    }

    /**
     * Method to process the pipeline map and populate the Pipeline object.
     *
     * @param pipelineMap the pipeline map
     * @param pipeline the pipeline object to populate
     */
    private void buildPipeline(Map<String, Object> pipelineMap, Pipeline pipeline) {
        // Build common node properties
        buildNode(pipelineMap, pipeline);

        List<Object> jobs = (List<Object>) pipelineMap.get("jobs");
        if (jobs != null) {
            buildJobs(jobs, pipeline);
        }
    }

    /**
     * Method to process the jobs list and populate the Pipeline object with Job objects.
     *
     * @param jobsList the list of jobs
     * @param pipeline the pipeline object to populate with jobs
     * @throws SyntaxException if a syntax error occurs in the YAML file
     */
    private void buildJobs(List<Object> jobsList, Pipeline pipeline) throws SyntaxException {
        List<Job> result = new ArrayList<>();

        for (Object o : jobsList) {
            Map<String, Object> jobMap = (Map<String, Object>) o;
            Job job = new Job();

            // Build common node properties
            buildNode(jobMap, job);

            List<Object> steps = (List<Object>) jobMap.get("steps");
            if (steps != null) {
                buildSteps(steps, job);
            }

            result.add(job);
        }

        pipeline.setJobs(result);
    }

    /**
     * Method to process the steps list and populate the Job object with Step objects.
     *
     * @param steps the list of steps
     * @param job the job object to populate with steps
     */
    private void buildSteps(List<Object> steps, Job job) {
        List<Step> result = new ArrayList<>();

        for (Object o : steps) {
            Map<String, Object> stepMap = (Map<String, Object>) o;
            Step step = new Step();

            // Build common node properties
            buildNode(stepMap, step);
            step.setRun((String) stepMap.get("run"));

            result.add(step);
        }

        job.setSteps(result);
    }

    private void buildNode(Map<String, Object> map, Node node) {
        MapSupport.ifPresent(map, "name")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new SyntaxException(node.getType().getValue() + " \"name\" is blank");
                    }
                    return s;
                })
                .ifPresent(node::setName);

        MapSupport.ifPresent(map, "description")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new IllegalArgumentException(node.getType().getValue() + " \"description\" is blank");
                    }
                    return s;
                })
                .ifPresent(node::setDescription);

        MapSupport.ifPresent(map, "if")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new IllegalArgumentException(node.getType().getValue() + " \"if\" is blank");
                    }
                    return s;
                })
                .ifPresent(node::setConditional);

        MapSupport.ifPresent(map, "enabled")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new IllegalArgumentException(node.getType().getValue() + " \"enabled\" is blank");
                    }
                    return s;
                })
                .map(value -> {
                    if (YAML_TRUE_VALUES.contains(value.toLowerCase())) {
                        return true;
                    } else if (YAML_FALSE_VALUES.contains(value.toLowerCase())) {
                        return false;
                    } else {
                        throw new SyntaxException(
                                node.getType().getValue() + " \"enabled\" [" + value + "] is invalid");
                    }
                })
                .ifPresent(node::setEnabled);

        MapSupport.ifPresent(map, "working-directory")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new IllegalArgumentException(
                                node.getType().getValue() + " \"working-directory\" is blank");
                    }
                    return s;
                })
                .ifPresent(node::setWorkingDirectory);

        MapSupport.ifPresent(map, "shell")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new IllegalArgumentException(node.getType().getValue() + " \"shell\" is blank");
                    }
                    return s;
                })
                .ifPresent(node::setShell);

        MapSupport.ifPresent(map, "timeout-minutes")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new IllegalArgumentException(node.getType().getValue() + " \"timeout-minutes\" is blank");
                    }
                    return s;
                })
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        throw new SyntaxException(
                                node.getType().getValue() + " \"timeout-minutes\" value [" + value + "] is invalid");
                    }
                })
                .ifPresent(node::setTimeoutMinutes);

        MapSupport.ifPresent(map, "env")
                .map(o -> {
                    if (!(o instanceof Map)) {
                        throw new SyntaxException(node.getType().getValue() + " \"env\" is not a map");
                    }

                    return (Map<String, String>) o;
                })
                .map(m -> {
                    if (m.isEmpty()) {
                        throw new SyntaxException(node.getType().getValue() + " \"env\" is empty");
                    }

                    return m;
                })
                .ifPresent(m -> {
                    for (Map.Entry<String, String> entry : m.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();

                        if (EnvironmentVariable.isInvalid(key)) {
                            throw new SyntaxException(node.getType().getValue() + " \"env\" environment variable name ["
                                    + key + "] is invalid");
                        }

                        if (value == null) {
                            value = "";
                        }

                        node.getEnvironmentVariables().put(key, value);
                    }
                });

        MapSupport.ifPresent(map, "with")
                .map(o -> {
                    if (!(o instanceof Map)) {
                        throw new SyntaxException(node.getType().getValue() + " \"with\" is not a map");
                    }

                    return (Map<String, String>) o;
                })
                .map(m -> {
                    if (m.isEmpty()) {
                        throw new SyntaxException(node.getType().getValue() + " \"with\" is empty");
                    }

                    return m;
                })
                .ifPresent(m -> {
                    for (Map.Entry<String, String> entry : m.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();

                        if (Variable.isInvalid(key)) {
                            throw new SyntaxException(
                                    node.getType().getValue() + " \"with\" variable name [" + key + "] is invalid");
                        }

                        if (value == null) {
                            value = "";
                        }

                        node.getVariables().put(key, value);
                    }
                });
    }

    /**
     * Constructor that overrides the default scalar constructor to treat all scalars as strings.
     */
    private static class AllStringsConstructor extends StandardConstructor {

        /**
         * Constructor
         *
         * @param loadSettings the load settings
         */
        public AllStringsConstructor(LoadSettings loadSettings) {
            super(loadSettings);
        }

        @Override
        protected Object constructObject(org.snakeyaml.engine.v2.nodes.Node node) {
            if (node instanceof ScalarNode) {
                // Treat all scalar nodes as strings
                return ((ScalarNode) node).getValue();
            }

            // For other node types, use the default behavior
            return super.constructObject(node);
        }
    }
}
