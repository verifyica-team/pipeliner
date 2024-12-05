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

package org.verifyica.pipeliner.core2.model;

import static java.lang.String.format;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Base {

    private Base parent;
    private String name;
    private String id;
    private String enabled;
    private Map<String, String> env;
    private Map<String, String> with;
    private Map<String, String> opt;
    private String workingDirectory;

    public Base() {
        enabled = "true";
        with = new LinkedHashMap<>();
        env = new LinkedHashMap<>();
        opt = new LinkedHashMap<>();
        workingDirectory = ".";
    }

    public void setParent(Base parent) {
        this.parent = parent;
    }

    public Base getParent() {
        return parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setWith(Map<String, String> with) {
        this.with.clear();
        this.with.putAll(with);
    }

    public Map<String, String> getWith() {
        return with;
    }

    public void setEnv(Map<String, String> env) {
        this.env.clear();
        this.env.putAll(env);
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setOpt(Map<String, String> opt) {
        this.opt.clear();
        this.opt.putAll(opt);
    }

    public Map<String, String> getOpt() {
        return opt;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    protected abstract void validate();

    protected static void validateName(Base base) {
        if (base.getName() == null) {
            throw new ModeDefinitionException(format("%s name is null", base));
        }

        if (base.getName().trim().isEmpty()) {
            throw new ModeDefinitionException(format("%s name is blank", base));
        }
    }

    protected static void validateId(Base base) {
        if (base.getId() == null) {
            throw new ModeDefinitionException(format("%s id is null", base));
        }

        if (base.getId().trim().isEmpty()) {
            throw new ModeDefinitionException(format("%s id is blank", base));
        }
    }

    protected static void validateEnv(Base base) {
        base.getEnv().entrySet().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();

            // TODO validate key is a proper environment variable name

            if (value == null) {
                throw new ModeDefinitionException(format("%s env=[%s] is null", base, key));
            }

            // TODO validate value is proper environment variable value
        });
    }

    protected static void validateWith(Base base) {
        base.getWith().entrySet().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();

            // TODO validate key is a property name

            if (value == null) {
                throw new ModeDefinitionException(format("%s with=[%s] is null", base, key));
            }
        });
    }

    protected static void validateOpt(Base base) {
        base.getOpt().entrySet().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value == null) {
                throw new ModeDefinitionException(format("%s opt=[%s] is null", base, key));
            }

            // TODO validate key is a proper name
        });
    }

    @Override
    public String toString() {
        return "name=[" + name + "] id=[" + id + "]";
    }
}
