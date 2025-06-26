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

/**
 * Exception class for handling error when running an instruction.
 */
public class InterpreterException extends RuntimeException {

    /**
     * The instruction that caused the exception.
     */
    private final Instruction instruction;

    /**
     * Constructor
     *
     * @param message the message
     * @param instruction the instruction that caused the exception
     * @param throwable the cause of the exception
     */
    public InterpreterException(String message, Instruction instruction, Throwable throwable) {
        super(message, throwable);

        // Set the instruction that caused the exception
        this.instruction = instruction;
    }

    /**
     * Get the instruction that caused the exception.
     *
     * @return the instruction that caused the exception, or null if not specified
     */
    public Instruction getInstruction() {
        return instruction;
    }
}
