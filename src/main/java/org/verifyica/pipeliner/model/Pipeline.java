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
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.common.Stopwatch;

/** Class to implement Pipeline */
@SuppressWarnings("PMD.EmptyControlStatement")
public class Pipeline implements Action {

    private final String reference;
    private String name;
    private String id;
    private Status status;
    private final Map<String, String> environmentVariables;
    private final Map<String, String> properties;
    private final List<Job> jobs;
    private int exitCode;
    private final Stopwatch stopwatch;

    /**
     * Constructor
     */
    public Pipeline() {
        this.reference = "pipeline";
        this.environmentVariables = new LinkedHashMap<>();
        this.properties = new LinkedHashMap<>();
        this.jobs = new ArrayList<>();
        this.stopwatch = new Stopwatch();
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
     * Method to add environment variables
     *
     * @param environmentVariables environmentVariables
     */
    public void addEnvironmentVariables(Map<String, String> environmentVariables) {
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
     * Method to add properties
     *
     * @param properties properties
     */
    public void addProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    /**
     * Method to get properties
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
    public void addJobs(List<Job> jobs) {
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

    @Override
    public void execute(Console console) {
        stopwatch.reset();

        console.trace("------------------------------------------------------------");
        console.trace("execute %s", this);
        console.trace("------------------------------------------------------------");

        console.log(this);

        if (isEnabled()) {
            for (Job job : getJobs()) {
                job.execute(console);
            }
        } else {
            // TODO make configurable?
            /*
            for (Job job : getJobs()) {
                job.skip(console);
            }
            */
        }

        getJobs().stream()
                .filter(job -> job.getExitCode() != 0)
                .findFirst()
                .ifPresent(job -> setExitCode(job.getExitCode()));

        console.log(
                "%s exit-code=[%d] ms=[%d]",
                this, getExitCode(), stopwatch.elapsedTime().toMillis());
    }

    @Override
    public void skip(Console console) {
        stopwatch.reset();

        console.trace("------------------------------------------------------------");
        console.trace("skip %s", this);

        for (Job job : getJobs()) {
            job.skip(console);
        }

        console.log(
                "%s exit-code=[%d] ms=[%d]",
                this, getExitCode(), stopwatch.elapsedTime().toMillis());
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return format(
                "@pipeline name=[%s] id=[%s] ref=[%s] enabled=[%b]", getName(), getId(), getReference(), isEnabled());
    }
}
