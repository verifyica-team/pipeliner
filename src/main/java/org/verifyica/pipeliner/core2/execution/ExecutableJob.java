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

package org.verifyica.pipeliner.core2.execution;

import java.util.Iterator;
import java.util.List;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.core2.model.Job;

public class ExecutableJob extends Executable {

    private final Job job;
    private List<ExecutableStep> executableSteps;

    public ExecutableJob(Job job) {
        this.job = job;
    }

    public void setExecutableSteps(List<ExecutableStep> executableSteps) {
        this.executableSteps = executableSteps;
    }

    @Override
    public void execute(Console console) {
        if (decodeEnabled(job.getEnabled())) {
            getStopwatch().reset();

            console.log("%s status=[EXECUTING]", job);

            Iterator<ExecutableStep> executableStepIterator = executableSteps.iterator();
            while (executableStepIterator.hasNext()) {
                ExecutableStep executableStep = executableStepIterator.next();
                executableStep.execute(console);

                if (executableStep.getExitCode() != 0) {
                    setExitCode(executableStep.getExitCode());
                    break;
                }
            }

            while (executableStepIterator.hasNext()) {
                executableStepIterator.next().skip(console, "SKIPPED");
            }

            String status = getExitCode() == 0 ? "PASSED" : "FAILED";

            console.log(
                    "%s status=[%s] exit-code=[%d] ms=[%d]",
                    job, status, getExitCode(), getStopwatch().elapsedTime().toMillis());
        } else {
            skip(console, "DISABLED");
        }
    }

    @Override
    public void skip(Console console, String reason) {
        console.log("%s status=[%s]", job, reason);

        executableSteps.forEach(executableStep -> executableStep.skip(console, reason));
    }
}
