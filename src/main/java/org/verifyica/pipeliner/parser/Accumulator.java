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
     * Method to append a character
     *
     * @param c the character
     * @return the accumulator
     */
    public Accumulator append(char c) {
        stringBuilder.append(c);
        return this;
    }

    /**
     * Method to return if the accumulator has accumulated any characters
     *
     * @return true if the accumulator has accumulated any characters, else false
     */
    public boolean hasAccumulated() {
        return stringBuilder.length() > 0;
    }

    /**
     * Method to drain the accumulated characters
     *
     * @return the accumulated characters
     */
    public String drain() {
        if (stringBuilder.length() == 0) {
            throw new IllegalStateException("No characters to drain");
        }

        String result = stringBuilder.toString();
        stringBuilder.setLength(0);

        return result;
    }
}
