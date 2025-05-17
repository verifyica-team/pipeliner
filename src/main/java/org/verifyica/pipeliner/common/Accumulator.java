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

package org.verifyica.pipeliner.common;

/** Class to implement Accumulator */
public class Accumulator {

    private final StringBuilder stringBuilder;

    /**
     * Constructor
     */
    public Accumulator() {
        stringBuilder = new StringBuilder();
    }

    /**
     * Method to add a character to the accumulator
     *
     * @param c the character
     * @throws IllegalArgumentException if the character is null
     */
    public void accumulate(char c) {
        if (c == 0) {
            throw new IllegalArgumentException("c is null");
        }

        // Append the character
        stringBuilder.append(c);
    }

    /**
     * Method to add a string to the accumulator
     *
     * @param string the string
     * @throws IllegalArgumentException if the string is null
     */
    public void accumulate(String string) {
        if (string == null) {
            throw new IllegalArgumentException("string is null");
        }

        // Append the string
        stringBuilder.append(string);
    }

    /**
     * Method to return if the accumulator is empty
     *
     * @return true if the accumulator is empty, else false
     */
    public boolean isEmpty() {
        return stringBuilder.length() == 0;
    }

    /**
     * Method to return if the accumulator is not empty
     *
     * @return true if the accumulator is not empty, else false
     */
    public boolean isNotEmpty() {
        return stringBuilder.length() != 0;
    }

    /**
     * Method to get the length of the accumulated characters
     *
     * @return the length of the accumulated characters
     */
    public int length() {
        return stringBuilder.length();
    }

    /**
     * Method to drain the accumulated characters, resetting the accumulator
     * <p>If there are no characters to drain, return the default value</p>
     *
     * @param defaultValue the default value
     * @return the accumulated string or the default value
     */
    public String drain(String defaultValue) {
        if (stringBuilder.length() == 0) {
            return defaultValue;
        }

        return drain();
    }

    /**
     * Method to drain the accumulated characters, resetting the accumulator
     *
     * @return the accumulated string
     * @throws IllegalStateException if the accumulator is empty
     */
    public String drain() {
        if (stringBuilder.length() == 0) {
            throw new IllegalStateException("No characters to drain");
        }

        // Drain the accumulated characters
        String result = stringBuilder.toString();

        // Reset the accumulator
        stringBuilder.setLength(0);

        return result;
    }
}
