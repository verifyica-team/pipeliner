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

/** Class to implement Scanner */
public class CharacterStream {

    private final String input;
    private int position;

    /**
     * Constructor
     *
     * @param input the input string
     */
    private CharacterStream(String input) {
        this.input = input != null ? input : "";
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
        if (!hasNext()) {
            throw new IllegalStateException("No more characters to peek");
        }

        return input.charAt(position);
    }

    /**
     * Method to check if there is a character to read
     *
     * @return true if there is a character to read, else false
     */
    public boolean hasNext() {
        return position < input.length();
    }

    /**
     * Method to check if the next character is the given character
     *
     * @param c the character to check for
     * @return true if the next character matches, else false
     */
    public boolean hasNext(char c) {
        return hasNext() && peek() == c;
    }

    /**
     * Method to get the next character
     *
     * @return the next character\
     * @throws IllegalStateException if there are no more characters to read
     */
    public char next() {
        if (!hasNext()) {
            throw new IllegalStateException("No more characters to read");
        }

        return input.charAt(position++);
    }

    /**
     * Method to create a CharacterStream
     *
     * @param input the input string
     * @return a CharacterStream
     */
    public static CharacterStream of(String input) {
        return new CharacterStream(input);
    }
}
