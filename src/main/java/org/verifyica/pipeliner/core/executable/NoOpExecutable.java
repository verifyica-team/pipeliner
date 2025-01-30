/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.core.executable;

import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.Job;
import org.verifyica.pipeliner.core.Pipeline;
import org.verifyica.pipeliner.core.Step;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement NoOpExecutable */
public class NoOpExecutable implements Executable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpExecutable.class);

    private final Pipeline pipeline;
    private final Job job;
    private final Step step;

    /**
     * Constructor
     */
    public NoOpExecutable(Step step) {
        this.step = step;
        this.job = step.getParent(Job.class);
        this.pipeline = job.getParent(Pipeline.class);
    }

    @Override
    public int execute(Context context) {
        LOGGER.trace("executing %s %s %s", pipeline, job, step);

        return 0;
    }
}
