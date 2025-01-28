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

package org.verifyica.pipeliner.core;

import static java.lang.String.format;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.common.Stopwatch;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.parser.Parser;

/** Class to implement Node */
public abstract class Node {

    private static final int MIN_TIMEOUT_MINUTES = 1;

    private static final int DEFAULT_TIMEOUT_MINUTES = 360;

    private static final int MAX_TIMEOUT_MINUTES = 4320;

    private static final Set<String> RESTRICTED_ENVIRONMENT_VARIABLES;

    static {
        RESTRICTED_ENVIRONMENT_VARIABLES = new HashSet<>();

        // Add restricted environment variables
        RESTRICTED_ENVIRONMENT_VARIABLES.add(Constants.PWD);
        RESTRICTED_ENVIRONMENT_VARIABLES.add(Constants.PIPELINER_HOME);
        RESTRICTED_ENVIRONMENT_VARIABLES.add(Constants.PIPELINER);
        RESTRICTED_ENVIRONMENT_VARIABLES.add(Constants.PIPELINER_IPC_IN);
        RESTRICTED_ENVIRONMENT_VARIABLES.add(Constants.PIPELINER_IPC_OUT);
    }

    private Node parent;
    private String name;
    private String id;
    private String enabled;
    private final Map<String, String> environmentVariables;
    private final Map<String, String> variables;
    private String workingDirectory;
    private String timeoutMinutes;
    private final Stopwatch stopwatch;

    /**
     * Constructor
     */
    public Node() {
        enabled = "true";
        variables = new LinkedHashMap<>();
        environmentVariables = new LinkedHashMap<>();
        timeoutMinutes = String.valueOf(DEFAULT_TIMEOUT_MINUTES);
        stopwatch = new Stopwatch();
    }

    /**
     * Method to set the parent
     *
     * @param parent the parent
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Method to get the parent
     *
     * @param parentClass the parent class
     * @return the parent
     * @param <T> the parent class
     */
    public <T> T getParent(Class<T> parentClass) {
        return parentClass.cast(parent);
    }

    /**
     * Method to set the id
     *
     * @param id the id
     */
    public void setId(String id) {
        if (id != null && !id.trim().isEmpty()) {
            this.id = id.trim();
        }
    }

    /**
     * Method to get the id
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Method to set the name
     *
     * @param name the name
     */
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
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
     * Method to set enabled
     *
     * @param enabled the enabled
     */
    public void setEnabled(String enabled) {
        if (enabled != null) {
            this.enabled = enabled.trim();
        }
    }

    /**
     * Method to get enabled
     *
     * @return enabled
     */
    public String getEnabled() {
        return enabled;
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
     * Method to set the working directory
     *
     * @param workingDirectory the working directory
     */
    public void setWorkingDirectory(String workingDirectory) {
        if (workingDirectory != null) {
            this.workingDirectory = workingDirectory.trim();
        }
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
     * Method to set the timeout minutes
     *
     * @param timeoutMinutes the timeout minutes
     */
    public void setTimeoutMinutes(String timeoutMinutes) {
        if (timeoutMinutes != null) {
            this.timeoutMinutes = timeoutMinutes.trim();
        }
    }

    /**
     * Method to set the timeout minutes
     *
     * @return the timeout minutes
     */
    public String getTimeoutMinutes() {
        return timeoutMinutes;
    }

    /**
     * Method to validate the node
     */
    public abstract void validate();

    /**
     * Method to execute the node
     *
     * @param context the context
     * @return the exit code
     */
    public abstract int execute(Context context);

    @Override
    public String toString() {
        if (id == null && name == null) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();

        if (id != null) {
            stringBuilder.append(" id=[").append(id).append("]");
        }

        if (name != null && !name.trim().isEmpty()) {
            stringBuilder.append(" name=[").append(name.trim()).append("]");
        }

        return stringBuilder.toString();
    }

    /**
     * Method to get the logger
     *
     * @return the logger
     */
    protected abstract Logger getLogger();

    /**
     * Method to get the stopwatch
     *
     * @return the stopwatch
     */
    protected Stopwatch getStopwatch() {
        return stopwatch;
    }

    /**
     * Method to validate id value
     */
    protected void validateId() {
        Logger logger = getLogger();

        String id = getId();

        if (id != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("validating id [%s]", id);
            }

            if (Id.isInvalid(id)) {
                throw new PipelineDefinitionException(format("%s -> id=[%s] is invalid", this, id));
            }
        }
    }

    /**
     * Method to validate the enabled value
     */
    protected void validateEnabled() {
        Logger logger = getLogger();

        String enabled = getEnabled();

        if (logger.isTraceEnabled()) {
            logger.trace("validating enabled [%s]", enabled);
        }

        if (enabled.isEmpty()) {
            throw new PipelineDefinitionException(
                    format("%s -> enabled=[%s] is invalid. Must be [true] or [false]", this, enabled));
        }

        if (Enabled.decode(enabled) == null) {
            throw new PipelineDefinitionException(
                    format("%s -> enabled=[%s] is invalid. Must be [true] or [false]", this, enabled));
        }
    }

    /**
     * Method to validate environment variables
     */
    protected void validateEnvironmentVariables() {
        Logger logger = getLogger();

        Map<String, String> environmentVariables = getEnvironmentVariables();

        if (logger.isTraceEnabled()) {
            logger.trace("validating environment variables ...");
        }

        if (!environmentVariables.isEmpty()) {
            environmentVariables.forEach((name, value) -> {
                if (logger.isTraceEnabled()) {
                    logger.trace("validating environment variable [%s] = [%s]", name, value);
                }

                // Check if the environment variable is restricted
                if (RESTRICTED_ENVIRONMENT_VARIABLES.contains(name)) {
                    throw new PipelineDefinitionException(
                            format("%s -> env=[%s] is a restricted environment variable", this, name));
                }

                if (name == null) {
                    throw new PipelineDefinitionException(format("%s -> env name is null", this));
                }

                if (EnvironmentVariable.isInvalid(name)) {
                    throw new PipelineDefinitionException(
                            format("%s -> env=[%s] is an invalid environment variable", this, name));
                }

                if (value == null) {
                    throw new PipelineDefinitionException(format("%s -> env=[%s] value is null", this, name));
                }

                try {
                    Parser.validate(value);
                } catch (Throwable t) {
                    throw new PipelineDefinitionException(
                            format("%s -> env=[%s] value=[%s] has syntax error", this, name, value));
                }
            });
        }
    }

    /**
     * Method to validate the variables
     */
    protected void validateVariables() {
        Logger logger = getLogger();

        Map<String, String> variables = getVariables();

        if (logger.isTraceEnabled()) {
            logger.trace("validating variables ...");
        }

        if (!variables.isEmpty()) {
            variables.forEach((name, value) -> {
                if (logger.isTraceEnabled()) {
                    logger.trace("validating variable [%s] = [%s]", name, value);
                }

                if (name == null) {
                    throw new PipelineDefinitionException(format("%s -> with name is null", this));
                }

                if (Variable.isInvalid(name)) {
                    throw new PipelineDefinitionException(format("%s -> with=[%s] is an invalid variable", this, name));
                }

                if (value == null) {
                    throw new PipelineDefinitionException(format("%s -> with=[%s] value is null", this, name));
                }

                try {
                    Parser.validate(value);
                } catch (Throwable t) {
                    throw new PipelineDefinitionException(
                            format("%s -> with=[%s] value=[%s] has syntax error", this, name, value));
                }
            });
        }
    }

    /**
     * Method to validate the working directory value
     */
    protected void validateWorkingDirectory() {
        Logger logger = getLogger();

        String workingDirectory = getWorkingDirectory();

        if (workingDirectory != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("validating working directory [%s]", workingDirectory);
            }

            if (workingDirectory.isEmpty()) {
                throw new PipelineDefinitionException(format("%s -> working-directory is blank", this));
            }

            try {
                Parser.validate(workingDirectory);
            } catch (Throwable t) {
                throw new PipelineDefinitionException(
                        format("%s -> working-directory=[%s] has syntax error", this, workingDirectory));
            }
        }
    }

    /**
     * Method to validate the timeout minutes value
     */
    protected void validateTimeoutMinutes() {
        Logger logger = getLogger();

        String timeoutMinutes = getTimeoutMinutes();

        if (logger.isTraceEnabled()) {
            logger.trace("validating timeout-minutes [%s]", timeoutMinutes);
        }

        if (timeoutMinutes != null) {
            if (timeoutMinutes.isEmpty()) {
                throw new PipelineDefinitionException(format("%s -> timeout-minutes is blank", this));
            }

            int intTimeoutMinutes;

            try {
                intTimeoutMinutes = Integer.parseInt(timeoutMinutes);
            } catch (NumberFormatException e) {
                throw new PipelineDefinitionException(format(
                        "%s -> timeout-minutes=[%s] must be an integer in the range %s to %s (inclusive)",
                        this, timeoutMinutes, MIN_TIMEOUT_MINUTES, MAX_TIMEOUT_MINUTES));
            }

            if (intTimeoutMinutes < MIN_TIMEOUT_MINUTES || intTimeoutMinutes > MAX_TIMEOUT_MINUTES) {
                throw new PipelineDefinitionException(format(
                        "%s -> timeout-minutes=[%s] must be an integer in the range %s to %s (inclusive)",
                        this, timeoutMinutes, MIN_TIMEOUT_MINUTES, MAX_TIMEOUT_MINUTES));
            }
        }
    }
}
