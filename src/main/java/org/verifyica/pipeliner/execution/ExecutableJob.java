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
import org.verifyica.pipeliner.model.Job;

/** Class to implement ExecutableJob */
public class ExecutableJob extends Executable {

    private final Job job;
    private List<ExecutableStep> executableSteps;

    /**
     * Constructor
     *
     * @param job job
     */
    public ExecutableJob(Job job) {
        this.job = job;
    }

    /**
     * Method to set the list of ExecutableSteps
     *
     * @param executableSteps executableSteps
     */
    public void setExecutableSteps(List<ExecutableStep> executableSteps) {
        this.executableSteps = executableSteps;
    }

    @Override
    public void execute(ExecutableContext executableContext) {
        if (decodeEnabled(job.getEnabled())) {
            getStopwatch().reset();

            executableContext.getConsole().log("%s status=[%s]", job, Status.RUNNING);

            Iterator<ExecutableStep> executableStepIterator = executableSteps.iterator();
            while (executableStepIterator.hasNext()) {
                ExecutableStep executableStep = executableStepIterator.next();
                executableStep.execute(executableContext);

                if (executableStep.getExitCode() != 0) {
                    setExitCode(executableStep.getExitCode());
                    break;
                }
            }

            while (executableStepIterator.hasNext()) {
                executableStepIterator.next().skip(executableContext, Status.SKIPPED);
            }

            Status status = getExitCode() == 0 ? Status.SUCCESS : Status.FAILURE;

            executableContext
                    .getConsole()
                    .log(
                            "%s status=[%s] exit-code=[%d] ms=[%d]",
                            job,
                            status,
                            getExitCode(),
                            getStopwatch().elapsedTime().toMillis());
        } else {
            skip(executableContext, Status.DISABLED);
        }
    }

    @Override
    public void skip(ExecutableContext executableContext, Status status) {
        executableContext.getConsole().log("%s status=[%s]", job, status);

        executableSteps.forEach(executableStep -> executableStep.skip(executableContext, status));
    }
}
