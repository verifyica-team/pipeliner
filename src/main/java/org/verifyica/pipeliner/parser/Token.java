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

/** Class to implement Token */
public class Token {

    /**
     * Enum to implement token type
     */
    public enum Type {

        /**
         * Text token
         */
        TEXT,
        /**
         * Property token
         */
        PROPERTY,
        /**
         * Environment variable token
         */
        ENVIRONMENT_VARIABLE,
    }

    private final int position;
    private final String text;
    private final int length;
    private Token.Type type;
    private String value;

    /**
     * Constructor
     *
     * @param type the type
     * @param text the text
     * @param value the value
     */
    public Token(Token.Type type, String text, String value) {
        this(type, text, value, -1);
    }

    /**
     * Constructor
     *
     * @param type the type
     * @param text the text
     * @param value the value
     * @param position the position, may be -1
     */
    public Token(Type type, String text, String value, int position) {
        this.type = type;
        this.position = position;
        this.text = text;
        this.value = value;
        this.length = text.length();
    }

    /**
     * Method to get the type
     *
     * @return the type
     */
    public Token.Type getType() {
        return type;
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
     * Method to get the value
     *
     * @return the value
     */
    public String getValue() {
        return value;
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
     * Method to get the length
     *
     * @return the length
     */
    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "Token { type=[" + type + "] position=[" + position + "] text=[" + text + "] value=[" + value
                + "] length=[" + length + "] }";
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Token token = (Token) object;
        return Objects.equals(text, token.text) && Objects.equals(value, token.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, value);
    }
}
