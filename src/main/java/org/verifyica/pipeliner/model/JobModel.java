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

/** Class to implement JobModel */
public class JobModel extends Model {

    private final List<StepModel> stepModels;

    /** Constructor */
    public JobModel() {
        super();
        stepModels = new ArrayList<>();
    }

    /**
     * Method to set the list of StepModels
     *
     * @param stepModels stepModels
     */
    public void setSteps(List<StepModel> stepModels) {
        if (stepModels != null) {
            this.stepModels.clear();
            this.stepModels.addAll(stepModels);
        }
    }

    /**
     * Method to get the list of StepModels
     *
     * @return the list of StepModels
     */
    public List<StepModel> getSteps() {
        return stepModels;
    }

    @Override
    public void validate() {
        validateName();
        validateId();
        validateEnabled();
        validateEnv();
        validateWith();
        validateWorkingDirectory();

        List<StepModel> stepModels = getSteps();
        if (stepModels.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> no steps defined", this));
        } else {
            getSteps().forEach(StepModel::validate);
        }
    }

    @Override
    public String toString() {
        return "@job " + super.toString();
    }
}
