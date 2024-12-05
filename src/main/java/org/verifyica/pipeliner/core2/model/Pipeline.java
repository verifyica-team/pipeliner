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

package org.verifyica.pipeliner.core2.model;

import static java.lang.String.format;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Pipeline extends Base {

    private List<Job> jobs;

    public Pipeline() {
        super();
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    @Override
    public void validate() {
        propagateIds();
        validateIds();

        validateName(this);
        validateId(this);
        validateEnv(this);
        validateWith(this);
        validateOpt(this);

        getJobs().forEach(Job::validate);
    }

    private void propagateIds() {
        int pipelineIndex = 1;

        if (getId() == null || getId().trim().isEmpty()) {
            setId("pipeline." + pipelineIndex);
        }

        int jobIndex = 1;
        for (Job job : jobs) {
            job.setParent(this);

            if (job.getId() == null || job.getId().trim().isEmpty()) {
                job.setId("pipeline." + pipelineIndex + ".job." + jobIndex);
            }

            int stepIndex = 1;
            for (Step step : job.getSteps()) {
                step.setParent(job);

                if (step.getId() == null || step.getId().trim().isEmpty()) {
                    step.setId("pipeline." + pipelineIndex + ".job." + jobIndex + ".step." + stepIndex);
                }
                stepIndex++;
            }

            jobIndex++;
        }
    }

    private void validateIds() {
        Set<String> set = new LinkedHashSet<>();
        set.add(getId());

        for (Job job : jobs) {
            if (!set.add(job.getId())) {
                throw new ModeDefinitionException(format("%s id not unique", job));
            }

            for (Step step : job.getSteps()) {
                if (!set.add(step.getId())) {
                    throw new ModeDefinitionException(format("%s is not unique", step));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "@pipeline " + super.toString();
    }
}
