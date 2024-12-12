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

import java.util.Iterator;
import java.util.List;
import org.verifyica.pipeliner.execution.support.Status;
import org.verifyica.pipeliner.model.JobModel;
import org.verifyica.pipeliner.model.support.Enabled;

/** Class to implement Job */
public class Job extends Executable {

    private final JobModel jobModel;
    private List<Step> steps;

    /**
     * Constructor
     *
     * @param jobModel jobModel
     */
    public Job(JobModel jobModel) {
        this.jobModel = jobModel;
    }

    /**
     * Method to set the list of Steps
     *
     * @param steps Steps
     */
    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    @Override
    public void execute(Context context) {
        if (Boolean.TRUE.equals(Enabled.decode(jobModel.getEnabled()))) {
            getStopwatch().reset();

            context.getConsole().info("%s status=[%s]", jobModel, Status.RUNNING);

            Iterator<Step> StepIterator = steps.iterator();
            while (StepIterator.hasNext()) {
                Step step = StepIterator.next();
                step.execute(context);

                if (step.getExitCode() != 0) {
                    setExitCode(step.getExitCode());
                    break;
                }
            }

            while (StepIterator.hasNext()) {
                StepIterator.next().skip(context, Status.SKIPPED);
            }

            Status status = getExitCode() == 0 ? Status.SUCCESS : Status.FAILURE;

            context.getConsole()
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
        context.getConsole().info("%s status=[%s]", jobModel, status);

        steps.forEach(step -> step.skip(context, status));
    }
}
