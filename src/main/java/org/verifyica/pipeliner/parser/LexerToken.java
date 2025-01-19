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

package org.verifyica.pipeliner.parser;

import java.util.Objects;

/** Class to implement LexerToken */
public class LexerToken {

    /**
     * Enum to implement token type
     */
    public enum Type {

        /**
         * Backslash token
         */
        BACKSLASH,
        /**
         * Text token
         */
        TEXT,
        /**
         * Variable token
         */
        VARIABLE,
        /**
         * Environment variable token
         */
        ENVIRONMENT_VARIABLE,
    }

    private final Type type;
    private final int position;
    private final String text;
    private final int length;

    /**
     * Constructor
     *
     * @param type the type
     * @param position the position
     * @param text the text
     */
    public LexerToken(Type type, int position, String text) {
        this.type = type;
        this.position = position;
        this.text = text;
        this.length = text.length();
    }

    /**
     * Method to get the type
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Method to get the position
     *
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Method to get the text
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Method to get the text length
     *
     * @return the text length
     */
    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "ParsedToken { type=[" + type + "] position=[" + position + "] text=[" + text + "] }";
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LexerToken)) return false;
        LexerToken lexerToken = (LexerToken) object;
        return type == lexerToken.type && Objects.equals(text, lexerToken.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, text);
    }
}
