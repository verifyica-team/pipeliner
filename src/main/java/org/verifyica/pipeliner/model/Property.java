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

/** Class to implement Property */
public class Property {

    @SuppressWarnings("SpellCheckingInspection")
    private static final String ALPHA_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final String DIGIT_CHARACTERS = "0123456789";

    private static final String PROPERTY_BEGIN_CHARACTERS = ALPHA_CHARACTERS + DIGIT_CHARACTERS;

    private static final String PROPERTY_BEGIN_CHARACTERS_2 = ALPHA_CHARACTERS + DIGIT_CHARACTERS + "_";

    private static final String PROPERTY_MIDDLE_CHARACTERS = ALPHA_CHARACTERS + DIGIT_CHARACTERS + "_.-";

    private static final String PROPERTY_END_CHARACTERS = ALPHA_CHARACTERS + DIGIT_CHARACTERS + "_";

    /** Property scope separators */
    public static final String[] SCOPE_SEPARATORS = {".", "/"};

    /** Constructor */
    private Property() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to return if a string is a valid property name
     *
     * @param input the input
     * @return true of the string is a valid property name, else false
     */
    /**
     * Method to check if a property value is valid
     *
     * @param value the value
     * @return true if the value is valid, else false
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        // Convert the value to a char array for easier processing
        char[] characters = value.toCharArray();

        // Special case: if the length is 1, validate only the start character
        if (characters.length == 1) {
            return inString(characters[0], PROPERTY_BEGIN_CHARACTERS);
        }

        // Validate the first character
        if (!inString(characters[0], PROPERTY_BEGIN_CHARACTERS_2)) {
            return false;
        }

        // Special case: if the length is 2, validate only the end characters
        if (characters.length == 2) {
            return inString(characters[1], PROPERTY_END_CHARACTERS);
        }

        // Validate the middle characters
        for (int i = 1; i < characters.length - 2; i++) {
            if (!inString(characters[i], PROPERTY_MIDDLE_CHARACTERS)) {
                return false;
            }
        }

        // Validate the last character
        return inString(characters[characters.length - 1], PROPERTY_END_CHARACTERS);
    }

    /**
     * Method to return if a string is an invalid property name
     *
     * @param value the value
     * @return true if the value is an invalid property name, else false
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
