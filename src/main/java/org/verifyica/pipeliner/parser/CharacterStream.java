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

package org.verifyica.pipeliner.parser;

/** Class to implement CharacterStream */
public class CharacterStream {

    private final char[] characters;
    private int position;

    /**
     * Constructor
     *
     * @param input the input string
     */
    private CharacterStream(String input) {
        this.characters = input.toCharArray();
        this.position = 0;
    }

    /**
     * Method to get the current position
     *
     * @return the current position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Method to peek at the next character
     *
     * @return the next character
     * @throws IllegalStateException if there are no more characters to peek
     */
    public char peek() {
        if (position >= characters.length) {
            throw new IllegalStateException("No more characters to peek");
        }

        return characters[position];
    }

    /**
     * Method to check if there is a character to read
     *
     * @return true if there is a character to read, else false
     */
    public boolean hasNext() {
        return position < characters.length;
    }

    /**
     * Method to get the next character
     *
     * @return the next character
     * @throws IllegalStateException if there are no more characters to read
     */
    public char next() {
        if (position >= characters.length) {
            throw new IllegalStateException("No more characters to read");
        }

        return characters[position++];
    }

    /**
     * Method to create a CharacterStream
     *
     * @param input the input string
     * @return a CharacterStream
     */
    public static CharacterStream fromString(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input is null");
        }

        return new CharacterStream(input);
    }
}
