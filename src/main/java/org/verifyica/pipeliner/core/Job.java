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

package org.verifyica.pipeliner.core;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement Job */
public class Job extends Node {

    private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

    private final List<Step> steps;

    /**
     * Constructor
     */
    public Job() {
        super();
        steps = new ArrayList<>();
    }

    /**
     * Method to set the list of steps
     *
     * @param steps this list of steps
     */
    public void setSteps(List<Step> steps) {
        if (steps != null) {
            this.steps.clear();
            this.steps.addAll(steps);
        }
    }

    /**
     * Method to get the list of steps
     *
     * @return the list of steps
     */
    public List<Step> getSteps() {
        return steps;
    }

    @Override
    public void validate() {
        validateId();
        validateEnabled();
        validateEnvironmentVariables();
        validateVariables();
        validateWorkingDirectory();
        validateTimeoutMinutes();

        // Validate that the job has at least one step
        if (steps.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> no steps defined", this));
        }

        // Validate the steps
        steps.forEach(Step::validate);
    }

    @Override
    public int execute(Context context) {
        getStopwatch().reset();

        int exitCode = 0;

        // Get the console
        Console console = context.getConsole();

        if (Boolean.TRUE.equals(Enabled.decode(getEnabled()))) {
            boolean shouldExecute;

            try {
                shouldExecute = shouldExecute(context);
            } catch (Throwable t) {
                // Set the exit code
                exitCode = 1;

                // Emit the error
                console.print("@error %s -> %s", this, t.getMessage());

                return exitCode;
            }

            if (shouldExecute) {
                // Emit the status
                console.print("%s status=[%s]", this, Status.RUNNING);

                // Add the job environment variables to the context
                getEnvironmentVariables().forEach((name, value) -> context.getEnvironmentVariables()
                        .put(name, value));

                String pipelineId = getParent(Pipeline.class).getId();
                String jobId = getId();

                // Add the job variables to the context
                getVariables().forEach((name, value) -> context.setVariable(name, value, pipelineId, jobId, null));

                // Execute the steps
                for (Step step : steps) {
                    // Execute the step
                    exitCode = step.execute(context);

                    // If the exit code is not 0, break the loop
                    if (exitCode != 0) {
                        break;
                    }
                }

                // Get the status based on the exit code
                Status status = exitCode == 0 ? Status.SUCCESS : Status.FAILURE;

                // Emit the status
                console.print(
                        "%s status=[%s] exit-code=[%d] ms=[%s]",
                        this, status, exitCode, getStopwatch().elapsedTime().toMillis());
            }
        }

        return exitCode;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    public String toString() {
        return "@job" + super.toString();
    }
}
