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

package org.verifyica.pipeliner.exception;

/**
 * An exception that indicates the pipeline should halt execution.
 */
public class HaltException extends RuntimeException {

    /**
     * The qualifier of the halt, which can be used to indicate success or failure.
     */
    private final String qualifier;

    /**
     * The exit code to use when halting execution.
     */
    private final int exitCode;

    /**
     * Constructor
     *
     * @param qualifier the status qualifier for the halt, e.g., "ok" or "error"
     * @param exitCode the exit code to use when halting execution
     */
    public HaltException(String qualifier, int exitCode) {
        super();
        this.qualifier = qualifier;
        this.exitCode = exitCode;
    }

    /**
     * Constructor
     *
     * @param qualifier the status qualifier for the halt, e.g., "ok" or "error"
     * @param exitCode the exit code to use when halting execution
     * @param message the exit message to display
     */
    public HaltException(String qualifier, int exitCode, String message) {
        super(message);
        this.qualifier = qualifier;
        this.exitCode = exitCode;
    }

    /**
     * Gets the qualifier of this halt.
     *
     * @return the status qualifier, e.g., "ok" or "error"
     */
    public String getQualifier() {
        return qualifier;
    }

    /**
     * Gets the exit code associated with this halt.
     *
     * @return the exit code
     */
    public int getExitCode() {
        return exitCode;
    }
}
