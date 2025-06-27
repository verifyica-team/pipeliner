/*
 * Copyright (C) Pipeliner project authors and contributors
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

import java.util.ArrayList;
import java.util.List;

/** Class to implement Pipeline */
public class Pipeline extends Node {

    private final List<Job> jobs;

    /**
     * Constructor
     */
    public Pipeline() {
        super(Type.PIPELINE);

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

    /**
     * Method to get the list of jobs
     *
     * @return the list of jobs
     */
    public List<Job> getJobs() {
        return jobs;
    }
}
