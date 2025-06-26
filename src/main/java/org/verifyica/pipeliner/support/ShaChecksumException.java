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

package org.verifyica.pipeliner.support;

/** Class to implement ChecksumException */
public class ShaChecksumException extends Exception {

    /**
     * Constructor
     *
     * @param message the message
     */
    public ShaChecksumException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param message the message
     * @param cause the cause
     */
    public ShaChecksumException(String message, Throwable cause) {
        super(message, cause);
    }
}
