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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class to implement Base */
public abstract class Base {

    private Base parent;
    private String name;
    private String id;
    private String enabled;
    private Map<String, String> env;
    private Map<String, String> with;
    private Map<String, String> opt;
    private String workingDirectory;

    /** Constructor */
    public Base() {
        enabled = "true";
        with = new LinkedHashMap<>();
        env = new LinkedHashMap<>();
        opt = new LinkedHashMap<>();
        workingDirectory = ".";
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
        this.name = name;
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
        this.id = id;
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
        this.enabled = enabled;
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
        this.with.clear();
        this.with.putAll(with);
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
        this.env.clear();
        this.env.putAll(env);
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
     * Method to set the opt Map
     *
     * @param opt opt
     */
    public void setOpt(Map<String, String> opt) {
        this.opt.clear();
        this.opt.putAll(opt);
    }

    /**
     * Method to get the opt Map
     *
     * @return the opt Map
     */
    public Map<String, String> getOpt() {
        return opt;
    }

    /**
     * Method to set the working directory
     *
     * @param workingDirectory workingDirectory
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
            throw new ModeDefinitionException(format("%s name is null", base));
        }

        if (base.getName().trim().isEmpty()) {
            throw new ModeDefinitionException(format("%s name is blank", base));
        }
    }

    /**
     * Method to validate the id
     *
     * @param base base
     */
    protected static void validateId(Base base) {
        if (base.getId() == null) {
            throw new ModeDefinitionException(format("%s id is null", base));
        }

        if (base.getId().trim().isEmpty()) {
            throw new ModeDefinitionException(format("%s id is blank", base));
        }
    }

    /**
     * Method to validate the env Map
     *
     * @param base base
     */
    protected static void validateEnv(Base base) {
        if (!base.getEnv().isEmpty()) {
            String regex = "^[A-Za-z_][A-Za-z0-9_]*$";
            Matcher matcher = Pattern.compile(regex).matcher("");

            base.getEnv().forEach((key, value) -> {
                if (key == null) {
                    throw new ModeDefinitionException(format("%s env key is null", base));
                }

                matcher.reset(key);
                if (!matcher.find()) {
                    throw new ModeDefinitionException(format("%s env=[%s] is invalid", base, key));
                }

                if (value == null) {
                    throw new ModeDefinitionException(format("%s env=[%s] value is null", base, key));
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
            String regex = "^[A-Za-z0-9][A-Za-z0-9-_\\.]*$";
            Matcher matcher = Pattern.compile(regex).matcher("");

            base.getWith().forEach((key, value) -> {
                if (key == null) {
                    throw new ModeDefinitionException(format("%s with key is null", base));
                }

                matcher.reset(key);
                if (!matcher.find()) {
                    throw new ModeDefinitionException(format("%s with=[%s] is invalid", base, key));
                }

                if (value == null) {
                    throw new ModeDefinitionException(format("%s with=[%s] value is null", base, key));
                }
            });
        }
    }

    /**
     * Method to validate the opt Map
     *
     * @param base base
     */
    protected static void validateOpt(Base base) {
        if (!base.getOpt().isEmpty()) {
            String regex = "^[A-Za-z0-9][A-Za-z0-9-_\\.]*$";
            Matcher matcher = Pattern.compile(regex).matcher("");

            base.getOpt().forEach((key, value) -> {
                if (key == null) {
                    throw new ModeDefinitionException(format("%s opt key is null", base));
                }

                matcher.reset(key);
                if (!matcher.find()) {
                    throw new ModeDefinitionException(format("%s opt=[%s] is invalid", base, key));
                }

                if (value == null) {
                    throw new ModeDefinitionException(format("%s opt=[%s] value is null", base, key));
                }
            });
        }
    }

    @Override
    public String toString() {
        return "name=[" + name + "] id=[" + id + "]";
    }
}
