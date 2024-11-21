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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.common.Stopwatch;

/** Class to implement Job */
@SuppressWarnings("PMD.EmptyControlStatement")
public class Job implements Action {

    private final Pipeline pipeline;
    private final String reference;
    private String id;
    private String name;
    private boolean enabled;
    private final Map<String, String> environmentVariables;
    private final Map<String, String> properties;
    private final List<Step> steps;
    private int exitCode;
    private final Stopwatch stopwatch;

    /**
     * Constructor
     *
     * @param pipeline pipeline
     * @param index index
     */
    public Job(Pipeline pipeline, int index) {
        this.pipeline = pipeline;
        this.reference = pipeline.getReference() + "-job-" + index;
        this.enabled = true;
        this.environmentVariables = new LinkedHashMap<>();
        this.properties = new LinkedHashMap<>();
        this.steps = new ArrayList<>();
        this.stopwatch = new Stopwatch();
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
        return enabled && pipeline.isEnabled();
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
     * @return the map of properties properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Method to add a list of steps
     *
     * @param steps step
     */
    public void addSteps(List<Step> steps) {
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
    private void setExitCode(int exitCode) {
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
            Iterator<Step> iterator = getSteps().iterator();

            while (iterator.hasNext()) {
                Step step = iterator.next();

                if (step.isEnabled()) {
                    step.execute(console);
                    if (step.getExitCode() != 0) {
                        step.getJob().setExitCode(step.getExitCode());
                        break;
                    }
                } else {
                    // TODO make configurable?
                    // step.skip(console);
                }
            }

            while (iterator.hasNext()) {
                iterator.next().skip(console);
            }
        } else {
            // TODO make configurable?
            /*
            for (Step step : getSteps()) {
                step.skip(console);
            }
            */
        }

        getSteps().stream()
                .filter(step -> step.getExitCode() != 0)
                .findFirst()
                .ifPresent(step -> setExitCode(step.getExitCode()));

        console.log(
                "%s exit-code=[%d] ms=[%d]",
                this, getExitCode(), stopwatch.elapsedTime().toMillis());
    }

    @Override
    public void skip(Console console) {
        stopwatch.reset();
        console.trace("skip %s", this);

        for (Step step : getSteps()) {
            step.skip(console);
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
        return format("@job name=[%s] id=[%s] ref=[%s] enabled=[%b]", getName(), getId(), getReference(), isEnabled());
    }
}
