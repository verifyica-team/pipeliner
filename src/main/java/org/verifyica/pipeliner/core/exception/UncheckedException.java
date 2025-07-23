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

package org.verifyica.pipeliner.core.exception;

/**
 * UncheckedException is a runtime exception that can be used to wrap checked exceptions
 */
public class UncheckedException extends RuntimeException {

    /**
     * Constructor
     */
    public UncheckedException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message the message of the exception
     */
    public UncheckedException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause the cause of the exception
     */
    public UncheckedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     *
     * @param message the message of the exception
     * @param cause the cause of the exception
     */
    public UncheckedException(String message, Throwable cause) {
        super(message, cause);
    }
}
