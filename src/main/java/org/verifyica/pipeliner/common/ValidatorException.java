/*
 * Copyright (C) 2024-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.common;

import static java.lang.String.format;

/** Class to implement ValidatorException */
public class ValidatorException extends Exception {

    /**
     * Constructor
     *
     * @param message message
     */
    public ValidatorException(String message) {
        super(message);
    }

    /**
     * Method to throw a ValidatorException
     *
     * @param message message
     * @throws ValidatorException ValidatorException
     */
    public static void propagate(String message) throws ValidatorException {
        throw new ValidatorException(message);
    }

    /**
     * Method to throw a ValidatorException
     *
     * @param format format
     * @param objects objects
     * @throws ValidatorException ValidatorException
     */
    public static void propagate(String format, Object... objects) throws ValidatorException {
        throw new ValidatorException(format(format, objects));
    }
}
