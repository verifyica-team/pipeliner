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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Class to implement PipelineModel */
public class PipelineModel extends Model {

    private static final String PIPELINE_ID_PREFIX = "pipeline-";

    private static final String JOB_ID_PREFIX = "job-";

    private static final String STEP_ID_PREFIX = "step-";

    private final List<JobModel> jobModels;

    /** Constructor */
    public PipelineModel() {
        super();

        jobModels = new ArrayList<>();
    }

    /**
     * Method to set the list of job models
     *
     * @param jobModels the list of job models
     */
    public void setJobs(List<JobModel> jobModels) {
        if (jobModels != null) {
            this.jobModels.clear();
            this.jobModels.addAll(jobModels);
        }
    }

    /**
     * Method to get the list of job models
     *
     * @return the list of job models
     */
    public List<JobModel> getJobs() {
        return jobModels;
    }

    @Override
    public void validate() {
        buildTree();
        validateIds();
        validateId();
        validateEnabled();
        validateEnv();
        validateWith();
        validateWorkingDirectory();
        validateTimeoutMinutes();

        List<JobModel> jobModels = getJobs();
        if (jobModels.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> no jobs defined", this));
        } else {
            getJobs().forEach(JobModel::validate);
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

        for (JobModel jobModel : jobModels) {
            // Set the parent
            jobModel.setParent(this);

            // Set the job id if not set
            if (jobModel.getId() == null) {
                jobModel.setId(JOB_ID_PREFIX + jobIndex++);
            }

            for (StepModel stepModel : jobModel.getSteps()) {
                // Set the parent
                stepModel.setParent(jobModel);

                // Set the step id if not set
                if (stepModel.getId() == null) {
                    stepModel.setId(STEP_ID_PREFIX + stepIndex++);
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

        for (JobModel jobModel : jobModels) {
            if (!set.add(jobModel.getId())) {
                throw new PipelineDefinitionException(format("%s -> id=[%s] not unique", jobModel, jobModel.getId()));
            }

            for (StepModel stepModel : jobModel.getSteps()) {
                if (!set.add(stepModel.getId())) {
                    throw new PipelineDefinitionException(
                            format("%s -> id=[%s] is not unique", stepModel, stepModel.getId()));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "@pipeline " + super.toString();
    }
}
