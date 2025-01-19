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
import java.util.List;

/** Class to implement PipelineModel */
public class PipelineModel extends Model {

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

        validateName();
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
        for (JobModel jobModel : jobModels) {
            jobModel.setParent(this);
            for (StepModel stepModel : jobModel.getSteps()) {
                stepModel.setParent(jobModel);
            }
        }
    }

    @Override
    public String toString() {
        return "@pipeline " + super.toString();
    }
}
