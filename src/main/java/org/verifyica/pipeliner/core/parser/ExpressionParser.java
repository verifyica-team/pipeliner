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

import java.util.ArrayList;
import java.util.List;
import org.verifyica.pipeliner.core.statements.Expression;
import org.verifyica.pipeliner.core.statements.expression.LiteralExpression;
import org.verifyica.pipeliner.core.statements.expression.NullExpression;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * Utility class for parsing expressions from a {@code Line}.
 */
public class ExpressionParser {

    /**
     * Constructor
     */
    private ExpressionParser() {
        // INTENTIONALLY EMPTY
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
     * @throws SyntaxException if the string literal is not properly terminated
     */
    public static Expression parseExpression(Line line) {
        Token token = line.peek();
        if ("str".equals(token.lexeme)) {
            return parseRawString(line);
        } else {
            return parseQuotedOrUnquoted(line);
        }
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
    private static Expression parseQuotedOrUnquoted(Line line) {
        if (line == null) {
            throw new SyntaxException(null, "Expected expression, but got null line");
        }

        if (line.isEmpty()) {
            return new LiteralExpression("");
        }

        Token firstToken = line.peek();

        if (line.size() == 2) {
            if (firstToken.lexeme.equals("\"") && line.get(1).lexeme.equals("\"")) {
                return new LiteralExpression("");
            }

            if (firstToken.lexeme.equals("'") && line.get(1).lexeme.equals("'")) {
                return new LiteralExpression("");
            }
        }

        StringBuilder builder = new StringBuilder();

        // Quoted string case: first token is a quote
        if (firstToken.lexeme.equals("\"") || firstToken.lexeme.equals("'")) {
            String firstLexeme = firstToken.lexeme;
            Token token;
            Token lastToken = null;

            while (line.peek() != null) {
                token = line.consume();
                builder.append(token.lexeme);
                lastToken = token;
            }

            if (lastToken == null || !lastToken.lexeme.equals(firstLexeme)) {
                throw new SyntaxException(
                        line,
                        "unterminated string literal: missing closing " + firstLexeme + " at " + firstToken.location);
            }

            return new LiteralExpression(builder.toString());
        }

        // Unquoted string: just append all lexemes
        while (line.peek() != null) {
            builder.append(line.consume().lexeme);
        }

        return new LiteralExpression(builder.toString());
    }

    /**
     * Parses a raw string starting with "str" followed by a sequence of "#" characters.
     * The raw string continues until the same number of "#" characters is found at the end.
     *
     * @param line the line of tokens to parse
     * @return an Expression representing the raw string
     * @throws SyntaxException if the raw string is not properly terminated
     */
    private static Expression parseRawString(Line line) {
        Token strToken = line.consume();

        Token peek = line.peek();
        if (peek == null || !peek.lexeme.equals("#")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(strToken.lexeme);

            // Unquoted string: just append all lexemes
            while (line.peek() != null) {
                stringBuilder.append(line.consume().lexeme);
            }

            return new LiteralExpression(stringBuilder.toString());
        }

        // Count opening hashes
        int hashCount = 0;
        while (line.peek() != null && line.peek().lexeme.equals("#")) {
            line.consume();
            hashCount++;
        }

        if (hashCount == 0) {
            throw new SyntaxException(line, "expected '#' after 'str' at " + strToken.location);
        }

        List<Token> buffer = new ArrayList<>();
        List<Token> hashBuffer = new ArrayList<>();

        while (!line.isEmpty()) {
            Token token = line.peek();

            if (!token.lexeme.equals("#")) {
                flush(hashBuffer, buffer);
                buffer.add(line.consume());
                continue;
            }

            // Token is "#", collect possible delimiter match
            hashBuffer.clear();

            int lookahead = 0;
            while ((lookahead < hashCount)
                    && (line.peek(lookahead) != null)
                    && line.peek(lookahead).lexeme.equals("#")) {
                hashBuffer.add(line.peek(lookahead));
                lookahead++;
            }

            if (hashBuffer.size() == hashCount) {
                Token after = line.peek(lookahead);
                if (after == null || !after.lexeme.equals("#")) {
                    // Valid closing delimiter
                    for (int i = 0; i < hashCount; i++) {
                        line.consume(); // consume closing hashes
                    }
                    return new LiteralExpression(buildLexeme(buffer));
                }
            }

            // Not a valid delimiter — treat current # as content
            buffer.add(line.consume());
        }

        throw new SyntaxException(
                line,
                "unterminated raw string: expected closing " + "#".repeat(hashCount) + " after " + strToken.location);
    }

    private static void flush(List<Token> from, List<Token> to) {
        to.addAll(from);
        from.clear();
    }

    private static String buildLexeme(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens) {
            sb.append(token.lexeme);
        }
        return sb.toString();
    }
}
