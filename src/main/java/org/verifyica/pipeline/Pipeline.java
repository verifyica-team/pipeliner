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

/** Class to implement Pipeline */
public class Pipeline {

    private String name;
    private Map<String, String> environmentVariables;
    private Map<String, String> properties;
    private List<Job> jobs;

    /** Constructor */
    public Pipeline() {
        initialize();
    }

    /** Method to initialize the pipeline */
    private void initialize() {
        name = UUID.randomUUID().toString();
        properties = new LinkedHashMap<>();
        jobs = new ArrayList<>();
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
     * Method to set the list of jobs
     *
     * @param jobs jobs
     */
    public void setJobs(List<Job> jobs) {
        this.jobs.addAll(jobs);
    }

    /**
     * Method to get the list of jobs
     *
     * @return the list of jobs
     */
    public List<Job> getJob() {
        return jobs;
    }

    @Override
    public String toString() {
        return "Pipeline{" + "name='" + name + '\'' + '}';
    }
}
