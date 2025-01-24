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

package org.verifyica.pipeliner.core;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement Job */
public class Job extends Node {

    private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

    private final List<Step> steps;

    /** Constructor */
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
    public int execute(Context context) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("executing job [%s] ...", this);
        }

        validate();

        Console console = context.getConsole();
        int exitCode = 0;

        if (Boolean.TRUE.equals(Enabled.decode(getEnabled()))) {
            getStopwatch().reset();

            console.info("%s status=[%s]", this, Status.RUNNING);

            Iterator<Step> stepIterator = steps.iterator();
            while (stepIterator.hasNext()) {
                exitCode = stepIterator.next().execute(context);
                if (exitCode != 0) {
                    break;
                }
            }

            while (stepIterator.hasNext()) {
                stepIterator.next().skip(context, Status.SKIPPED);
            }

            Status status = exitCode == 0 ? Status.SUCCESS : Status.FAILURE;

            console.info(
                    "%s status=[%s] exit-code=[%d] ms=[%d]",
                    this, status, exitCode, getStopwatch().elapsedTime().toMillis());

            return exitCode;
        } else {
            skip(context, Status.DISABLED);

            return 1;
        }
    }

    @Override
    public void skip(Context context, Status status) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("skipping job [%s] ...", this);
        }

        Console console = context.getConsole();

        Status effectiveStatus = Boolean.TRUE.equals(Enabled.decode(getEnabled())) ? status : Status.DISABLED;

        console.info("%s status=[%s]", this, effectiveStatus);

        steps.forEach(step -> step.skip(context, status));
    }

    @Override
    public String toString() {
        return "@job " + super.toString();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    /**
     * Method to validate the job
     */
    private void validate() {
        validateId();
        validateEnabled();
        validateEnv();
        validateWith();
        validateWorkingDirectory();
        validateTimeoutMinutes();

        if (steps.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> no steps defined", this));
        }
    }
}
