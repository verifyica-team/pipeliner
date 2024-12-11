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

    private final List<JobModel> jobModels;

    /** Constructor */
    public PipelineModel() {
        super();

        jobModels = new ArrayList<>();
    }

    /**
     * Method to set the list of JobModels
     *
     * @param jobModels jobModels
     */
    public void setJobs(List<JobModel> jobModels) {
        if (jobModels != null) {
            this.jobModels.clear();
            this.jobModels.addAll(jobModels);
        }
    }

    /**
     * Method to get the list of JobModels
     *
     * @return the list of JobModels
     */
    public List<JobModel> getJobs() {
        return jobModels;
    }

    @Override
    public void validate() {
        buildTree();

        validateIds();
        validateName();
        validateId();
        validateEnabled();
        validateEnv();
        validateWith();
        validateWorkingDirectory();

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

    /**
     * Method to validate ids
     */
    private void validateIds() {
        Set<String> set = new LinkedHashSet<>();
        if (getId() != null) {
            set.add(getId());
        }

        for (JobModel jobModel : jobModels) {
            if (jobModel.getId() != null && !set.add(jobModel.getId())) {
                throw new PipelineDefinitionException(format("%s -> id=[%s] not unique", jobModel, jobModel.getId()));
            }

            for (StepModel stepModel : jobModel.getSteps()) {
                if (stepModel.getId() != null && !set.add(stepModel.getId())) {
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
