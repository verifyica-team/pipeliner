/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.common;

import static java.lang.String.format;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/** Class to implement GroovyScriptEngine */
public class GroovyScriptEngine {

    /** Enum to implement Result */
    public enum Result {
        /** PIPELINER_CONTINUE */
        PIPELINER_CONTINUE,
        /** PIPELINER_ABORT */
        PIPELINER_ABORT,
        /** PIPELINER_ERROR */
        PIPELINER_ERROR
    }

    // Define constants in Java
    private static final String PIPELINER_CONTINUE = "PIPELINER_CONTINUE";
    private static final String PIPELINER_ABORT = "PIPELINER_ABORT";
    private static final String PIPELINER_ERROR = "PIPELINER_ERROR";

    private static final Map<String, Result> RESULT_MAP = new HashMap<>();

    static {
        RESULT_MAP.put(PIPELINER_CONTINUE, Result.PIPELINER_CONTINUE);
        RESULT_MAP.put(PIPELINER_ABORT, Result.PIPELINER_ABORT);
        RESULT_MAP.put(PIPELINER_ERROR, Result.PIPELINER_ERROR);
    }

    /** Constructor */
    private GroovyScriptEngine() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to execute a Groovy script
     *
     * @param script Groovy script
     * @param environmentVariables environment variables
     * @param properties properties
     * @return the result
     * @throws GroovyScriptEngineException if the Groovy script execution fails
     */
    public static Result executeScript(
            String script, Map<String, String> environmentVariables, Map<String, String> properties)
            throws GroovyScriptEngineException {
        Object result;

        try {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            Binding binding = new Binding();

            // Bind the constants
            binding.setVariable(PIPELINER_CONTINUE, PIPELINER_CONTINUE);
            binding.setVariable(PIPELINER_ABORT, PIPELINER_ABORT);
            binding.setVariable(PIPELINER_ERROR, PIPELINER_ERROR);

            // Bind the environment variables
            binding.setVariable("env", environmentVariables);
            binding.setVariable("with", properties);

            // Bind the PrintWriter to the binding
            binding.setVariable("out", printWriter);

            // Create a Groovy shell
            GroovyShell shell = new GroovyShell(binding);

            BufferedReader bufferedReader = new BufferedReader(new StringReader(stringWriter.toString()));

            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }

                System.out.printf("> %s%n", line);
            }

            // Execute the script
            result = shell.evaluate(script);
        } catch (Throwable t) {
            throw new GroovyScriptEngineException("Groovy script execution failed", t);
        }

        // If the result is null
        if (result == null) {
            // Assume the result is PIPELINER_CONTINUE
            return Result.PIPELINER_CONTINUE;
        }

        // If the result is not a string, throw an exception
        if (!(result instanceof String)) {
            throw new GroovyScriptEngineException(format("Groovy script return invalid value [%s]", result));
        }

        // Return the mapped result
        return RESULT_MAP.get(result.toString());
    }
}
