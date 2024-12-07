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
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.model.Job;

public class ExecutableJob extends Executable {

    private final Job job;
    private final Console console;
    private List<ExecutableStep> executableSteps;

    public ExecutableJob(Job job) {
        this.job = job;
        this.console = Console.getInstance();
    }

    public void setExecutableSteps(List<ExecutableStep> executableSteps) {
        this.executableSteps = executableSteps;
    }

    @Override
    public void execute() {
        if (decodeEnabled(job.getEnabled())) {
            getStopwatch().reset();

            console.log("%s status=[%s]", job, Status.RUNNING);

            Iterator<ExecutableStep> executableStepIterator = executableSteps.iterator();
            while (executableStepIterator.hasNext()) {
                ExecutableStep executableStep = executableStepIterator.next();
                executableStep.execute();

                if (executableStep.getExitCode() != 0) {
                    setExitCode(executableStep.getExitCode());
                    break;
                }
            }

            while (executableStepIterator.hasNext()) {
                executableStepIterator.next().skip(Status.SKIPPED);
            }

            Status status = getExitCode() == 0 ? Status.SUCCESS : Status.FAILURE;

            console.log(
                    "%s status=[%s] exit-code=[%d] ms=[%d]",
                    job, status, getExitCode(), getStopwatch().elapsedTime().toMillis());
        } else {
            skip(Status.DISABLED);
        }
    }

    @Override
    public void skip(Status status) {
        console.log("%s status=[%s]", job, status);

        executableSteps.forEach(executableStep -> executableStep.skip(status));
    }
}
