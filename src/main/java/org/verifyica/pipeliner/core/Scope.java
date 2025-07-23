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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a scope in the execution context, which can hold properties, environment variables, and variables.
 */
public final class Scope {

    private final Map<String, String> environmentVariables;
    private final Map<String, String> variables;
    private Path workingDirectory;

    /**
     * Constructor
     */
    public Scope() {
        environmentVariables = new HashMap<>();
        variables = new HashMap<>();
    }

    /**
     * Sets the working directory for this scope.
     *
     * @param workingDirectory the working directory to set
     */
    public void setWorkingDirectory(Path workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Gets the working directory for this scope.
     *
     * @return the working directory as a Path
     */
    public Path getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Sets an environment variable in this scope.
     *
     * @param name the name of the environment variable
     * @param value the value of the environment variable
     */
    public void setEnvironmentVariable(String name, String value) {
        environmentVariables.put(name, value);
    }

    /**
     * Adds all environment variables to this scope.
     *
     * @param environmentVariables a map of environment variables to add
     */
    public void putAllEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables.putAll(environmentVariables);
    }

    /**
     * Retrieves an environment variable by its name.
     *
     * @param name the name of the environment variable to retrieve
     * @return the value of the environment variable, or an empty string if not found
     */
    public String getEnvironmentVariable(String name) {
        return environmentVariables.get(name);
    }

    /**
     * Retrieves all environment variables in this scope.
     *
     * @return a map of environment variables where the key is the variable name and the value is the variable value
     */
    public Map<String, String> getEnvironmentVariables() {
        return new HashMap<>(environmentVariables);
    }

    /**
     * Removes an environment variable from this scope.
     *
     * @param name the name of the environment variable to remove
     */
    public void removeEnvironmentVariable(String name) {
        environmentVariables.remove(name);
    }

    /**
     * Sets a variable in this scope.
     *
     * @param name the name of the variable
     * @param value the value of the variable
     */
    public void setVariable(String name, String value) {
        variables.put(name, value);
    }

    /**
     * Adds all variables to this scope.
     *
     * @param variables a map of variables to add
     */
    public void putAllVariables(Map<String, String> variables) {
        this.variables.putAll(variables);
    }

    /**
     * Retrieves a variable by its name.
     *
     * @param name the name of the variable to retrieve
     * @return the value of the variable, or an empty string if not found
     */
    public String getVariable(String name) {
        return variables.get(name);
    }

    /**
     * Removes a variable from this scope.
     *
     * @param key the name of the variable to remove
     */
    public void removeVariable(String key) {
        variables.remove(key);
    }
}
