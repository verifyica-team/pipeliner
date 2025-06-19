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
 * Instruction to set a variable in the context.
 */
public class SetVariable implements Instruction {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SetVariable.class);

    /**
     * The variable name to set.
     */
    private final String name;

    /**
     * The variable value to set.
     */
    private final String value;

    /**
     * Constructor
     *
     * @param name the variable name
     * @param value the variable value
     */
    private SetVariable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("name = [%s]", name);
        LOGGER.trace("value = [%s]", value);

        // Put the variable in the context
        context.getVariables().put(name, value);
    }

    @Override
    public String toString() {
        return getClass().getName() + " { name [" + name + "] value [" + value + "] }";
    }

    /**
     * Factory method to create a new instance of SetVariable.
     *
     * @param name the variable name
     * @param value the variable value
     * @return a new SetVariable instance
     */
    public static SetVariable of(String name, String value) {
        return new SetVariable(name, value);
    }
}
