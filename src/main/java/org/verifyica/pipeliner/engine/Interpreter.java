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

package org.verifyica.pipeliner.engine;

import java.util.List;
import org.verifyica.pipeliner.support.PeekIterator;

/**
 * Class responsible for executing the instructions.
 */
public class Interpreter {

    private final Context context;

    /**
     * Constructor
     *
     * @param context the context in which the instructions will be executed.
     */
    public Interpreter(Context context) {
        this.context = context;
    }

    /**
     * Executes the instructions read from the BufferedReader.
     *
     * @param instructions a list of instructions to execute.
     * @throws Throwable if an error occurs during execution.
     */
    public void execute(List<Instruction> instructions) throws Throwable {
        // Create an iterator for the instructions
        PeekIterator<Instruction> peekIterator = new PeekIterator<>(instructions.iterator());

        // Iterate through the instructions
        while (peekIterator.hasNext()) {
            // Get the next instruction
            Instruction instruction = peekIterator.next();

            // Remove the instruction
            peekIterator.remove();

            try {
                // Execute the instruction
                instruction.execute(context, peekIterator);
            } catch (ExitException e) {
                // If a StopException is thrown, rethrow it
                throw e;
            } catch (Throwable t) {
                throw new InterpreterException("exception executing instruction", instruction, t);
            }
        }
    }
}
