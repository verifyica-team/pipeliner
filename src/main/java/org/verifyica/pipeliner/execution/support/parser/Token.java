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

package org.verifyica.pipeliner.execution.support.parser;

import java.util.Objects;

/**
 * Class to implement Token
 */
public class Token {

    /**
     * Enum to define the type of the token
     */
    public enum Type {

        /**
         * Text token
         */
        TEXT,
        /**
         * Property token
         */
        PROPERTY
    }

    private final Type type;
    private final String token;
    private final String value;

    /**
     * Constructor for PropertyParserToken
     *
     * @param type  type
     * @param token token
     */
    public Token(Type type, String token) {
        this.type = type;
        this.token = token;

        if (type == Type.PROPERTY) {
            this.value = token.substring(3, token.length() - 2).trim();
        } else {
            this.value = token;
        }
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
     * Method to get the token
     *
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * Method to get the value
     *
     * <p>If the token type == PROPERTY, returns the token value</p>
     * <p>Otherwise, returns the token itself</p>
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "PropertyParserToken{" + "type=" + type + ", token='" + token + '\'' + ", value='" + value + '\'' + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Token token1 = (Token) object;
        return type == token1.type && Objects.equals(token, token1.token) && Objects.equals(value, token1.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, token, value);
    }
}
