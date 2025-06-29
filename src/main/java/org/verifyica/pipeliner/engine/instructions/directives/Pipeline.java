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

import org.verifyica.pipeliner.CLI;
import org.verifyica.pipeliner.engine.Context;
import org.verifyica.pipeliner.engine.EngineException;
import org.verifyica.pipeliner.engine.Instruction;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.support.PeekIterator;
import org.verifyica.pipeliner.support.Precondition;
import org.verifyica.pipeliner.support.QuotedStringTokenizer;

/**
 * Instruction to execute a pipeline.
 */
public class Pipeline implements Instruction {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);

    /**
     * Prefix for the directive
     */
    public static final String PREFIX = "--pipeline";

    /**
     * The instruction line
     */
    private final String line;

    /**
     * Constructor
     *
     * @param line the line
     */
    private Pipeline(String line) {
        this.line = line;
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("line = [%s]", line);

        Precondition.notNull(context, "context is null");
        Precondition.notNull(peekIterator, "peekIterator is null");

        String line = this.line.substring((PREFIX + " ").length()).trim();

        LOGGER.trace("line [%s]", line);

        // Execute the engine and return the exit code
        int exitCode = new CLI().execute(QuotedStringTokenizer.tokenize(line).toArray(new String[0]));

        if (exitCode != 0) {
            throw new EngineException("execution failed");
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
    public static Pipeline of(String line) {
        return new Pipeline(line);
    }
}
