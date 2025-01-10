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

import java.util.LinkedHashMap;
import java.util.Map;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.tokenizer.Tokenizer;

/** Class to implement Model */
public abstract class Model {

    private static final Logger LOGGER = LoggerFactory.getLogger(Model.class);

    private static final int MIN_TIMEOUT_MINUTES = 1;

    private static final int DEFAULT_TIMEOUT_MINUTES = 360;

    private static final int MAX_TIMEOUT_MINUTES = 4320;

    private Model parent;
    private String name;
    private String id;
    private String enabled;
    private final Map<String, String> env;
    private final Map<String, String> with;
    private String workingDirectory;
    private String timeoutMinutes;

    /** Constructor */
    public Model() {
        enabled = "true";
        with = new LinkedHashMap<>();
        env = new LinkedHashMap<>();
        timeoutMinutes = String.valueOf(DEFAULT_TIMEOUT_MINUTES);
    }

    /**
     * Method to set the parent
     *
     * @param parent parent
     */
    public void setParent(Model parent) {
        this.parent = parent;
    }

    /**
     * Method to get the parent
     *
     * @return the parent
     */
    public Model getParent() {
        return parent;
    }

    /**
     * Method to set the name
     *
     * @param name the name
     */
    public void setName(String name) {
        if (name != null) {
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
     * Method to set the id
     *
     * @param id id
     */
    public void setId(String id) {
        if (id != null) {
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
     * Method to set enabled
     *
     * @param enabled enabled
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
     * Method to set the with Map
     *
     * @param with with
     */
    public void setWith(Map<String, String> with) {
        if (with != null) {
            this.with.clear();
            this.with.putAll(with);
        }
    }

    /**
     * Method to get the with Map
     *
     * @return the with Map
     */
    public Map<String, String> getWith() {
        return with;
    }

    /**
     * Method to set the env Map
     *
     * @param env env
     */
    public void setEnv(Map<String, String> env) {
        if (env != null) {
            this.env.clear();
            this.env.putAll(env);
        }
    }

    /**
     * Method to get the env Map
     *
     * @return the env Map
     */
    public Map<String, String> getEnv() {
        return env;
    }

    /**
     * Method to set the working directory
     *
     * @param workingDirectory workingDirectory
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
     * @param timeoutMinutes timeoutMinutes
     */
    public void setTimeoutMinutes(String timeoutMinutes) {
        if (timeoutMinutes != null) {
            this.timeoutMinutes = timeoutMinutes.trim();
        }
    }

    /**
     * Method to set the timeout minutes
     *
     * @return the timeoutMinutes
     */
    public String getTimeoutMinutes() {
        return timeoutMinutes;
    }

    /**
     * Method to validate the model
     */
    protected abstract void validate();

    /**
     * Method to validate the name value
     */
    protected void validateName() {
        String name = getName();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("validating name [%s]", name);
        }

        if (name == null) {
            throw new PipelineDefinitionException(format("%s -> name is null", this));
        }

        if (name.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> name is blank", this));
        }
    }

    /**
     * Method to validate id value
     */
    protected void validateId() {
        String id = getId();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("validating id [%s]", id);
        }

        if (id != null) {
            if (id.isEmpty()) {
                throw new PipelineDefinitionException(format("%s -> id is blank", this));
            }

            if (!Id.isValid(id)) {
                throw new PipelineDefinitionException(format("%s -> id=[%s] is an invalid id", this, id));
            }
        }
    }

    /**
     * Method to validate the enabled value
     */
    protected void validateEnabled() {
        String enabled = getEnabled();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("validating enabled [%s]", enabled);
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
     * Method to validate env Map keys/values
     */
    protected void validateEnv() {
        Map<String, String> envMap = getEnv();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("validating env ...");
        }

        if (!envMap.isEmpty()) {
            envMap.forEach((key, value) -> {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("validating key [%s] value [%s]", key, value);
                }

                if (key == null) {
                    throw new PipelineDefinitionException(format("%s -> env key is null", this));
                }

                if (!EnvironmentVariable.isValid(key)) {
                    throw new PipelineDefinitionException(
                            format("%s -> env=[%s] is an invalid environment variable", this, key));
                }

                if (value == null) {
                    throw new PipelineDefinitionException(format("%s -> env=[%s] value is null", this, key));
                }

                try {
                    Tokenizer.validate(value);
                } catch (Throwable t) {
                    throw new PipelineDefinitionException(
                            format("%s -> env=[%s] value=[%s] has syntax error", this, key, value));
                }
            });
        }
    }

    /**
     * Method to validate the with Map keys/values
     */
    protected void validateWith() {
        Map<String, String> withMap = getWith();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("validating with ...");
        }

        if (!withMap.isEmpty()) {
            withMap.forEach((key, value) -> {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("validating key [%s] value [%s]", key, value);
                }

                if (key == null) {
                    throw new PipelineDefinitionException(format("%s -> with key is null", this));
                }

                if (!Property.isValid(key)) {
                    throw new PipelineDefinitionException(format("%s -> with=[%s] is an invalid property", this, key));
                }

                if (value == null) {
                    throw new PipelineDefinitionException(format("%s -> with=[%s] value is null", this, key));
                }

                try {
                    Tokenizer.validate(value);
                } catch (Throwable t) {
                    throw new PipelineDefinitionException(
                            format("%s -> with=[%s] value=[%s] has syntax error", this, key, value));
                }
            });
        }
    }

    /**
     * Method to validate the working directory value
     */
    protected void validateWorkingDirectory() {
        String workingDirectory = getWorkingDirectory();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("validating working directory [%s]", workingDirectory);
        }

        if (workingDirectory != null) {
            if (workingDirectory.isEmpty()) {
                throw new PipelineDefinitionException(format("%s -> working-directory is blank", this));
            }

            try {
                Tokenizer.validate(workingDirectory);
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
        String timeoutMinutes = getTimeoutMinutes();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("validating timeout Minutes [%s]", timeoutMinutes);
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("name=[").append(name).append("]");

        if (getId() != null) {
            stringBuilder.append(" id=[").append(getId()).append("]");
        }

        return stringBuilder.toString();
    }
}
