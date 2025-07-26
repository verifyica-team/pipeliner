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
 * Parser that optionally matches a token of a specific type in a line.
 */
public class OptionalParser {

    private final Token.Type type;

    /**
     * Constructor
     *
     * @param type the token type
     */
    private OptionalParser(Token.Type type) {
        this.type = type;
    }

    /**
     * Parses the next token in the line if it matches the specified type.
     *
     * @param line the line to parse
     * @return the lexeme of the token if it matches, or null if it does not
     */
    public String parse(Line line) {
        Token token = line.peek();
        if (token == null) {
            return null;
        }

        if (type.equals(token.type)) {
            return line.consume().lexeme;
        }

        return null;
    }

    /**
     * Factory method to create an instance of OptionalParser for a specific token type.
     *
     * @param type the token type to match
     * @return an OptionalParser instance for the specified type
     */
    public static OptionalParser of(Token.Type type) {
        return new OptionalParser(type);
    }
}
