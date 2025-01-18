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

    @SuppressWarnings("SpellCheckingInspection")
    private static final String ALPHA_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final String DIGIT_CHARACTERS = "0123456789";

    private static final String ENVIRONMENT_VARIABLE_BEGIN_CHARACTERS = ALPHA_CHARACTERS + "_";

    private static final String ENVIRONMENT_VARIABLE_REMAINING_CHARACTERS =
            ENVIRONMENT_VARIABLE_BEGIN_CHARACTERS + DIGIT_CHARACTERS;

    private static final String PROPERTY_BEGIN_CHARACTERS = ALPHA_CHARACTERS + DIGIT_CHARACTERS;

    private static final String PROPERTY_BEGIN_CHARACTERS_2 = ALPHA_CHARACTERS + DIGIT_CHARACTERS + "_";

    private static final String PROPERTY_MIDDLE_CHARACTERS = ALPHA_CHARACTERS + DIGIT_CHARACTERS + "#_.-/";

    private static final String PROPERTY_END_CHARACTERS = ALPHA_CHARACTERS + DIGIT_CHARACTERS + "_";

    private static final String DOLLAR_LEFT_BRACE = "${";

    private static final List<ParsedToken> EMPTY_PARSED_TOKEN_LIST = Collections.unmodifiableList(new ArrayList<>());

    private static final LRUCache<String, List<ParsedToken>> PARSED_TOKEN_LIST_CACHE = new LRUCache<>(1000);

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
    public static List<ParsedToken> parse(String input) throws SyntaxException {
        if (input == null || input.isEmpty()) {
            return EMPTY_PARSED_TOKEN_LIST;
        }

        // Check if the input has already been tokenized
        List<ParsedToken> parsedTokens = PARSED_TOKEN_LIST_CACHE.get(input);
        if (parsedTokens != null) {
            // Return the cached tokens
            return parsedTokens;
        }

        // Tokenize the input
        List<LexerToken> lexerTokens = new Lexer(input).tokenize();

        /*
         * Phase 1: Merge...
         *
         * BACKSLASH + PROPERTY into a TEXT token
         * BACKSLASH + HASH_PROPERTY into a TEXT token
         * BACKSLASH + ENVIRONMENT into a TEXT token
         */
        List<LexerToken> phase1LexerTokens = new ArrayList<>();
        for (int i = 0; i < lexerTokens.size(); i++) {
            LexerToken current = lexerTokens.get(i);

            // Check if current is a BACKSLASH and the next token is a PROPERTY, HASH_PROPERTY, or ENVIRONMENT_VARIABLE
            if (current.getType() == LexerToken.Type.BACKSLASH
                    && i + 1 < lexerTokens.size()
                    && (lexerTokens.get(i + 1).getType() == LexerToken.Type.PROPERTY
                            || lexerTokens.get(i + 1).getType() == LexerToken.Type.HASH_PROPERTY
                            || lexerTokens.get(i + 1).getType() == LexerToken.Type.ENVIRONMENT_VARIABLE)) {

                // Merge BACKSLASH + PROPERTY/HASH_PROPERTY/ENVIRONMENT_VARIABLE into a single TEXT token
                String mergedText = current.getText() + lexerTokens.get(i + 1).getText();
                phase1LexerTokens.add(new LexerToken(LexerToken.Type.TEXT, current.getPosition(), mergedText));

                // Skip the next token (PROPERTY/HASH_PROPERTY/ENVIRONMENT_VARIABLE)
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
            if (lexerToken.getType() == LexerToken.Type.TEXT || lexerToken.getType() == LexerToken.Type.BACKSLASH) {
                // Start a new sequence if this is the first token being merged
                if (accumulator.isEmpty()) {
                    currentPosition = lexerToken.getPosition();
                }
                // Accumulate TEXT and BACKSLASH content
                accumulator.accumulate(lexerToken.getText());
            } else {
                // Flush accumulated TEXT/BACKSLASH tokens
                if (accumulator.isNotEmpty()) {
                    phase2LexerTokens.add(new LexerToken(LexerToken.Type.TEXT, currentPosition, accumulator.drain()));
                }
                // Add non-TEXT/BACKSLASH token as is
                phase2LexerTokens.add(lexerToken);
            }
        }

        // Add any remaining accumulated TEXT/BACKSLASH tokens
        if (accumulator.isNotEmpty()) {
            phase2LexerTokens.add(new LexerToken(LexerToken.Type.TEXT, currentPosition, accumulator.drain()));
        }

        // Create the list to hold the tokens
        parsedTokens = new ArrayList<>();

        for (LexerToken lexerToken : phase2LexerTokens) {
            // Get the token type
            LexerToken.Type type = lexerToken.getType();

            // Get the token position
            int position = lexerToken.getPosition();

            // Get the token text
            String text = lexerToken.getText();

            switch (type) {
                case PROPERTY:
                case HASH_PROPERTY: {
                    // Remove the ${ and } characters
                    String value =
                            text.substring(3, lexerToken.getText().length() - 2).trim();

                    // Check if the property value is invalid
                    if (isInvalidProperty(value)) {
                        throw new SyntaxException(format(
                                "invalid property [%s] at position [%d]",
                                lexerToken.getText(), lexerToken.getPosition()));
                    }

                    // Add the PROPERTY token
                    parsedTokens.add(new ParsedToken(ParsedToken.Type.PROPERTY, position, text, value));

                    break;
                }
                case ENVIRONMENT_VARIABLE: {
                    String value;

                    // Get the value of the environment variable
                    if (text.startsWith(DOLLAR_LEFT_BRACE)) {
                        // Remove the ${ and } characters
                        value = text.substring(2, lexerToken.getText().length() - 1);
                    } else {
                        // Remove the $ character
                        value = text.substring(1);
                    }

                    // Check if the environment variable is invalid
                    if (isInvalidEnvironmentVariable(value)) {
                        throw new SyntaxException(format(
                                "invalid environment variable [%s] at position [%d]",
                                lexerToken.getText(), lexerToken.getPosition()));
                    }

                    // Add the ENVIRONMENT_VARIABLE token
                    parsedTokens.add(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, position, text, value));

                    break;
                }
                default: {
                    // Add the TEXT token
                    parsedTokens.add(new ParsedToken(ParsedToken.Type.TEXT, position, text, text));
                    break;
                }
            }
        }

        // Cache the tokens
        PARSED_TOKEN_LIST_CACHE.put(input, parsedTokens);

        return parsedTokens;
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

    /**
     * Method to check if a character is in a string
     *
     * @param c the character
     * @param string the string of characters
     * @return true if the character is in the string, else false
     */
    private static boolean inString(char c, String string) {
        // Check if the character is in the set
        for (int i = 0; i < string.length(); i++) {
            if (c == string.charAt(i)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Method to check if a property value is invalid
     *
     * @param value the value
     * @return true if the value is invalid, else false
     */
    private static boolean isInvalidProperty(String value) {
        return !isValidProperty(value);
    }

    /**
     * Method to check if a property value is valid
     *
     * @param value the value
     * @return true if the value is valid, else false
     */
    private static boolean isValidProperty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        // Convert the value to a char array for easier processing
        char[] characters = value.toCharArray();

        // Special case: if the length is 1, validate only the start character
        if (characters.length == 1) {
            return inString(characters[0], PROPERTY_BEGIN_CHARACTERS);
        }

        // Validate the first character
        if (!inString(characters[0], PROPERTY_BEGIN_CHARACTERS_2)) {
            return false;
        }

        // Special case: if the length is 2, validate only the end characters
        if (characters.length == 2) {
            return inString(characters[1], PROPERTY_END_CHARACTERS);
        }

        // Validate the middle characters
        for (int i = 1; i < characters.length - 2; i++) {
            if (!inString(characters[i], PROPERTY_MIDDLE_CHARACTERS)) {
                return false;
            }
        }

        // Validate the last character
        return inString(characters[characters.length - 1], PROPERTY_END_CHARACTERS);
    }

    /**
     * Method to check if an environment variable is invalid
     *
     * @param value the value
     * @return true if the value is invalid, else false
     */
    private static boolean isInvalidEnvironmentVariable(String value) {
        return !isValidEnvironmentVariable(value);
    }

    /**
     * Method to check if an environment variable is valid
     *
     * @param value the value
     * @return true if the value is valid, else false
     */
    private static boolean isValidEnvironmentVariable(String value) {
        // If the value is null or empty, it is invalid
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        // Convert the value to a char array for easier processing
        char[] characters = value.toCharArray();

        // Special case: if the length is 1, validate only the start character
        if (characters.length == 1) {
            return inString(characters[0], ENVIRONMENT_VARIABLE_BEGIN_CHARACTERS);
        }

        // Validate the middle characters
        for (int i = 1; i < characters.length; i++) {
            if (!inString(characters[i], ENVIRONMENT_VARIABLE_REMAINING_CHARACTERS)) {
                return false;
            }
        }

        return true;
    }
}
