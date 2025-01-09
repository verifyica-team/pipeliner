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

package org.verifyica.pipeliner.tokenizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement Tokenizer2 */
public class Tokenizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tokenizer.class);

    private static final List<Token> EMPTY_TOKENS = Collections.unmodifiableList(new ArrayList<>());

    /** Constructor */
    private Tokenizer() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to tokenize a string
     *
     * @param input the input string
     * @return a list of tokens
     * @throws TokenizerException If an error occurs during tokenization
     */
    public static List<Token> tokenize(String input) throws TokenizerException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("tokenizer class [%s]", Tokenizer.class.getName());
            LOGGER.trace("string [%s]", input);
        }

        // If the input string is null or empty, return an empty list (short circuit)
        if (input == null || input.isEmpty()) {
            return EMPTY_TOKENS;
        }

        // Phase 1: Find all environment variable and property tokens
        List<Token> phase1Tokens = phase1(input);

        // Phase 2: process the tokens and add any TEXT tokens
        List<Token> phase2Tokens = phase2(phase1Tokens, input);

        // Phase 3: process the tokens and set the value for each token
        List<Token> phase3Tokens = phase3(phase2Tokens);

        // Phase 4: merge adjacent TEXT tokens
        List<Token> tokens = phase4(phase3Tokens);

        if (LOGGER.isTraceEnabled()) {
            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                LOGGER.trace(
                        "token[%d] type [%s] text [%s] value [%s]",
                        i, token.getType(), token.getText(), token.getValue());
            }
        }

        return tokens;
    }

    /**
     * Method to validate a string
     *
     * @param input the input string
     * @throws TokenizerException If an error occurs during tokenization
     */
    public static void validate(String input) throws TokenizerException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("validating input [%s]", input);
        }

        // Tokenize the input string to validate it
        tokenize(input);
    }

    /**
     * Phase 1: Find all ENVIRONMENT_VARIABLE, PROPERTY, and TEXT tokens
     *
     * @param input the input string
     * @return A list of tokens
     */
    private static List<Token> phase1(String input) {
        // Order matters since the regular expressions are not mutually exclusive

        // Find all environment variables of the form $VAR
        List<Token> environmentVariablesA =
                TokenMatcher.findMatches(input, "(?<!\\\\)\\$[a-zA-Z_][a-zA-Z0-9_]*", Token.Type.ENVIRONMENT_VARIABLE);

        // Find all environment variables of the form ${VAR}
        List<Token> environmentVariablesB =
                TokenMatcher.findMatches(input, "\\$\\{[a-zA-Z_][a-zA-Z0-9_]*}", Token.Type.ENVIRONMENT_VARIABLE);

        // Find all properties of the form of ${{ VAR }} and ${{VAR}}
        List<Token> properties = TokenMatcher.findMatches(
                input,
                "(?<!\\\\)\\$\\{\\{\\s*([a-zA-Z0-9-_][a-zA-Z0-9-_.]*[a-zA-Z0-9-_])\\s*\\}\\}",
                Token.Type.PROPERTY);

        // Find all escaped properties of the form \${{VAR}} treating them as TEXT tokens
        List<Token> escapedProperties = TokenMatcher.findMatches(
                input, "\\\\\\$\\{\\{\\s*([a-zA-Z0-9-][a-zA-Z0-9-_.]*)\\s*\\}\\}", Token.Type.TEXT);

        // Combine all the tokens
        List<Token> tokens = new ArrayList<>();
        tokens.addAll(environmentVariablesA);
        tokens.addAll(environmentVariablesB);
        tokens.addAll(properties);
        tokens.addAll(escapedProperties);

        // Sort the tokens by position
        tokens.sort(Comparator.comparingInt(Token::getPosition));

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("phase1 tokens ...");

            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                LOGGER.trace(
                        "token[%d] type [%s] text [%s] value [%s]",
                        i, token.getType(), token.getText(), token.getValue());
            }
        }

        return tokens;
    }

    /**
     * Phase 2: process the tokens and add any TEXT tokens
     *
     * @param tokens The list of tokens
     * @param input the input string
     * @return A list of tokens
     */
    private static List<Token> phase2(List<Token> tokens, String input) {
        List<Token> result = new ArrayList<>();

        int currentPosition = 0;

        // Iterate over the tokens
        for (Token token : tokens) {
            // Get the position and length of the token
            int position = token.getPosition();
            int length = token.getLength();

            // If there is text before the token, create a TEXT token for it
            if (currentPosition < position) {
                String text = input.substring(currentPosition, position);
                result.add(new Token(Token.Type.TEXT, text, currentPosition));
            }

            // Add the token itself
            result.add(token);

            // Move the current position past this token
            currentPosition = position + length;
        }

        // Add any remaining text after the last token as a final TEXT token
        if (currentPosition < input.length()) {
            String substring = input.substring(currentPosition);
            result.add(new Token(Token.Type.TEXT, substring, currentPosition));
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("phase2 tokens ...");

            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                LOGGER.trace(
                        "token[%d] type [%s] text [%s] value [%s]",
                        i, token.getType(), token.getText(), token.getValue());
            }
        }

        return result;
    }

    /**
     * Phase 3: process the tokens and set the value for each token
     *
     * @param tokens The list of tokens
     * @return A list of tokens
     */
    private static List<Token> phase3(List<Token> tokens) {
        for (Token token : tokens) {
            switch (token.getType()) {
                case ENVIRONMENT_VARIABLE: {
                    String text = token.getText();

                    if (text.startsWith("${") && text.endsWith("}")) {
                        // Matches the form ${VAR}, so remove the ${ and } for the value
                        token.setValue(text.substring(2, text.length() - 1));
                    } else {
                        // Matches the form $VAR, so remove the $ for the value
                        token.setValue(text.substring(1));
                    }

                    break;
                }
                case PROPERTY: {
                    String text = token.getText();

                    // Matches the form ${{VAR}}, so remove the ${{ and }} for the value
                    token.setValue(text.substring(3, text.length() - 2).trim());

                    break;
                }
                default: {
                    // For TEXT tokens, the value is the same as the text
                    token.setValue(token.getText());
                }
            }
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("phase3 tokens ...");

            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                LOGGER.trace(
                        "token[%d] type [%s] text [%s] value [%s]",
                        i, token.getType(), token.getText(), token.getValue());
            }
        }

        return tokens;
    }

    /**
     * Phase 4: merge adjacent TEXT tokens
     *
     * @param tokens The list of tokens
     * @return A list of tokens
     */
    private static List<Token> phase4(List<Token> tokens) {
        List<Token> result = new ArrayList<>();
        StringBuilder mergedText = new StringBuilder();

        for (Token token : tokens) {
            // If the token is TEXT, accumulate it
            if (token.getType() == Token.Type.TEXT) {
                if (mergedText.length() > 0) {
                    // If there's already merged text, append the new text to it
                    mergedText.append(token.getText());
                } else {
                    // If no merged text yet, start with the current token
                    mergedText.append(token.getText());
                }
            } else {
                // If we encounter a non-TEXT token and there is merged text, add it to the result
                if (mergedText.length() > 0) {
                    // Add the merged text as a TEXT token to the result
                    result.add(new Token(Token.Type.TEXT, mergedText.toString(), mergedText.toString()));

                    // Reset the merged text for the next group
                    mergedText.setLength(0);
                }

                // Add the non-TEXT token to the result list
                result.add(token);
            }
        }

        // If there's any remaining text that hasn't been merged
        if (mergedText.length() > 0) {
            // Add the remaining merged text as a TEXT token to the result
            result.add(new Token(Token.Type.TEXT, mergedText.toString(), mergedText.toString()));
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("phase4 tokens ...");

            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                LOGGER.trace(
                        "token[%d] type [%s] text [%s] value [%s]",
                        i, token.getType(), token.getText(), token.getValue());
            }
        }

        return result;
    }
}
