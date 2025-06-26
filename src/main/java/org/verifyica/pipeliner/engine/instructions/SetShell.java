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
 * Instruction to set the shell of the current frame
 */
public class SetShell implements Instruction {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SetShell.class);

    /**
     * The shell to set for the current frame.
     */
    private final String shell;

    /**
     * Constructor
     *
     * @param shell the shell to set for the current frame
     */
    private SetShell(String shell) {
        this.shell = shell;
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("shell = [%s]", shell);

        // Set the shell of the current frame
        context.getFrame().setShell(shell);
    }

    @Override
    public String toString() {
        return getClass().getName() + " { shell [" + shell + "] }";
    }

    /**
     * Factory method to create a new instance of SetShell.
     *
     * @param shell the shell to set for the current frame
     * @return a new SetShell instance
     */
    public static SetShell of(String shell) {
        return new SetShell(shell);
    }
}
