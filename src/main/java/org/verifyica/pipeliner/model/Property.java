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

package org.verifyica.pipeliner.model;

import java.util.regex.Pattern;

/** Class to implement Property */
public class Property {

    private static final String REGEX = "^[a-zA-Z0-9_][a-zA-Z0-9-_.]*[a-zA-Z0-9_]$";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    /** Property scope separators */
    public static final String[] SCOPE_SEPARATORS = {".", "/"};

    /** Constructor */
    private Property() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to return if a string is a valid property name
     *
     * @param input the input string
     * @return true of the string is a valid property name, else false
     */
    public static boolean isValid(String input) {
        return PATTERN.matcher(input).matches();
    }

    /**
     * Method to return if a string is an invalid property name
     *
     * @param input the input string
     * @return true if the string is an invalid property name, else false
     */
    public static boolean isInvalid(String input) {
        return !isValid(input);
    }
}
