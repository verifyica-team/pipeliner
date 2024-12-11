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
import org.verifyica.pipeliner.model.parser.YamlDefinitionException;
import org.verifyica.pipeliner.model.support.Enabled;
import org.verifyica.pipeliner.model.support.EnvironmentVariable;
import org.verifyica.pipeliner.model.support.Id;
import org.verifyica.pipeliner.model.support.Property;

/** Class to implement Base */
public abstract class Base {

    private Base parent;
    private String name;
    private String id;
    private String enabled;
    private final Map<String, String> env;
    private final Map<String, String> with;
    private String workingDirectory;

    /** Constructor */
    public Base() {
        enabled = "true";
        with = new LinkedHashMap<>();
        env = new LinkedHashMap<>();
    }

    /**
     * Method to set the parent
     *
     * @param parent parent
     */
    public void setParent(Base parent) {
        this.parent = parent;
    }

    /**
     * Method to get the parent
     *
     * @return the parent
     */
    public Base getParent() {
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
     * Method to validate
     */
    protected abstract void validate();

    /**
     * Method to validate the name
     *
     * @param base base
     */
    protected static void validateName(Base base) {
        if (base.getName() == null) {
            throw new YamlDefinitionException(format("%s -> name is null", base));
        }

        if (base.getName().trim().isEmpty()) {
            throw new YamlDefinitionException(format("%s -> name is blank", base));
        }
    }

    /**
     * Method to validate the id
     *
     * @param base base
     */
    protected static void validateId(Base base) {
        if (base.getId() != null) {
            if (base.getId().isEmpty()) {
                throw new YamlDefinitionException(format("%s -> id is blank", base));
            }

            if (!Id.isValid(base.getId())) {
                throw new YamlDefinitionException(format("%s -> id=[%s] is not a valid id", base, base.getId()));
            }
        }
    }

    /**
     * Method to validate enabled
     *
     * @param base base
     */
    protected static void validateEnabled(Base base) {
        if (base.getEnabled().isEmpty()) {
            throw new YamlDefinitionException(
                    format("%s -> enabled=[%s] is not valid. Must be [true] or [false]", base, base.getEnabled()));
        }

        if (Enabled.decodeEnabled(base.getEnabled()) == null) {
            throw new YamlDefinitionException(
                    format("%s -> enabled=[%s] is not a valid. Must be [true] or [false]", base, base.getEnabled()));
        }
    }

    /**
     * Method to validate the env Map
     *
     * @param base base
     */
    protected static void validateEnv(Base base) {
        if (!base.getEnv().isEmpty()) {
            base.getEnv().forEach((key, value) -> {
                if (key == null) {
                    throw new YamlDefinitionException(format("%s -> env key is null", base));
                }

                if (!EnvironmentVariable.isValid(key)) {
                    throw new YamlDefinitionException(
                            format("%s -> env=[%s] is not a valid environment variable", base, key));
                }

                if (value == null) {
                    throw new YamlDefinitionException(format("%s -> env=[%s] value is null", base, key));
                }
            });
        }
    }

    /**
     * Method to validate the with Map
     *
     * @param base base
     */
    protected static void validateWith(Base base) {
        if (!base.getWith().isEmpty()) {
            base.getWith().forEach((key, value) -> {
                if (key == null) {
                    throw new YamlDefinitionException(format("%s -> with key is null", base));
                }

                if (!Property.isValid(key)) {
                    throw new YamlDefinitionException(format("%s -> with=[%s] is not a valid property", base, key));
                }

                if (value == null) {
                    throw new YamlDefinitionException(format("%s -> with=[%s] value is null", base, key));
                }
            });
        }
    }

    /**
     * Method to validate the working directory
     *
     * @param base base
     */
    protected static void validateWorkingDirectory(Base base) {
        if (base.getWorkingDirectory() != null) {
            if (base.getWorkingDirectory().trim().isEmpty()) {
                throw new YamlDefinitionException(format("%s -> working-directory is blank", base));
            }

            base.setWorkingDirectory(base.getWorkingDirectory().trim());
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
