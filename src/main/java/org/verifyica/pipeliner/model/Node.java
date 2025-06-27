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

import java.util.LinkedHashMap;
import java.util.Map;

/** Class to implement Node */
public abstract class Node {

    /**
     * Constant for the default timeout in minutes.
     */
    private static final int DEFAULT_TIMEOUT_MINUTES = 360;

    /**
     * Enum to represent the type of Node
     */
    public enum Type {

        /**
         * Pipeline type
         */
        PIPELINE("pipeline"),

        /**
         * Job type
         */
        JOB("job"),

        /**
         * Step type
         */
        STEP("step");

        /**
         * The string representation of the type
         */
        private final String value;

        /**
         * Constructor for Type enum
         *
         * @param value the string representation of the type
         */
        Type(String value) {
            this.value = value;
        }

        /**
         * Method to get the string representation of the type
         *
         * @return the string representation of the type
         */
        public String getValue() {
            return value;
        }
    }

    private final Type type;
    private Boolean enabled;
    private String name;
    private String description;
    private String conditional;
    private String workingDirectory;
    private String shell;
    private Integer timeoutMinutes;
    private final Map<String, String> environmentVariables;
    private final Map<String, String> variables;

    /**
     * Constructor
     *
     * @param type the type of Node
     */
    protected Node(Type type) {
        this.type = type;
        this.enabled = true;
        this.timeoutMinutes = DEFAULT_TIMEOUT_MINUTES;
        this.environmentVariables = new LinkedHashMap<>();
        this.variables = new LinkedHashMap<>();
    }

    /**
     * Method to get the type
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Method to set the name
     *
     * @param name the name
     */
    public void setName(String name) {
        if (name != null) {
            this.name = name.replaceAll("[\r\n\t]", " ").trim().replaceAll(" +", " ");
        }
    }

    /**
     * Method to get the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Method to set the description
     *
     * @param description the description
     */
    public void setDescription(String description) {
        if (description != null) {
            this.description = description.replaceAll("[\r\n\t]", " ").trim().replaceAll(" +", " ");
        }
    }

    /**
     * Method to get the description
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Method to set enabled
     *
     * @param enabled the enabled
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Method to get enabled
     *
     * @return enabled
     */
    public Boolean isEnabled() {
        return enabled;
    }

    /**
     * Method to set the conditional
     *
     * @param conditional the conditional
     */
    public void setConditional(String conditional) {
        this.conditional = conditional;
    }

    /**
     * Method to get the conditional
     *
     * @return the conditional
     */
    public String getConditional() {
        return conditional;
    }

    /**
     * Method to set the working directory
     *
     * @param workingDirectory the working directory
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Method to get the working directory
     *
     * @return the working directory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Method to set the shell
     *
     * @param shell the shell
     */
    public void setShell(String shell) {
        this.shell = shell;
    }

    /**
     * Method to get the shell
     *
     * @return the shell
     */
    public String getShell() {
        return shell;
    }

    /**
     * Method to set the timeout minutes
     *
     * @param timeoutMinutes the timeout minutes
     */
    public void setTimeoutMinutes(Integer timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }

    /**
     * Method to set the timeout minutes
     *
     * @return the timeout minutes
     */
    public Integer getTimeoutMinutes() {
        return timeoutMinutes;
    }

    /**
     * Method to set the environment variables
     *
     * @param environmentVariables the environment variables
     */
    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        if (environmentVariables != null) {
            this.environmentVariables.clear();
            this.environmentVariables.putAll(environmentVariables);
        }
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
     * Method to set the variables
     *
     * @param variables the variables
     */
    public void setVariables(Map<String, String> variables) {
        if (variables != null) {
            this.variables.clear();
            this.variables.putAll(variables);
        }
    }

    /**
     * Method to get the variables
     *
     * @return the variables
     */
    public Map<String, String> getVariables() {
        return variables;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("type [").append(getType()).append("]");

        String name = getName();
        if (name != null && !name.isBlank()) {
            stringBuilder.append(" name [").append(name.trim()).append("]");
        }

        String description = getDescription();
        if (description != null && !description.isBlank()) {
            stringBuilder.append(" description [").append(description.trim()).append("]");
        }

        return stringBuilder.toString();
    }
}
