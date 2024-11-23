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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.MessageSupplier;
import org.verifyica.pipeliner.common.ValidatorException;
import org.verifyica.pipeliner.common.yaml.YamlConverter;
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
        step.getRuns().addAll(parseRun(console, step, stepMap.get("run")));

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
    private List<Run> parseRun(Console console, Step step, Object object) throws ValidatorException {
        List<Run> runs = new ArrayList<>();

        if (object instanceof String) {
            String string = ((String) object).trim();
            List<String> lines = splitOnCRLF(console, string);

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
    private List<String> splitOnCRLF(Console console, String string) {
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
