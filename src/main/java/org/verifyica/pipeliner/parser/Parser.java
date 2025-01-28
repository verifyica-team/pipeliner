/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.verifyica.pipeliner.common.Accumulator;
import org.verifyica.pipeliner.common.LRUCache;
import org.verifyica.pipeliner.parser.tokens.EnvironmentVariableToken;
import org.verifyica.pipeliner.parser.tokens.TextToken;
import org.verifyica.pipeliner.parser.tokens.Token;
import org.verifyica.pipeliner.parser.tokens.VariableToken;

/** Class to implement Parser */
public class Parser {

    private static final List<Token> EMPTY_TOKEN_LIST = Collections.unmodifiableList(new ArrayList<>());

    private static final LRUCache<String, List<Token>> TOKEN_LIST_CACHE = new LRUCache<>(1000);

    /**
     * Constructor
     */
    private Parser() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to parse the input string
     *
     * @param input the input string
     * @return a list of tokens
     * @throws SyntaxException If the input string contains invalid syntax
     */
    public static List<Token> parse(String input) throws SyntaxException {
        if (input == null || input.isEmpty()) {
            return EMPTY_TOKEN_LIST;
        }

        // Check if the input has already been parsed
        List<Token> tokens = TOKEN_LIST_CACHE.get(input);
        if (tokens != null) {
            // Return the parsed tokens
            return tokens;
        }

        // Tokenize the input
        List<LexerToken> lexerTokens = new Lexer(input).tokenize();

        /*
         * Phase 1: Merge
         */
        List<LexerToken> phase1LexerTokens = new ArrayList<>();
        for (int i = 0; i < lexerTokens.size(); i++) {
            LexerToken current = lexerTokens.get(i);

            // Check if current is a BACKSLASH and the next token is a VARIABLE, ENVIRONMENT_VARIABLE_BRACES or
            // ENVIRONMENT_VARIABLE
            if (current.getType() == LexerToken.Type.BACKSLASH
                    && i + 1 < lexerTokens.size()
                    && (lexerTokens.get(i + 1).getType() == LexerToken.Type.VARIABLE
                            || lexerTokens.get(i + 1).getType() == LexerToken.Type.ENVIRONMENT_VARIABLE_BRACES
                            || lexerTokens.get(i + 1).getType() == LexerToken.Type.ENVIRONMENT_VARIABLE)) {
                // Merge BACKSLASH + VARIABLE tokens into a single TEXT token
                String mergedText = current.getText() + lexerTokens.get(i + 1).getText();
                phase1LexerTokens.add(new LexerToken(LexerToken.Type.TEXT, current.getPosition(), mergedText));

                // Skip the next token
                i++;
            } else {
                // Add the token as is
                phase1LexerTokens.add(current);
            }
        }

        /*
         * Phase 2: Merge adjacent TEXT and BACKSLASH tokens
         */
        List<LexerToken> phase2LexerTokens = new ArrayList<>();
        Accumulator accumulator = new Accumulator();
        int currentPosition = -1;

        for (LexerToken lexerToken : phase1LexerTokens) {
            switch (lexerToken.getType()) {
                case TEXT:
                case BACKSLASH:
                    // Start a new sequence if this is the first token being merged
                    if (accumulator.isEmpty()) {
                        currentPosition = lexerToken.getPosition();
                    }

                    // Accumulate TEXT and BACKSLASH content
                    accumulator.accumulate(lexerToken.getText());

                    break;

                default:
                    // Flush accumulated text as a TEXT token
                    if (accumulator.isNotEmpty()) {
                        phase2LexerTokens.add(
                                new LexerToken(LexerToken.Type.TEXT, currentPosition, accumulator.drain()));
                    }

                    // Add non-TEXT/BACKSLASH token
                    phase2LexerTokens.add(lexerToken);

                    break;
            }
        }

        // Add any remaining accumulated text as a TEXT token
        if (accumulator.isNotEmpty()) {
            phase2LexerTokens.add(new LexerToken(LexerToken.Type.TEXT, currentPosition, accumulator.drain()));
        }

        // Create the list to hold the tokens
        tokens = new ArrayList<>();

        for (LexerToken lexerToken : phase2LexerTokens) {
            // Get the token type
            LexerToken.Type type = lexerToken.getType();

            // Get the token position
            int position = lexerToken.getPosition();

            // Get the token text
            String text = lexerToken.getText();

            switch (type) {
                case VARIABLE: {
                    // Text format ${{ ws* foo ws* }}, so remove the ${{ and }} characters and trim
                    String value =
                            text.substring(3, lexerToken.getText().length() - 2).trim();

                    // Add the VARIABLE token
                    tokens.add(VariableToken.create(position, text, value));

                    break;
                }
                case ENVIRONMENT_VARIABLE_BRACES: {
                    // Text format ${foo}, so remove the ${ and } characters
                    String value = text.substring(2, lexerToken.getText().length() - 1);

                    // Add the ENVIRONMENT_VARIABLE token
                    tokens.add(EnvironmentVariableToken.create(position, text, value));

                    break;
                }
                case ENVIRONMENT_VARIABLE: {
                    // Text format $foo, so remove the $ character
                    String value = text.substring(1);

                    // Add the ENVIRONMENT_VARIABLE token
                    tokens.add(EnvironmentVariableToken.create(position, text, value));

                    break;
                }
                default: {
                    // Default to a TEXT token
                    tokens.add(new TextToken(position, text));

                    break;
                }
            }
        }

        // Cache the tokens
        TOKEN_LIST_CACHE.put(input, tokens);

        return tokens;
    }

    /**
     * Method to validate the input string
     *
     * @param input the input string
     * @throws SyntaxException If the input string contains invalid syntax
     */
    public static void validate(String input) throws SyntaxException {
        // Parse the input string to validate it
        parse(input);
    }
}
