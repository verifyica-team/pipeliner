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

package org.verifyica.pipeline.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Class to implement Job */
public class Job {

    private final Pipeline pipeline;
    private final int index;
    private final String location;
    private String id;
    private String name;
    private boolean enabled;
    private final Map<String, String> environmentVariables;
    private final List<Step> steps;
    private int exitCode;

    /**
     * Constructor
     *
     * @param pipeline pipeline
     * @param index index
     */
    public Job(Pipeline pipeline, int index) {
        this.pipeline = pipeline;
        this.index = index;
        this.location = pipeline.getLocation() + "_job-" + index;
        this.enabled = true;
        this.environmentVariables = new LinkedHashMap<>();
        this.steps = new ArrayList<>();
    }

    /**
     * Method to get the pipeline
     *
     * @return the pipeline
     */
    public Pipeline getPipeline() {
        return pipeline;
    }

    /**
     * Method to get the location
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Method to get the index
     *
     * @return the index
     */
    public int getIndex() {
        return index;
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
        return id != null ? id : location;
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
     * Method to set environment variables
     *
     * @param environmentVariables environmentVariables
     */
    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables.putAll(environmentVariables);
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
    public List<Step> getSteps() {
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
