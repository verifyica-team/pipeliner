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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.verifyica.pipeliner.common.Accumulator;
import org.verifyica.pipeliner.common.LRUCache;
import org.verifyica.pipeliner.core.Id;
import org.verifyica.pipeliner.core.Variable;
import org.verifyica.pipeliner.parser.tokens.Modifier;
import org.verifyica.pipeliner.parser.tokens.ParsedEnvironmentVariable;
import org.verifyica.pipeliner.parser.tokens.ParsedText;
import org.verifyica.pipeliner.parser.tokens.ParsedToken;
import org.verifyica.pipeliner.parser.tokens.ParsedVariable;

/** Class to implement Parser */
public class Parser {

    private static final List<ParsedToken> EMPTY_PARSED_TOKEN_LIST = Collections.unmodifiableList(new ArrayList<>());

    private static final LRUCache<String, List<ParsedToken>> TOKEN_LIST_CACHE = new LRUCache<>(1000);

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

        // Check if the input has already been parsed
        List<ParsedToken> parsedTokens = TOKEN_LIST_CACHE.get(input);
        if (parsedTokens != null) {
            // Return the parsed tokens
            return parsedTokens;
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
        parsedTokens = new ArrayList<>();

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

                    // Add the parsed variable token
                    parsedTokens.add(createParsedVariable(position, text));

                    break;
                }
                case ENVIRONMENT_VARIABLE_BRACES: {
                    // Text format ${foo}, so remove the ${ and } characters
                    String value = text.substring(2, lexerToken.getText().length() - 1);

                    // Add the ParedEnvironmentVariable
                    parsedTokens.add(ParsedEnvironmentVariable.builder()
                            .position(position)
                            .text(text)
                            .value(value)
                            .build());

                    break;
                }
                case ENVIRONMENT_VARIABLE: {
                    // Text format $foo, so remove the $ character
                    String value = text.substring(1);

                    // Add the ParedEnvironmentVariable
                    parsedTokens.add(ParsedEnvironmentVariable.builder()
                            .position(position)
                            .text(text)
                            .value(value)
                            .build());

                    break;
                }
                default: {
                    // Default to ParsedText
                    parsedTokens.add(
                            ParsedText.builder().position(position).text(text).build());

                    break;
                }
            }
        }

        // Cache the tokens
        TOKEN_LIST_CACHE.put(input, parsedTokens);

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
     * Method to create a new variable token
     *
     * @param position the position
     * @param text the text
     * @return a parsed variable token
     * @throws SyntaxException If the variable is invalid
     */
    private static ParsedVariable createParsedVariable(int position, String text) throws SyntaxException {
        ParsedVariable.Builder parsedVariableBuilder =
                ParsedVariable.builder().position(position).text(text);

        String value = text.substring(3, text.length() - 2).trim();

        Set<Modifier> modifiers = new HashSet<>();

        int modifierDelimiterIndex = value.lastIndexOf(ParsedVariable.MODIFIER_SEPARATOR);
        if (modifierDelimiterIndex != -1) {
            String modifierPrefix = value.substring(0, modifierDelimiterIndex);
            String[] modifierParts = modifierPrefix.split(ParsedVariable.MODIFIER_SEPARATOR);
            for (String modifierPart : modifierParts) {
                try {
                    if (Modifier.isValid(modifierPart)) {
                        modifiers.add(Modifier.valueOf(modifierPart.toUpperCase()));
                    } else {
                        throw new SyntaxException(
                                format("invalid modifier [%s] for variable [%s]", modifierPart, text));
                    }
                } catch (IllegalArgumentException e) {
                    throw new SyntaxException(format("invalid modifier [%s] for variable [%s]", modifierPart, text));
                }
            }

            value = value.substring(modifierDelimiterIndex + 1);
        }

        if (value.startsWith(ParsedVariable.SCOPE_SEPARATOR) || value.endsWith(ParsedVariable.SCOPE_SEPARATOR)) {
            throw new SyntaxException("invalid variable [" + text + "]");
        }

        if (value.contains(ParsedVariable.SCOPE_SEPARATOR + ParsedVariable.SCOPE_SEPARATOR)) {
            throw new SyntaxException("invalid variable [" + text + "]");
        }

        // Split the value by the SCOPE_SEPARATOR
        String[] parts = value.split(Pattern.quote(ParsedVariable.SCOPE_SEPARATOR));

        // Check parts that represent a scope
        if (parts.length > 1) {
            // Check parts that represent and id
            for (int i = 0; i < parts.length - 1; i++) {
                if (Id.isInvalid(parts[i])) {
                    throw new SyntaxException("invalid variable [" + text + "]");
                }
            }
        }

        // Check the last part that represents a value
        if (Variable.isInvalid(parts[parts.length - 1])) {
            throw new SyntaxException("invalid variable [" + text + "]");
        }

        String scope = null;

        // If there are more than one part, created a scoped variable
        if (parts.length > 1) {
            // Build the scope
            scope = String.join(ParsedVariable.SCOPE_SEPARATOR, Arrays.copyOf(parts, parts.length - 1));

            // The unscoped value is the last part
            value = parts[parts.length - 1];
        }

        return parsedVariableBuilder
                .scope(scope)
                .value(value)
                .modifiers(modifiers)
                .build();
    }
}
