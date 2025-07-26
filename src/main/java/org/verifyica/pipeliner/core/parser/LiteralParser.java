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
 * Parses a literal value from a line of code.
 */
public class LiteralParser {

    private final String allowed;

    /**
     * Constructor
     *
     * @param allowed the allowed literal value
     */
    private LiteralParser(String allowed) {
        this.allowed = allowed;
    }

    /**
     * Parses the next token in the line if it matches the specified literal.
     *
     * @param line the line to parse
     * @return the lexeme of the token if it matches, or throws a SyntaxException if it does not
     */
    public String parse(Line line) {
        Token token = line.peek();

        if (token == null) {
            throw new SyntaxException(line.location() + ": Expected literal '" + allowed + "' but found end of line");
        }

        if (!allowed.equals(token.lexeme)) {
            throw new SyntaxException(
                    line.location() + ": Expected literal '" + allowed + "' but found '" + token.lexeme + "'");
        }

        return line.consume().lexeme;
    }

    /**
     * Factory method to create an instance of LiteralParser for a specific literal.
     *
     * @param allowed the literal value to match
     * @return a LiteralParser instance for the specified literal
     */
    public static LiteralParser of(String allowed) {
        return new LiteralParser(allowed);
    }
}
