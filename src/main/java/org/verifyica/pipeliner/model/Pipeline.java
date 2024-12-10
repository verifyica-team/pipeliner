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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.verifyica.pipeliner.model.parser.YamlDefinitionException;

/** Class to implement Pipeline */
public class Pipeline extends Base {

    private List<Job> jobs;

    /** Constructor */
    public Pipeline() {
        super();
    }

    /**
     * Method to set the list of Jobs
     *
     * @param jobs jobs
     */
    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    /**
     * Method to get the list of Jobs
     *
     * @return the list of Jobs
     */
    public List<Job> getJobs() {
        return jobs;
    }

    @Override
    public void validate() {
        // generateMissingIds();
        buildTree();

        validateName(this);
        validateId(this);
        validateEnv(this);
        validateWith(this);
        validateWorkingDirectory(this);

        getJobs().forEach(Job::validate);

        validateIds();
    }

    private void buildTree() {
        for (Job job : jobs) {
            job.setParent(this);
            for (Step step : job.getSteps()) {
                step.setParent(job);
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
            if (job.getId() != null && !set.add(job.getId())) {
                throw new YamlDefinitionException(format("%s -> id=[%s] not unique", job, job.getId()));
            }

            for (Step step : job.getSteps()) {
                if (step.getId() != null && !set.add(step.getId())) {
                    throw new YamlDefinitionException(format("%s -> id=[%s] is not unique", step, step.getId()));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "@pipeline " + super.toString();
    }
}
