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
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.model.Enabled;
import org.verifyica.pipeliner.model.JobModel;

/** Class to implement Job */
public class Job extends Executable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

    private final JobModel jobModel;
    private final List<Step> steps;

    /**
     * Constructor
     *
     * @param jobModel the job model
     */
    public Job(JobModel jobModel) {
        this.jobModel = jobModel;
        this.steps = new ArrayList<>();
    }

    /**
     * Method to set the list of Steps
     *
     * @param steps the steps
     */
    public void setSteps(List<Step> steps) {
        if (steps != null) {
            this.steps.clear();
            this.steps.addAll(steps);
        }
    }

    @Override
    public void execute(Context context) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("executing job [%s] ...", jobModel);
        }

        prepare(context);

        if (Boolean.TRUE.equals(Enabled.decode(jobModel.getEnabled()))) {
            getStopwatch().reset();

            getConsole().info("%s status=[%s]", jobModel, Status.RUNNING);

            Iterator<Step> StepIterator = steps.iterator();
            while (StepIterator.hasNext()) {
                Step step = StepIterator.next();
                step.execute(context);
                setExitCode(step.getExitCode());
                if (getExitCode() != 0) {
                    break;
                }
            }

            while (StepIterator.hasNext()) {
                StepIterator.next().skip(context, Status.SKIPPED);
            }

            Status status = getExitCode() == 0 ? Status.SUCCESS : Status.FAILURE;

            getConsole()
                    .info(
                            "%s status=[%s] exit-code=[%d] ms=[%d]",
                            jobModel,
                            status,
                            getExitCode(),
                            getStopwatch().elapsedTime().toMillis());
        } else {
            skip(context, Status.DISABLED);
        }
    }

    @Override
    public void skip(Context context, Status status) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("skipping job [%s] ...", jobModel);
        }

        prepare(context);

        Status effectiveStatus = Boolean.TRUE.equals(Enabled.decode(jobModel.getEnabled())) ? status : Status.DISABLED;
        getConsole().info("%s status=[%s]", jobModel, effectiveStatus);

        steps.forEach(step -> step.skip(context, status));
    }
}
