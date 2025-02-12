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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement Pipeline */
public class Pipeline extends Node {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);

    private final List<Job> jobs;

    /**
     * Constructor
     */
    public Pipeline() {
        super();

        jobs = new ArrayList<>();
    }

    /**
     * Method to set the list of jobs
     *
     * @param jobs the list of jobs
     */
    public void setJobs(List<Job> jobs) {
        if (jobs != null) {
            this.jobs.clear();
            this.jobs.addAll(jobs);
        }
    }

    @Override
    public void validate() {
        buildRelationships();
        validateUniqueIds();
        validateId();
        validateEnabled();
        validateEnvironmentVariables();
        validateVariables();
        validateWorkingDirectory();
        validateTimeoutMinutes();

        // Validate the pipeline has at least one job
        if (jobs.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> no jobs defined", this));
        }

        // Validate the jobs
        jobs.forEach(Job::validate);
    }

    @Override
    public int execute(Context context) {
        getStopwatch().reset();

        Console console = context.getConsole();
        int exitCode = 0;

        if (Boolean.TRUE.equals(Enabled.decode(getEnabled()))) {
            // Emit the status
            console.emit("%s status=[%s]", this, Status.RUNNING);

            // Add the pipeline environment variables to the context
            getEnvironmentVariables()
                    .forEach((name, value) -> context.getEnvironmentVariables().put(name, value));

            // Add the pipeline variables to the context
            getVariables().forEach((name, value) -> context.setVariable(name, value, getId(), null, null));

            // Execute the jobs
            for (Job job : jobs) {
                // Execute the job
                exitCode = job.execute(context);

                // If the exit code is not 0, break the loop
                if (exitCode != 0) {
                    break;
                }
            }

            // Get the status based on the exit code
            Status status = exitCode == 0 ? Status.SUCCESS : Status.FAILURE;

            // Emit the status
            console.emit(
                    "%s status=[%s] exit-code=[%d] ms=[%s]",
                    this, status, exitCode, getStopwatch().elapsedTime().toMillis());
        }

        return exitCode;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    public String toString() {
        return "@pipeline" + super.toString();
    }

    /**
     * Method to build relationships
     */
    private void buildRelationships() {
        // Loop through the jobs
        for (Job job : jobs) {
            // Set the parent
            job.setParent(this);

            for (Step step : job.getSteps()) {
                // Set the parent
                step.setParent(job);
            }
        }
    }

    /**
     * Method to validate unique ids
     */
    private void validateUniqueIds() {
        // Create a set to track ids
        Set<String> idSet = new LinkedHashSet<>();

        String pipelineId = getId();
        if (pipelineId != null) {
            // Add the pipeline id
            idSet.add(pipelineId);
        }

        // Loop through the jobs
        for (Job job : jobs) {
            String jobId = job.getId();
            if (jobId != null && !idSet.add(jobId)) {
                throw new PipelineDefinitionException(format("%s -> id=[%s] not unique", job, job.getId()));
            }

            // Loop through the steps
            for (Step step : job.getSteps()) {
                String stepId = step.getId();
                if (stepId != null && !idSet.add(stepId)) {
                    throw new PipelineDefinitionException(format("%s -> id=[%s] is not unique", step, step.getId()));
                }
            }
        }
    }
}
