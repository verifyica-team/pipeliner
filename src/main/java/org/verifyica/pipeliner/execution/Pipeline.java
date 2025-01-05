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

package org.verifyica.pipeliner.execution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.verifyica.pipeliner.execution.support.Status;
import org.verifyica.pipeliner.model.Enabled;
import org.verifyica.pipeliner.model.PipelineModel;

/** Class to implement Pipeline */
public class Pipeline extends Executable {

    private final PipelineModel pipelineModel;
    private final List<Job> jobs;

    /**
     * Constructor
     *
     * @param pipelineModel pipelineModel
     */
    public Pipeline(PipelineModel pipelineModel) {
        this.pipelineModel = pipelineModel;
        this.jobs = new ArrayList<>();
    }

    /**
     * Method to set the list of Jobs
     *
     * @param jobs Jobs
     */
    public void setJobs(List<Job> jobs) {
        if (jobs != null) {
            this.jobs.clear();
            this.jobs.addAll(jobs);
        }
    }

    @Override
    public void execute(Context context) {
        prepare(context);

        if (Boolean.TRUE.equals(Enabled.decode(pipelineModel.getEnabled()))) {
            getStopwatch().reset();

            getConsole().info("%s status=[%s]", pipelineModel, Status.RUNNING);

            Iterator<Job> JobIterator = jobs.iterator();
            while (JobIterator.hasNext()) {
                Job job = JobIterator.next();
                job.execute(context);
                setExitCode(job.getExitCode());
                if (getExitCode() != 0) {
                    break;
                }
            }

            while (JobIterator.hasNext()) {
                JobIterator.next().skip(context, Status.SKIPPED);
            }

            Status status = getExitCode() == 0 ? Status.SUCCESS : Status.FAILURE;

            getConsole()
                    .info(
                            "%s status=[%s] exit-code=[%d] ms=[%d]",
                            pipelineModel,
                            status,
                            getExitCode(),
                            getStopwatch().elapsedTime().toMillis());
        } else {
            skip(context, Status.DISABLED);
        }
    }

    @Override
    public void skip(Context context, Status status) {
        prepare(context);

        if (Boolean.TRUE.equals(Enabled.decode(pipelineModel.getEnabled()))) {
            getConsole().info("%s status=[%s]", pipelineModel, status);
        } else {
            getConsole().info("%s status=[%s]", pipelineModel, Status.DISABLED);
        }

        jobs.forEach(job -> job.skip(context, status));
    }
}
