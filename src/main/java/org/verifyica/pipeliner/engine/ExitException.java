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
 * Exception thrown to stop the execution of a pipeline.
 */
public class ExitException extends RuntimeException {

    /**
     * The exit code
     */
    private final int exitCode;

    /**
     * Constructor
     *
     * @param message  the message
     * @param exitCode the exit code
     */
    public ExitException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    /**
     * Gets the exit code.
     *
     * @return the exit code
     */
    public int getExitCode() {
        return exitCode;
    }
}
