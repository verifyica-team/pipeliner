/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.pipeline;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Class to implement Job */
public class Job {

    private String name;
    private boolean enabled;
    private Map<String, String> environmentVariables;
    private Map<String, String> properties;
    private List<Step> steps;
    private int exitCode;

    /** Constructor */
    public Job() {
        initialize();
    }

    /**
     * Method to initialize the job
     */
    private void initialize() {
        name = UUID.randomUUID().toString();
        enabled = true;
        properties = new LinkedHashMap<>();
        steps = new ArrayList<>();
    }

    /**
     * Method to set the name
     *
     * @param name name
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
     * Method to set environment variables
     *
     * @param environmentVariables environmentVariables
     */
    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    /**
     * Method to get environment variables
     *
     * @return the map of environment variables
     */
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * Method to set the list of properties
     *
     * @param properties properties
     */
    public void setProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    /**
     * Method to get the map of properties
     *
     * @return the map of properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Method to set enabled
     *
     * @param enabled enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Method to get enabled
     *
     * @return true if enabled, else false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Method to set the list of steps
     *
     * @param steps step
     */
    public void setSteps(List<Step> steps) {
        this.steps.addAll(steps);
    }

    /**
     * Method to get the list of steps
     *
     * @return the list of steps
     */
    public List<Step> getStep() {
        return steps;
    }

    /**
     * Method to set the exit code
     *
     * @param exitCode exitCode
     */
    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Method to get the exit code
     *
     * @return the exit code
     */
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return "Job{" + "name='" + name + '\'' + '}';
    }
}
