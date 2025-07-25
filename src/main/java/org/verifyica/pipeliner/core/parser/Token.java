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

package org.verifyica.pipeliner.core.parser;

/**
 * Represents a single lexical token.
 */
public class Token {

    /**
     * Represents the {@code Token} type
     */
    public enum Type {

        /**
         * Whitespace token.
         */
        WHITESPACE,

        /**
         * Literal token, such as a string or number.
         */
        LITERAL
    }

    /**
     * The type of this token.
     */
    public final Type type;

    /**
     * The lexeme of this token, which is the actual text matched by the lexer.
     */
    public final String lexeme;

    /**
     * The location in the source code where this token was found.
     */
    public final Location location;

    /**
     * Constructor
     *
     * @param type the type of the token
     * @param lexeme the lexeme of the token
     * @param location the position in the source code where this token was found
     */
    public Token(Type type, String lexeme, Location location) {
        this.type = type;
        this.lexeme = lexeme;
        this.location = location;
    }

    @Override
    public String toString() {
        return "Token{" + "type=" + type + ", location=" + location + ", lexeme='" + lexeme + '\'' + '}';
    }
}
