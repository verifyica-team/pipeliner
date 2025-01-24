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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement Pipeline */
public class Pipeline extends Node {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);

    private static final String PIPELINE_ID_PREFIX = "pipeline-";

    private static final String JOB_ID_PREFIX = "job-";

    private static final String STEP_ID_PREFIX = "step-";

    private final List<Job> jobs;

    /** Constructor */
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
    public int execute(Context context) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("executing pipeline [%s] ...", this);
        }

        validate();

        Console console = context.getConsole();
        int exitCode = 0;

        if (Boolean.TRUE.equals(Enabled.decode(getEnabled()))) {
            getStopwatch().reset();

            console.info("%s status=[%s]", this, Status.RUNNING);

            Iterator<Job> jobIterator = jobs.iterator();
            while (jobIterator.hasNext()) {
                exitCode = jobIterator.next().execute(context);
                if (exitCode != 0) {
                    break;
                }
            }

            while (jobIterator.hasNext()) {
                jobIterator.next().skip(context, Status.SKIPPED);
            }

            Status status = exitCode == 0 ? Status.SUCCESS : Status.FAILURE;

            console.info(
                    "%s status=[%s] exit-code=[%d] ms=[%d]",
                    this, status, exitCode, getStopwatch().elapsedTime().toMillis());
            return exitCode;
        } else {
            skip(context, Status.DISABLED);

            return 0;
        }
    }

    @Override
    public void skip(Context context, Status status) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("skipping pipeline [%s] ...", this);
        }

        Console console = context.getConsole();

        Status effectiveStatus = Boolean.TRUE.equals(Enabled.decode(getEnabled())) ? status : Status.DISABLED;

        console.info("%s status=[%s]", this, effectiveStatus);

        jobs.forEach(job -> job.skip(context, status));
    }

    @Override
    public String toString() {
        return "@pipeline " + super.toString();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    /**
     * Method to validate the pipeline
     */
    private void validate() {
        buildTree();
        validateIds();
        validateId();
        validateEnabled();
        validateEnv();
        validateWith();
        validateWorkingDirectory();
        validateTimeoutMinutes();

        if (jobs.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> no jobs defined", this));
        }
    }

    /**
     * Method to build the tree
     */
    private void buildTree() {
        int pipelineIndex = 1;
        int jobIndex = 1;
        int stepIndex = 1;

        // Set the pipeline id if not set
        if (getId() == null) {
            setId(PIPELINE_ID_PREFIX + pipelineIndex);
        }

        for (Job job : jobs) {
            // Set the parent
            job.setParent(this);

            // Set the job id if not set
            if (job.getId() == null) {
                job.setId(JOB_ID_PREFIX + jobIndex++);
            }

            for (Step step : job.getSteps()) {
                // Set the parent
                step.setParent(job);

                // Set the step id if not set
                if (step.getId() == null) {
                    step.setId(STEP_ID_PREFIX + stepIndex++);
                }
            }
        }
    }

    /**
     * Method to validate ids
     */
    private void validateIds() {
        Set<String> set = new LinkedHashSet<>();
        if (getId() != null) {
            set.add(getId());
        }

        for (Job job : jobs) {
            if (!set.add(job.getId())) {
                throw new PipelineDefinitionException(format("%s -> id=[%s] not unique", job, job.getId()));
            }

            for (Step step : job.getSteps()) {
                if (!set.add(step.getId())) {
                    throw new PipelineDefinitionException(format("%s -> id=[%s] is not unique", step, step.getId()));
                }
            }
        }
    }
}
