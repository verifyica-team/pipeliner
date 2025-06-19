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

import org.verifyica.pipeliner.Verbosity;
import org.verifyica.pipeliner.engine.Context;
import org.verifyica.pipeliner.engine.ExitException;
import org.verifyica.pipeliner.engine.Instruction;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.support.PeekIterator;

/**
 * Instruction to stop pipeline execution.
 */
public class Exit implements Instruction {

    /**
     * The logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Exit.class);

    /**
     * The instruction line
     */
    private final String line;

    /**
     * Constructor
     *
     * @param line the line
     */
    private Exit(String line) {
        this.line = line;
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("line [%s]", line);

        // Get the verbosity level
        Verbosity verbosity = context.getConsole().getVerbosity();

        // If verbosity is normal
        if (verbosity.isNormal()) {
            // Print the line to the console
            context.getConsole().println("@run %s", line);
        }

        try {
            // Parse the exit code
            int exitCode = Integer.parseInt(line.substring(line.indexOf(" ")).trim());

            // Throw a StopException with the parsed exit code
            throw new ExitException("", exitCode);
        } catch (NumberFormatException e) {
            // If parsing fails, throw a StopException with code 1
            throw new ExitException("", 1);
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + " { line [" + line + "] }";
    }

    /**
     * Factory method to create a new instance of PrintDirective.
     *
     * @param line the line
     * @return a new PrintDirective instance
     */
    public static Exit of(String line) {
        return new Exit(line);
    }
}
