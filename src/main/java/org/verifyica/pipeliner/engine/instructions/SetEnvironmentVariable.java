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
import org.verifyica.pipeliner.support.PeekIterator;

/**
 * Instruction to set an environment variable in the context.
 */
public class SetEnvironmentVariable implements Instruction {

    private final String name;
    private final String value;

    /**
     * Constructor
     *
     * @param name the environment variable name
     * @param value the environment variable value
     */
    private SetEnvironmentVariable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        // Put the environment variable in the context
        context.getEnvironmentVariables().put(name, value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " { name [" + name + "] value [" + value + "] }";
    }

    /**
     * Factory method to create a new instance of SetEnvironmentVariable.
     *
     * @param name the environment variable name
     * @param value the environment variable value
     * @return a new SetEnvironmentVariable instance
     */
    public static SetEnvironmentVariable of(String name, String value) {
        return new SetEnvironmentVariable(name, value);
    }
}
