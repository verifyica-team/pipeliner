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

import java.util.ArrayList;
import java.util.List;
import org.verifyica.pipeliner.core.exception.SyntaxException;
import org.verifyica.pipeliner.core.statement.Line;
import org.verifyica.pipeliner.core.statement.StatementParser;
import org.verifyica.pipeliner.core.statement.Token;

/**
 * Utility class for parsing expressions from a {@code Line}.
 */
public class ExpressionParser {

    /**
     * Constructor
     */
    private ExpressionParser() {
        // INTENTIONALLY BLANK
    }

    /**
     * Parses an optional expression from the given {@code Line}.
     * If the {@code Line} is null, it returns a NullExpression.
     * If the {@code Line} is empty, it returns an {@code LiteralExpression} with an empty string.
     * If the {@code Line} is not empty, it parses the expression
     *
     * @param line the line of tokens to parse
     * @return an Expression representing the parsed line, or a LiteralExpression with an empty string if the line is empty
     */
    public static Expression parseOptionalExpression(Line line) {
        if (line == null) {
            return NullExpression.SINGLETON;
        }

        if (line.isEmpty()) {
            return new LiteralExpression("");
        }

        return parseExpression(line);
    }

    /**
     * Parses an expression from the given line of tokens.
     *
     * @param line the line of tokens to parse
     * @return an Expression representing the parsed line
     * @throws IllegalArgumentException if the line is null or empty
     * @throws SyntaxException if the string literal is not properly terminated
     */
    public static Expression parseExpression(Line line) {
        return parseQuotedOrUnquoted(line);
    }

    /**
     * Parses a list of expressions from the given statementParser.
     * The parsing continues a line containing only the delimiter is encountered or EOF is reached.
     *
     * @param statementParser the statement parser to read from
     * @param delimiter the delimiter that indicates the end of the expression list
     * @return a list of parsed expressions
     * @throws SyntaxException if an unexpected end of input occurs while parsing
     */
    public static List<Expression> parseExpressionList(StatementParser statementParser, String delimiter) {
        List<Expression> expressions = new ArrayList<>();

        while (true) {
            Line line = statementParser.peekLine();

            if (line == null) {
                throw new SyntaxException("Unexpected end of input while parsing expression list");
            }

            if (line.isEmpty()) {
                statementParser.nextLine();
                continue;
            }

            Token firstToken = line.peek();
            if (firstToken == null) {
                throw new SyntaxException("Unexpected null token");
            }

            String firstLexeme = firstToken.lexeme;
            if (firstLexeme.startsWith("#") || firstLexeme.startsWith("//")) {
                // Skip comment lines
                statementParser.nextLine();
                continue;
            }

            if (line.size() == 1) {
                Token token = line.tokens().get(0);
                if (token.lexeme.equals(delimiter)) {
                    statementParser.nextLine();
                    break;
                }
            }

            line = statementParser.nextLine();
            expressions.add(parseQuotedOrUnquoted(line));
        }

        return expressions;
    }

    /**
     * Parses a quoted or unquoted expression from the given line.
     * If the first token is a quote, it parses until the matching closing quote.
     * If not, it treats the entire line as an unquoted string.
     *
     * @param line the line of tokens to parse
     * @return an Expression representing the parsed string
     * @throws SyntaxException if the string literal is not properly terminated
     */
    public static Expression parseQuotedOrUnquoted(Line line) {
        if (line == null || line.isEmpty()) {
            throw new SyntaxException("Expected expression, but got empty or null line");
        }

        Token firstToken = line.peek();
        if (firstToken == null) {
            throw new SyntaxException("Unexpected null token");
        }

        StringBuilder builder = new StringBuilder();

        // Quoted string case: first token is a quote
        if (firstToken.lexeme.equals("\"") || firstToken.lexeme.equals("'")) {
            String firstLexeme = firstToken.lexeme;
            Token token;
            Token lastToken = null;

            while ((token = line.next()) != null) {
                builder.append(token.lexeme);
                lastToken = token;
            }

            if (lastToken == null || !lastToken.lexeme.equals(firstLexeme)) {
                throw new SyntaxException(
                        "Unterminated string literal: missing closing " + firstLexeme + " at " + firstToken.location);
            }

            return new LiteralExpression(builder.toString());
        }

        // Unquoted string: just append all lexemes
        Token token;
        while ((token = line.next()) != null) {
            builder.append(token.lexeme);
        }

        return new LiteralExpression(builder.toString());
    }
}
