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

package org.verifyica.pipeliner.parser.tokens;

import java.util.HashSet;
import java.util.Set;

/** Enum to implement Modifier */
public enum Modifier {

    /**
     * Required
     */
    REQUIRED("required");

    private static final Set<String> VALUES = new HashSet<>();

    private final String value;

    static {
        // Populate the set of values
        for (Modifier modifier : values()) {
            VALUES.add(modifier.value);
        }
    }

    /**
     * Constructor
     *
     * @param value the value
     */
    Modifier(String value) {
        this.value = value;
    }

    /**
     * Method to get the value
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Method to check if the value is a valid modifier
     *
     * @param value the value
     * @return true if the value is a valid modifier, false otherwise
     */
    public static boolean isValid(String value) {
        return VALUES.contains(value);
    }

    /**
     * Method to check if the value is an invalid modifier
     *
     * @param value the value
     * @return true if the value is an invalid modifier, false otherwise
     */
    public static boolean isInvalid(String value) {
        return !VALUES.contains(value);
    }
}
