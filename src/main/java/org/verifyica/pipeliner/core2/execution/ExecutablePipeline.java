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
import org.verifyica.pipeliner.core2.model.Pipeline;

public class ExecutablePipeline extends Executable {

    private final Pipeline pipeline;
    private List<ExecutableJob> executableJobs;

    public ExecutablePipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public void setExecutableJobs(List<ExecutableJob> executableJobs) {
        this.executableJobs = executableJobs;
    }

    @Override
    public void execute(Console console) {
        if (decodeEnabled(pipeline.getEnabled())) {
            getStopwatch().reset();

            console.log("%s status=[EXECUTING]", pipeline);

            Iterator<ExecutableJob> executableJobIterator = executableJobs.iterator();
            while (executableJobIterator.hasNext()) {
                ExecutableJob executableJob = executableJobIterator.next();
                executableJob.execute(console);

                if (executableJob.getExitCode() != 0) {
                    setExitCode(executableJob.getExitCode());
                    break;
                }
            }

            while (executableJobIterator.hasNext()) {
                executableJobIterator.next().skip(console, "SKIPPED");
            }

            String status = getExitCode() == 0 ? "PASSED" : "FAILED";

            console.log(
                    "%s status=[%s] exit-code=[%d] ms=[%d]",
                    pipeline,
                    status,
                    getExitCode(),
                    getStopwatch().elapsedTime().toMillis());
        } else {
            skip(console, "DISABLED");
        }
    }

    @Override
    public void skip(Console console, String reason) {
        console.log("%s status=[%s]", pipeline, reason);

        executableJobs.forEach(executableJob -> executableJob.skip(console, reason));
    }
}
