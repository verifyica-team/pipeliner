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
 * Instruction to remove a variable
 */
public class RemoveVariable implements Instruction {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveVariable.class);

    /**
     * The variable name to remove
     */
    private final String name;

    /**
     * Constructor
     *
     * @param name the variable name
     */
    private RemoveVariable(String name) {
        this.name = name;
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("name = [%s]", name);

        // Remove the variable from the context
        context.getVariables().remove(name);
    }

    @Override
    public String toString() {
        return getClass().getName() + " { name [" + name + "] }";
    }

    /**
     * Factory method to create a new instance of SetVariable.
     *
     * @param name the variable name
     * @return a new RemoveVariable instance
     */
    public static RemoveVariable of(String name) {
        return new RemoveVariable(name);
    }
}
