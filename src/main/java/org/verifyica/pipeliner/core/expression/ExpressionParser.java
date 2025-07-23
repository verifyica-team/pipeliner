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

package org.verifyica.pipeliner.core.expression;

import java.util.List;
import org.verifyica.pipeliner.core.exception.SyntaxException;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.Token;

/**
 * Utility class for parsing string expressions from sequences of tokens.
 */
public class ExpressionParser {

    /**
     * Constructor
     */
    private ExpressionParser() {
        // INTENTIONALLY BLANK
    }

    /**
     * Parses a string expression from the given line of tokens.
     *
     * @param line the line of tokens to parse
     * @return a LiteralExpression containing the parsed string
     * @throws IllegalArgumentException if the line is null or empty
     * @throws SyntaxException if the string literal is not properly terminated
     */
    public static Expression parseStringExpression(Line line) {
        if (line == null || line.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }

        List<Token> tokens = line.tokens();
        StringBuilder sb = new StringBuilder();

        for (Token token : tokens) {
            sb.append(token.lexeme);
        }

        String input = sb.toString();
        char first = input.charAt(0);
        char last = input.charAt(input.length() - 1);

        if ((first == '"' || first == '\'') && first == last) {
            input = input.substring(1, input.length() - 1);
        } else if (first == '"' || first == '\'') {
            Token firstToken = tokens.get(0);
            throw new SyntaxException("Unterminated string literal at " + firstToken.location);
        } else {
            input = input.trim();
        }

        return new LiteralExpression(input);
    }

    /**
     * Parses an optional string expression from the given line of tokens.
     * If the line is empty or null, returns a LiteralExpression with an empty string.
     *
     * @param line the line of tokens to parse
     * @return a LiteralExpression containing the parsed string or an empty string
     */
    public static Expression parseOptionalStringExpression(Line line) {
        if (line == null || line.isEmpty()) {
            return new LiteralExpression("");
        }

        return parseStringExpression(line);
    }
}
