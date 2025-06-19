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
 * Instruction to set the working directory of the current frame.
 */
public class SetWorkingDirectory implements Instruction {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SetWorkingDirectory.class);

    /**
     * The working directory to set for the current frame.
     */
    private final String workingDirectory;

    /**
     * Constructor
     *
     * @param workingDirectory the working directory to set for the current frame
     */
    private SetWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("workingDirectory = [%s]", workingDirectory);

        // Set the working directory of the current frame
        context.getFrame().setWorkingDirectory(workingDirectory);
    }

    @Override
    public String toString() {
        return getClass().getName() + " { workingDirectory [" + workingDirectory + "] }";
    }

    /**
     * Factory method to create a new instance of SetWorkingDirectory.
     *
     * @param workingDirectory the working directory
     * @return a new SetWorkingDirectory instance
     */
    public static SetWorkingDirectory of(String workingDirectory) {
        return new SetWorkingDirectory(workingDirectory);
    }
}
