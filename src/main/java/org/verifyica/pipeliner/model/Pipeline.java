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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Class to implement Pipeline */
public class Pipeline {

    private final String reference;
    private String name;
    private String id;
    private boolean enabled;
    private final Map<String, String> environmentVariables;
    private final List<Job> jobs;
    private int exitCode;

    /**
     * Constructor
     */
    public Pipeline() {
        this.reference = "pipeline";
        this.environmentVariables = new LinkedHashMap<>();
        this.jobs = new ArrayList<>();
    }

    /**
     * Method to get the location
     *
     * @return the location
     */
    public String getReference() {
        return reference;
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
        return id != null ? id : reference;
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
    public List<Job> getJobs() {
        return jobs;
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
        return format(
                "@pipeline name=[%s] id=[%s] ref=[%s] enabled=[%b]", getName(), getId(), getReference(), isEnabled());
    }
}
