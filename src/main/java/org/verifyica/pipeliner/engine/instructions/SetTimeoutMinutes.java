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

package org.verifyica.pipeliner.engine.instructions;

import org.verifyica.pipeliner.engine.Context;
import org.verifyica.pipeliner.engine.Instruction;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.support.PeekIterator;

/**
 * Instruction to set the timeout in minutes of the current frame
 */
public class SetTimeoutMinutes implements Instruction {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SetTimeoutMinutes.class);

    /**
     * The timeout in minutes to set for the current frame.
     */
    private final String timeoutMinutes;

    /**
     * Constructor
     *
     * @param timeoutMinutes the timeout in minutes to set for the current frame
     */
    private SetTimeoutMinutes(String timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("timeoutMinutes = [%s]", timeoutMinutes);

        // Set the timeout in minutes for the current frame
        context.getFrame().setTimeoutMinutes(timeoutMinutes);
    }

    @Override
    public String toString() {
        return getClass().getName() + " { timeoutMinutes [" + timeoutMinutes + "] }";
    }

    /**
     * Factory method to create a new instance of SetTimeoutMinutes.
     *
     * @param timeoutMinutes the timeout in minutes
     * @return a new SetTimeoutMinutes instance
     */
    public static SetTimeoutMinutes of(String timeoutMinutes) {
        return new SetTimeoutMinutes(timeoutMinutes);
    }
}
