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
import org.verifyica.pipeliner.execution.support.Status;
import org.verifyica.pipeliner.model.Pipeline;

/** Class to implement ExecutablePipeline */
public class ExecutablePipeline extends Executable {

    private final Pipeline pipeline;
    private final Console console;
    private List<ExecutableJob> executableJobs;

    /**
     * Constructor
     *
     * @param pipeline pipeline
     */
    public ExecutablePipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
        this.console = Console.getInstance();
    }

    /**
     * Method to set the list of ExecutableJobs
     *
     * @param executableJobs executableJobs
     */
    public void setExecutableJobs(List<ExecutableJob> executableJobs) {
        this.executableJobs = executableJobs;
    }

    @Override
    public void execute() {
        if (decodeEnabled(pipeline.getEnabled())) {
            getStopwatch().reset();

            console.log("%s status=[%s]", pipeline, Status.RUNNING);

            Iterator<ExecutableJob> executableJobIterator = executableJobs.iterator();
            while (executableJobIterator.hasNext()) {
                ExecutableJob executableJob = executableJobIterator.next();
                executableJob.execute();
                if (executableJob.getExitCode() != 0) {
                    setExitCode(executableJob.getExitCode());
                    break;
                }
            }

            while (executableJobIterator.hasNext()) {
                executableJobIterator.next().skip(Status.SKIPPED);
            }

            Status status = getExitCode() == 0 ? Status.SUCCESS : Status.FAILURE;

            console.log(
                    "%s status=[%s] exit-code=[%d] ms=[%d]",
                    pipeline,
                    status,
                    getExitCode(),
                    getStopwatch().elapsedTime().toMillis());
        } else {
            skip(Status.DISABLED);
        }
    }

    @Override
    public void skip(Status status) {
        console.log("%s status=[%s]", pipeline, status);

        executableJobs.forEach(executableJob -> executableJob.skip(status));
    }
}
