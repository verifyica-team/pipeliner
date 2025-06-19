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
import org.verifyica.pipeliner.engine.Frame;
import org.verifyica.pipeliner.engine.Instruction;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.support.PeekIterator;

/**
 * Instruction to print the current frame's status.
 */
public class PrintFrameStatus implements Instruction {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintFrameStatus.class);

    /**
     * The status to print for the current frame.
     */
    private final String status;

    /**
     * Constructor
     *
     * @param status the status to print
     */
    private PrintFrameStatus(String status) {
        this.status = status;
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("status = [%s]", status);

        // Get the current frame from the context
        Frame frame = context.getFrame();

        // Print the frame status
        context.getConsole().println("%s status=[%s]", frame.toConsoleString(), status);
    }

    @Override
    public String toString() {
        return getClass().getName() + " { status [" + status + "] }";
    }

    /**
     * Factory method to create a PrintFrameStatus instruction.
     *
     * @param status the status to print
     * @return a new PrintFrameStatus instance
     */
    public static PrintFrameStatus of(String status) {
        return new PrintFrameStatus(status);
    }
}
