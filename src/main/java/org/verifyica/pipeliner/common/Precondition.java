/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

/** Class to implement Precondition */
public class Precondition {

    /** Constructor */
    private Precondition() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to validate an Object is not null, throwing an IllegalArgumentException if it is null
     *
     * @param object the object
     * @param message the message
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Method to validate a String is not null and not blank, throwing an IllegalArgumentException
     * if it is null or blank
     *
     * @param string the string
     * @param nullMessage the null message
     * @param blankMessage the blank message
     */
    public static void notBlank(String string, String nullMessage, String blankMessage) {
        if (string == null) {
            throw new IllegalArgumentException(nullMessage);
        }

        if (string.trim().isEmpty()) {
            throw new IllegalArgumentException(blankMessage);
        }
    }

    /**
     * Method to validate a condition is true, throwing an IllegalArgumentException if it is false
     *
     * @param condition the condition
     * @param message the message
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Method to validate a condition is false, throwing an IllegalArgumentException if it is true
     *
     * @param condition the condition
     * @param message the message
     */
    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
