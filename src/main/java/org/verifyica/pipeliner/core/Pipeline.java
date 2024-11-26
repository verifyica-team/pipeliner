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

package org.verifyica.pipeliner.core;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.Stopwatch;

/** Class to implement Pipeline */
@SuppressWarnings("PMD.EmptyControlStatement")
public class Pipeline implements Element {

    private final String reference;
    private String name;
    private String id;
    private final Map<String, String> environmentVariables;
    private final Map<String, String> properties;
    private final Map<String, String> options;
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
        this.options = new LinkedHashMap<>();
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
     * Method to get environment variables
     *
     * @return the map of environment variables
     */
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
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
     * Method to get options
     *
     * @return the map of options
     */
    public Map<String, String> getOptions() {
        return options;
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

        console.log(this);

        for (Job job : getJobs()) {
            if (job.isEnabled()) {
                job.execute(console);
            } else {
                job.skip(console);
            }
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
        throw new RuntimeException("A pipeline can't be skipped");
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return format(
                "@pipeline name=[%s] id=[%s] ref=[%s]", getName() == null ? "" : getName(), getId(), getReference());
    }
}
