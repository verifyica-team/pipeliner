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

/** Class to implement Model */
public abstract class Model {

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
        timeoutMinutes = "360";
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
     * @param name name
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
            this.workingDirectory = workingDirectory;
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
            this.timeoutMinutes = timeoutMinutes;
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
     * Method to validate the name
     */
    protected void validateName() {
        if (getName() == null) {
            throw new PipelineDefinitionException(format("%s -> name is null", this));
        }

        if (getName().trim().isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> name is blank", this));
        }
    }

    /**
     * Method to validate the model id
     */
    protected void validateId() {
        if (getId() != null) {
            if (getId().isEmpty()) {
                throw new PipelineDefinitionException(format("%s -> id is blank", this));
            }

            if (!Id.isValid(getId())) {
                throw new PipelineDefinitionException(format("%s -> id=[%s] is an invalid id", this, getId()));
            }
        }
    }

    /**
     * Method to validate model enabled
     */
    protected void validateEnabled() {
        if (getEnabled().isEmpty()) {
            throw new PipelineDefinitionException(
                    format("%s -> enabled=[%s] is invalid. Must be [true] or [false]", this, getEnabled()));
        }

        if (Enabled.decode(getEnabled()) == null) {
            throw new PipelineDefinitionException(
                    format("%s -> enabled=[%s] is invalid. Must be [true] or [false]", this, getEnabled()));
        }
    }

    /**
     * Method to validate the model env Map
     */
    protected void validateEnv() {
        if (!getEnv().isEmpty()) {
            getEnv().forEach((key, value) -> {
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
            });
        }
    }

    /**
     * Method to validate the model with Map
     */
    protected void validateWith() {
        if (!getWith().isEmpty()) {
            getWith().forEach((key, value) -> {
                if (key == null) {
                    throw new PipelineDefinitionException(format("%s -> with key is null", this));
                }

                if (!Property.isValid(key)) {
                    throw new PipelineDefinitionException(format("%s -> with=[%s] is an invalid property", this, key));
                }

                if (value == null) {
                    throw new PipelineDefinitionException(format("%s -> with=[%s] value is null", this, key));
                }
            });
        }
    }

    /**
     * Method to validate the model working directory
     */
    protected void validateWorkingDirectory() {
        if (getWorkingDirectory() != null) {
            if (getWorkingDirectory().trim().isEmpty()) {
                throw new PipelineDefinitionException(format("%s -> working-directory is blank", this));
            }

            setWorkingDirectory(getWorkingDirectory().trim());
        }
    }

    /**
     * Method to validate the model timeout minutes
     */
    protected void validateTimeoutMinutes() {
        if (getTimeoutMinutes() != null) {
            if (getTimeoutMinutes().trim().isEmpty()) {
                throw new PipelineDefinitionException(format("%s -> timeout-minutes is blank", this));
            }

            long timeoutMinutes;

            try {
                timeoutMinutes = Long.parseLong(getTimeoutMinutes().trim());
            } catch (Throwable t) {
                throw new PipelineDefinitionException(
                        format("%s -> timeout-minutes=[%s] is not a valid integer", this, getTimeoutMinutes()));
            }

            if (timeoutMinutes < 1 || timeoutMinutes > Integer.MAX_VALUE) {
                throw new PipelineDefinitionException(format(
                        "%s -> timeout-minutes=[%s] must be in the inclusive range 1 to 2147483647 (inclusive)",
                        this, getTimeoutMinutes()));
            }

            setTimeoutMinutes(getTimeoutMinutes().trim());
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
