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

import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * Parses a number from a {@link Line}.
 */
public class NumberParser {

    private static final NumberParser SINGLETON = new NumberParser();

    /**
     * Constructor
     */
    private NumberParser() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Parses the next token in the line if it matches the specified literal.
     *
     * @param line the line to parse
     * @return the lexeme of the token if it matches, or throws a SyntaxException if it does not
     */
    public long parse(Line line) {
        Token token = line.consume();

        if (token == null) {
            throw new SyntaxException(line, "Expected number but found end of line");
        }

        try {
            return Long.parseLong(token.lexeme);
        } catch (NumberFormatException e) {
            throw new SyntaxException(line, token.location + ": Expected number but found '" + token.lexeme + "'");
        }
    }

    /**
     * Factory method to create an instance of NumberParser.
     *
     * @return a singleton instance of NumberParser
     */
    public static NumberParser of() {
        return SINGLETON;
    }
}
