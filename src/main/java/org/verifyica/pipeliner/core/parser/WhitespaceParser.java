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
 * A parser that validates the next token in a line is whitespace.
 */
public class WhitespaceParser {

    private static final WhitespaceParser SINGLETON = new WhitespaceParser();

    /**
     * Constructor
     */
    private WhitespaceParser() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Validates that the line has no remaining tokens.
     *
     * @param line the line to validate
     * @throws SyntaxException if any tokens remain
     */
    public void parse(Line line) {
        Token token = line.consume();

        if (token == null) {
            throw new SyntaxException(line, "expected whitespace but found end of line");
        }

        if (token.type != Token.Type.WHITESPACE) {
            throw new SyntaxException(line, "expected whitespace but found '" + token.lexeme + "'");
        }
    }

    /**
     * Factory method to create an instance of WhitespaceParser.
     *
     * @return the singleton instance of WhitespaceParser
     */
    public static WhitespaceParser singleton() {
        return SINGLETON;
    }
}
