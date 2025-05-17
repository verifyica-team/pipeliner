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

package org.verifyica.pipeliner.core;

import java.util.Map;
import java.util.TreeMap;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.Environment;
import org.verifyica.pipeliner.core.support.ExtensionManager;
import org.verifyica.pipeliner.parser.tokens.ParsedVariable;

/** Class to implement Context */
public class Context {

    private final Console console;
    private final Map<String, String> environmentVariables;
    private final Map<String, String> variables;
    private final ExtensionManager extensionManager;

    /**
     * Constructor
     *
     * @param console the console
     */
    public Context(Console console) {
        this.console = console;
        this.environmentVariables = new TreeMap<>(Environment.getenv());
        this.variables = new TreeMap<>();
        this.extensionManager = new ExtensionManager();
    }

    /**
     * Method to get the console
     *
     * @return the console
     */
    public Console getConsole() {
        return console;
    }

    /**
     * Method to get the environment variables
     *
     * @return the environment variables
     */
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * Method to get the variables
     *
     * @return the variables
     */
    public Map<String, String> getVariables() {
        return variables;
    }

    /**
     * Method to get the extension manager
     *
     * @return the extension manager
     */
    public ExtensionManager getExtensionManager() {
        return extensionManager;
    }

    /**
     * Method to set a variable with optional scopes
     *
     * <p>Ids may be null</p>
     *
     * @param name the name
     * @param value the value
     * @param pipelineId the pipeline id
     * @param jobId the job id
     * @param stepId the step id
     * @return this
     */
    public Context setVariable(String name, String value, String pipelineId, String jobId, String stepId) {
        // Add the unscoped variable
        variables.put(name, value);

        if (stepId != null) {
            // Add the step scoped variable
            variables.put(stepId + ParsedVariable.SCOPE_SEPARATOR + name, value);

            if (jobId != null) {
                // Add the job + step scoped variable
                variables.put(
                        jobId + ParsedVariable.SCOPE_SEPARATOR + stepId + ParsedVariable.SCOPE_SEPARATOR + name, value);

                if (pipelineId != null) {
                    // Add the pipeline + job + step scoped variable
                    variables.put(
                            pipelineId
                                    + ParsedVariable.SCOPE_SEPARATOR
                                    + jobId
                                    + ParsedVariable.SCOPE_SEPARATOR
                                    + stepId
                                    + ParsedVariable.SCOPE_SEPARATOR
                                    + name,
                            value);
                }
            }
        } else if (jobId != null) {
            // Add the job scoped variable
            variables.put(jobId + ParsedVariable.SCOPE_SEPARATOR + name, value);

            if (pipelineId != null) {
                // Add the pipeline + job scoped variable
                variables.put(
                        pipelineId + ParsedVariable.SCOPE_SEPARATOR + jobId + ParsedVariable.SCOPE_SEPARATOR + name,
                        value);
            }
        } else if (pipelineId != null) {
            // Add the pipeline scoped variable
            variables.put(pipelineId + ParsedVariable.SCOPE_SEPARATOR + name, value);
        }

        return this;
    }
}
