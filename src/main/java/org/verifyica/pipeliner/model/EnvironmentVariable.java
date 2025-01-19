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

/** Class to implement EnvironmentVariableName */
public class EnvironmentVariable {

    @SuppressWarnings("SpellCheckingInspection")
    private static final String ALPHA_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final String DIGIT_CHARACTERS = "0123456789";

    private static final String ENVIRONMENT_VARIABLE_BEGIN_CHARACTERS = ALPHA_CHARACTERS + "_";

    private static final String ENVIRONMENT_VARIABLE_REMAINING_CHARACTERS =
            ENVIRONMENT_VARIABLE_BEGIN_CHARACTERS + DIGIT_CHARACTERS;

    /** Constructor */
    private EnvironmentVariable() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to return if a string is a valid environment variable name
     *
     * @param value the value
     * @return true if the value is a valid environment variable name, else false
     */
    public static boolean isValid(String value) {
        // If the value is null or empty, it is invalid
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        // Convert the value to a char array for easier processing
        char[] characters = value.toCharArray();

        // Special case: if the length is 1, validate only the start character
        if (characters.length == 1) {
            return inString(characters[0], ENVIRONMENT_VARIABLE_BEGIN_CHARACTERS);
        }

        // Validate the middle characters
        for (int i = 1; i < characters.length; i++) {
            if (!inString(characters[i], ENVIRONMENT_VARIABLE_REMAINING_CHARACTERS)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Method to return if a string is an invalid environment variable name
     *
     * @param value the value
     * @return true if the value is an invalid environment variable name, else false
     */
    public static boolean isInvalid(String value) {
        return !isValid(value);
    }

    /**
     * Method to check if a character is in a string
     *
     * @param c the character
     * @param string the string of characters
     * @return true if the character is in the string, else false
     */
    private static boolean inString(char c, String string) {
        // Check if the character is in the set
        for (int i = 0; i < string.length(); i++) {
            if (c == string.charAt(i)) {
                return true;
            }
        }

        return false;
    }
}
