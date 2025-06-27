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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.model.support.EnvironmentVariable;
import org.verifyica.pipeliner.model.support.Variable;
import org.verifyica.pipeliner.support.MapSupport;
import org.verifyica.pipeliner.support.MultiLineParser;

/**
 * Factory class to create Pipeline objects from a YAML file.
 */
@SuppressWarnings("unchecked")
public class PipelineFactory {

    /**
     * Constant for YAML true boolean value.
     */
    private static final Set<String> YAML_TRUE_VALUES = Set.of("true", "yes", "1", "on");

    /**
     * Constant for YAML false boolean value.
     */
    private static final Set<String> YAML_FALSE_VALUES = Set.of("false", "no", "0", "off");

    /**
     * Set of reserved environment variable names that should not be used in the pipeline.
     */
    private static final Set<String> RESERVED_ENVIRONMENT_VARIABLES = Set.of(
            "PWD", // Current directory
            "HOME", // User's home directory
            "USER", // Username of the current user
            "LOGNAME", // Login name
            "SHELL", // User's default shell
            "PATH", // System path
            "TERM", // Terminal type
            "LANG", // Language/locale settings
            "DISPLAY", // X11 display identifier
            "XAUTHORITY", // X11 authentication file
            "LD_LIBRARY_PATH", // Library path
            "TMPDIR", // Temporary directory
            "SSH_CLIENT", // SSH client info (if in SSH session)
            "SSH_TTY", // SSH terminal
            "SSH_CONNECTION", // SSH connection info
            Constants.PIPELINER,
            Constants.PIPELINER_VERSION,
            Constants.PIPELINER_HOME,
            Constants.PIPELINER_IPC_IN,
            Constants.PIPELINER_IPC_IN_FILE_PREFIX,
            Constants.PIPELINER_IPC_OUT,
            Constants.PIPELINER_IPC_OUT_FILE_PREFIX);

    /**
     * Constructor
     */
    public PipelineFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to create a Pipeline from a file.
     *
     * @param filename the pipeline filename
     * @return a {@code Pipeline} object
     * @throws IOException if an I/O error occurs
     */
    public Pipeline createPipeline(String filename) throws IOException {
        return createPipeline(new File(filename));
    }

    /**
     * Method to create a Pipeline from a File.
     *
     * @param file the pipeline file
     * @return a @{code Pipeline} object
     * @throws IOException if an I/O error occurs
     */
    public Pipeline createPipeline(File file) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
            return createPipeline(bufferedReader);
        }
    }

    /**
     * Method to create a Pipeline from a Reader.
     *
     * @param reader the reader
     * @return a @{code Pipeline} object
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
     * @param pipeline the @{code Pipeline} object to populate
     */
    private void buildPipeline(Map<String, Object> pipelineMap, Pipeline pipeline) {
        // Build common node properties
        buildNode(pipelineMap, pipeline);

        MapSupport.ifPresent(pipelineMap, "jobs")
                .map(o -> {
                    if (o instanceof String) {
                        String value = (String) o;
                        if (value.isBlank()) {
                            return new ArrayList<>();
                        } else {
                            throw new SyntaxException(pipeline.getType().getValue() + " \"jobs:\" is not a list");
                        }
                    }
                    return o;
                })
                .map(o -> {
                    if (!(o instanceof List)) {
                        throw new SyntaxException(pipeline.getType().getValue() + " \"jobs:\" is not a list");
                    }
                    return (List<Object>) o;
                })
                .ifPresent(j -> buildJobs(j, pipeline));
    }

    /**
     * Method to process the jobs list and populate the Pipeline object with Job objects.
     *
     * @param jobsList the list of jobs
     * @param pipeline the @{code Pipeline} object to populate with jobs
     * @throws SyntaxException if a syntax error occurs in the YAML file
     */
    private void buildJobs(List<Object> jobsList, Pipeline pipeline) throws SyntaxException {
        List<Job> result = new ArrayList<>();

        for (Object o : jobsList) {
            Map<String, Object> jobMap = (Map<String, Object>) o;
            Job job = new Job();

            // Build common node properties
            buildNode(jobMap, job);

            MapSupport.ifPresent(jobMap, "steps")
                    .map(oo -> {
                        if (!(oo instanceof List)) {
                            throw new SyntaxException(job.getType().getValue() + " \"steps:\" is not a list");
                        }
                        return (List<Object>) oo;
                    })
                    .ifPresent(s -> {
                        buildSteps(s, job);
                    });

            result.add(job);
        }

        pipeline.setJobs(result);
    }

    /**
     * Method to process the steps list and populate the Job object with Step objects.
     *
     * @param steps the list of steps
     * @param job the {@code Job} object to populate with steps
     */
    private void buildSteps(List<Object> steps, Job job) {
        List<Step> result = new ArrayList<>();

        for (Object o : steps) {
            Map<String, Object> stepMap = (Map<String, Object>) o;
            Step step = new Step();

            // Build common node properties
            buildNode(stepMap, step);

            // Get the run text
            String run = (String) stepMap.get("run");

            if (run != null && !run.isBlank()) {
                // Parse the run text into commands
                List<String> commands = MultiLineParser.parse(run);

                // Set the commands in the step
                step.getCommands().addAll(commands);
            }

            result.add(step);
        }

        job.setSteps(result);
    }

    /**
     * Method to build common node properties for Pipeline, Job, and Step objects.
     *
     * @param map the map containing node properties
     * @param node the node object to populate with properties
     */
    private void buildNode(Map<String, Object> map, Node node) {
        MapSupport.ifPresent(map, "name")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new SyntaxException(node.getType().getValue() + " \"name:\" is blank");
                    }
                    return s;
                })
                .ifPresent(node::setName);

        MapSupport.ifPresent(map, "description")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new IllegalArgumentException(node.getType().getValue() + " \"description:\" is blank");
                    }
                    return s;
                })
                .ifPresent(node::setDescription);

        MapSupport.ifPresent(map, "if")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new IllegalArgumentException(node.getType().getValue() + " \"if:\" is blank");
                    }
                    return s;
                })
                .ifPresent(node::setConditional);

        MapSupport.ifPresent(map, "enabled")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new IllegalArgumentException(node.getType().getValue() + " \"enabled:\" is blank");
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
                                node.getType().getValue() + " \"enabled:\" [" + value + "] is invalid");
                    }
                })
                .ifPresent(node::setEnabled);

        MapSupport.ifPresent(map, "working-directory")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new IllegalArgumentException(
                                node.getType().getValue() + " \"working-directory:\" is blank");
                    }
                    return s;
                })
                .ifPresent(node::setWorkingDirectory);

        MapSupport.ifPresent(map, "shell")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new IllegalArgumentException(node.getType().getValue() + " \"shell:\" is blank");
                    }
                    return s;
                })
                .ifPresent(node::setShell);

        MapSupport.ifPresent(map, "timeout-minutes")
                .map(o -> (String) o)
                .map(s -> {
                    if (s.isBlank()) {
                        throw new IllegalArgumentException(
                                node.getType().getValue() + " \"timeout-minutes:\" is blank");
                    }
                    return s;
                })
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        throw new SyntaxException(
                                node.getType().getValue() + " \"timeout-minutes:\" value [" + value + "] is invalid");
                    }
                })
                .ifPresent(node::setTimeoutMinutes);

        MapSupport.ifPresent(map, "env")
                .map(o -> {
                    if (o instanceof String) {
                        String value = (String) o;
                        if (value.isBlank()) {
                            return new LinkedHashMap<>();
                        } else {
                            throw new SyntaxException(node.getType().getValue() + " \"env:\" is not a map");
                        }
                    }
                    return o;
                })
                .map(o -> {
                    if (!(o instanceof Map)) {
                        throw new SyntaxException(node.getType().getValue() + " \"env:\" is not a map");
                    }
                    return (Map<String, String>) o;
                })
                .map(m -> {
                    // Check for reserved environment variable names
                    for (String key : m.keySet()) {
                        if (RESERVED_ENVIRONMENT_VARIABLES.contains(key)) {
                            throw new SyntaxException(node.getType().getValue() + " \"env:\" environment variable ["
                                    + key + "] is reserved and cannot be used");
                        }
                    }
                    return m;
                })
                .ifPresent(m -> {
                    for (Map.Entry<String, String> entry : m.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();

                        if (EnvironmentVariable.isInvalid(key)) {
                            throw new SyntaxException(node.getType().getValue() + " \"env:\" environment variable ["
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
                    if (o instanceof String) {
                        String value = (String) o;
                        if (value.isBlank()) {
                            return new LinkedHashMap<>();
                        } else {
                            throw new SyntaxException(node.getType().getValue() + " \"with:\" is not a map");
                        }
                    }
                    return o;
                })
                .map(o -> {
                    if (!(o instanceof Map)) {
                        throw new SyntaxException(node.getType().getValue() + " \"with:\" is not a map");
                    }
                    return (Map<String, String>) o;
                })
                .ifPresent(m -> {
                    for (Map.Entry<String, String> entry : m.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();

                        if (Variable.isInvalid(key)) {
                            throw new SyntaxException(
                                    node.getType().getValue() + " \"with:\" variable name [" + key + "] is invalid");
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
