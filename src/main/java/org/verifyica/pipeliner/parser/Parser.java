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

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.verifyica.pipeliner.common.LRUCache;

/** Class to implement Parser */
public class Parser {

    private static final String ALPHA_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final String DIGIT_CHARACTERS = "0123456789";

    private static final String ENVIRONMENT_VARIABLE_BEGIN_CHARACTERS = ALPHA_CHARACTERS + "_";

    private static final String ENVIRONMENT_VARIABLE_REMAINING_CHARACTERS =
            ENVIRONMENT_VARIABLE_BEGIN_CHARACTERS + DIGIT_CHARACTERS;

    private static final String PROPERTY_BEGIN_CHARACTERS = ALPHA_CHARACTERS + DIGIT_CHARACTERS;

    private static final String PROPERTY_BEGIN_CHARACTERS_2 = ALPHA_CHARACTERS + DIGIT_CHARACTERS + "_";

    private static final String PROPERTY_MIDDLE_CHARACTERS = ALPHA_CHARACTERS + DIGIT_CHARACTERS + "_.-/";

    private static final String PROPERTY_END_CHARACTERS = ALPHA_CHARACTERS + DIGIT_CHARACTERS + "_";

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
     * @throws ParserException if the input string is invalid
     */
    public static List<Token> parse(String input) throws ParserException {
        if (input == null || input.isEmpty()) {
            return EMPTY_TOKEN_LIST;
        }

        // Check the cache first
        List<Token> tokens = TOKEN_LIST_CACHE.get(input);
        if (tokens != null) {
            // Cache it, return the tokens
            return tokens;
        }

        // Parse the input string
        tokens = mergeAdjacentTextTokens(parseTokens(input));

        // Cache the tokens
        TOKEN_LIST_CACHE.put(input, tokens);

        return tokens;
    }

    /**
     * Method to validate the input string
     *
     * @param input the input string
     * @throws ParserException if the input string is invalid
     */
    public static void validate(String input) throws ParserException {
        // Parse the input string only, no need to merge
        // adjacent TEXT tokens for validation
        parseTokens(input);
    }

    /**
     * Method to parse the input string to a list of tokens
     * <p>The list may contain adjacent TEXT tokens</p>
     *
     * @param input the input string
     * @return a list of tokens
     * @throws ParserException If the input string is invalid
     */
    private static List<Token> parseTokens(String input) throws ParserException {
        List<Token> tokens = new ArrayList<>();
        Accumulator accumulator = new Accumulator();
        Scanner scanner = new Scanner(input);

        while (scanner.hasNext()) {
            switch (scanner.peek()) {
                case '\\': {
                    accumulator.append(scanner.next());
                    if (scanner.hasNext() && scanner.peek() == '$') {
                        accumulator.append(scanner.next());
                        while (scanner.hasNext() && scanner.peek() != '$' && scanner.peek() != '\\') {
                            accumulator.append(scanner.next());
                        }
                        String text = accumulator.drain();
                        tokens.add(new Token(Token.Type.TEXT, text, text, scanner.getPosition()));
                    }
                    break;
                }
                case '$': {
                    if (accumulator.hasAccumulated()) {
                        String text = accumulator.drain();
                        tokens.add(new Token(Token.Type.TEXT, text, text, scanner.getPosition()));
                    }
                    accumulator.append(scanner.next());
                    if (scanner.hasNext() && scanner.peek() == '{') {
                        accumulator.append(scanner.next());
                        if (scanner.hasNext() && scanner.peek() == '{') {
                            // Possible PROPERTY token
                            accumulator.append(scanner.next());
                            while (scanner.hasNext() && scanner.peek() != '}') {
                                accumulator.append(scanner.next());
                            }
                            if (scanner.hasNext() && scanner.peek() == '}') {
                                accumulator.append(scanner.next());
                                if (scanner.hasNext() && scanner.peek() == '}') {
                                    // PROPERTY token
                                    accumulator.append(scanner.next());
                                    String text = accumulator.drain();
                                    String value =
                                            text.substring(3, text.length() - 2).trim();
                                    if (!value.isEmpty()) {
                                        if (isValidProperty(value)) {
                                            tokens.add(
                                                    new Token(Token.Type.PROPERTY, text, value, scanner.getPosition()));
                                        } else {
                                            throw new ParserException(format(
                                                    "invalid property [%s] at position [%d]",
                                                    text, scanner.getPosition()));
                                        }
                                    } else {
                                        if (!tokens.isEmpty()) {
                                            Token previousToken = tokens.get(tokens.size() - 1);
                                            if (previousToken.getType() == Token.Type.TEXT) {
                                                // Append the new text to the existing TEXT token
                                                String updatedText = previousToken.getText() + text;
                                                Token updatedToken = new Token(
                                                        Token.Type.TEXT,
                                                        updatedText,
                                                        updatedText,
                                                        scanner.getPosition());
                                                // Replace the previous TEXT token
                                                tokens.set(tokens.size() - 1, updatedToken);
                                            } else {
                                                // Add a new TEXT token if the previous token is not TEXT
                                                tokens.add(
                                                        new Token(Token.Type.TEXT, text, text, scanner.getPosition()));
                                            }
                                        } else {
                                            // Add the TEXT token if tokens list is empty
                                            tokens.add(new Token(Token.Type.TEXT, text, text, scanner.getPosition()));
                                        }
                                    }
                                } else {
                                    if (scanner.hasNext()) {
                                        // Accumulate text
                                        accumulator.append(scanner.next());
                                    }
                                }
                            } else {
                                if (scanner.hasNext()) {
                                    // Accumulate text
                                    accumulator.append(scanner.next());
                                }
                            }
                        } else {
                            // Possible ENVIRONMENT_VARIABLE with curly braces
                            if (scanner.hasNext() && inSet(scanner.peek(), ENVIRONMENT_VARIABLE_BEGIN_CHARACTERS)) {
                                accumulator.append(scanner.next());
                                while (scanner.hasNext()
                                        && inSet(scanner.peek(), ENVIRONMENT_VARIABLE_REMAINING_CHARACTERS)) {
                                    accumulator.append(scanner.next());
                                }
                                if (scanner.hasNext() && scanner.peek() == '}') {
                                    // ENVIRONMENT_VARIABLE with curly braces
                                    accumulator.append(scanner.next());
                                    String text = accumulator.drain();
                                    String value = text.substring(2, text.length() - 1);
                                    tokens.add(new Token(
                                            Token.Type.ENVIRONMENT_VARIABLE, text, value, scanner.getPosition()));
                                } else {
                                    if (scanner.hasNext()) {
                                        // Accumulate text
                                        accumulator.append(scanner.next());
                                    }
                                }
                            } else {
                                if (scanner.hasNext()) {
                                    // Accumulate text
                                    accumulator.append(scanner.next());
                                }
                            }
                        }
                    } else {
                        // Possible ENVIRONMENT_VARIABLE without curly braces
                        if (scanner.hasNext() && inSet(scanner.peek(), ENVIRONMENT_VARIABLE_BEGIN_CHARACTERS)) {
                            // ENVIRONMENT_VARIABLE without curly braces
                            accumulator.append(scanner.next());
                            while (scanner.hasNext()
                                    && inSet(scanner.peek(), ENVIRONMENT_VARIABLE_REMAINING_CHARACTERS)) {
                                accumulator.append(scanner.next());
                            }
                            String text = accumulator.drain();
                            String value = text.substring(1);
                            tokens.add(new Token(Token.Type.ENVIRONMENT_VARIABLE, text, value, scanner.getPosition()));
                        } else {
                            if (scanner.hasNext()) {
                                // Accumulate text
                                accumulator.append(scanner.next());
                            }
                        }
                    }
                    break;
                }
                default: {
                    if (scanner.hasNext()) {
                        // Accumulate text
                        accumulator.append(scanner.next());
                    }
                    break;
                }
            }
        }

        // Add any remaining text as a TEXT token
        if (accumulator.hasAccumulated()) {
            String text = accumulator.drain();
            tokens.add(new Token(Token.Type.TEXT, text, text, scanner.getPosition()));
        }

        return tokens;
    }

    /**
     * Method to merge adjacent TEXT tokens
     *
     * @param tokens the list of tokens
     * @return the list of tokens with adjacent TEXT tokens merged
     */
    private static List<Token> mergeAdjacentTextTokens(List<Token> tokens) {
        List<Token> result = new ArrayList<>();
        StringBuilder mergedText = new StringBuilder();
        int currentPosition = -1;

        for (Token token : tokens) {
            // If the token is TEXT, accumulate it
            if (token.getType() == Token.Type.TEXT) {
                if (mergedText.length() > 0) {
                    // If there's already merged text, append the new text to it
                    mergedText.append(token.getText());
                } else {
                    // If no merged text yet, start with the current token
                    mergedText.append(token.getText());
                    currentPosition = token.getPosition(); // Store the position of the first TEXT token
                }
            } else {
                // If we encounter a non-TEXT token and there is merged text, add it to the result
                if (mergedText.length() > 0) {
                    // Add the merged text as a TEXT token to the result
                    result.add(
                            new Token(Token.Type.TEXT, mergedText.toString(), mergedText.toString(), currentPosition));

                    // Reset the merged text and position for the next group
                    mergedText.setLength(0);
                    currentPosition = -1;
                }

                // Add the non-TEXT token to the result list
                result.add(token);
            }
        }

        // If there's any remaining text that hasn't been merged
        if (mergedText.length() > 0) {
            // Add the remaining merged text as a TEXT token to the result
            result.add(new Token(Token.Type.TEXT, mergedText.toString(), mergedText.toString(), currentPosition));
        }

        return result;
    }

    /**
     * Method to check if a character is in a set of characters
     *
     * @param c the character
     * @param string the string of characters
     * @return true if the character is in the set, else false
     */
    private static boolean inSet(char c, String string) {
        for (int i = 0; i < string.length(); i++) {
            if (c == string.charAt(i)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Method to validate a property value, allowing '/' for scoped properties
     *
     * @param value the value
     * @return true if the value is valid property value, else false
     */
    private static boolean isValidProperty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        // Convert the value to a char array for easier processing
        char[] characters = value.toCharArray();

        // Special case: if the length is 1, validate only the start character
        if (characters.length == 1) {
            return inSet(characters[0], PROPERTY_BEGIN_CHARACTERS);
        }

        // Validate the first character
        if (!inSet(characters[0], PROPERTY_BEGIN_CHARACTERS_2)) {
            return false;
        }

        // Special case: if the length is 2, validate only the end characters
        if (characters.length == 2) {
            return inSet(characters[1], PROPERTY_END_CHARACTERS);
        }

        // Validate the middle characters
        for (int i = 1; i < characters.length - 2; i++) {
            if (!inSet(characters[i], PROPERTY_MIDDLE_CHARACTERS)) {
                return false;
            }
        }

        // Validate the last character
        return inSet(characters[characters.length - 1], PROPERTY_END_CHARACTERS);
    }
}
