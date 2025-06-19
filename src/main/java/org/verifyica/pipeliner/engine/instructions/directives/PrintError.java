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

package org.verifyica.pipeliner.engine.instructions.directives;

import org.verifyica.pipeliner.engine.Context;
import org.verifyica.pipeliner.engine.Instruction;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.support.PeekIterator;

/**
 * Instruction to print an error message.
 */
public class PrintError implements Instruction {

    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintError.class);

    /**
     * Prefix for the directive
     */
    public static final String PREFIX = "--print:error";

    /**
     * The instruction line
     */
    private final String line;

    /**
     * Constructor
     *
     * @param line the line
     */
    private PrintError(String line) {
        this.line = line;
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("line [%s]", line);

        // Remove the prefix
        String message = line.substring((PREFIX + " ").length()).trim();

        LOGGER.trace("message [%s]", message);

        // Resolve variables in the message
        String resolvedMessage = context.resolveVariables(message).replace("\\$", "$");

        LOGGER.trace("resolvedMessage [%s]", resolvedMessage);

        // Print the message to the console
        context.getConsole().error(resolvedMessage);
    }

    @Override
    public String toString() {
        return getClass().getName() + " { line [" + line + "] }";
    }

    /**
     * Factory method to create a new instance of Print.
     *
     * @param line the line
     * @return a new Print instance
     */
    public static PrintError of(String line) {
        return new PrintError(line);
    }
}
