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

package org.verifyica.pipeliner.parser;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.verifyica.pipeliner.common.Accumulator;
import org.verifyica.pipeliner.common.LRUCache;
import org.verifyica.pipeliner.common.StateMachine;
import org.verifyica.pipeliner.core.Id;
import org.verifyica.pipeliner.core.Variable;
import org.verifyica.pipeliner.parser.lexer.Lexer;
import org.verifyica.pipeliner.parser.lexer.VariableLexer;
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
        List<Lexer.Token> tokens = new Lexer(input).tokenize();

        /*
         * Phase 1: Merge
         */
        List<Lexer.Token> phase1Tokens = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Lexer.Token current = tokens.get(i);

            // Check if current is a BACKSLASH and the next token is a VARIABLE, ENVIRONMENT_VARIABLE_BRACES or
            // ENVIRONMENT_VARIABLE
            if (current.getType() == Lexer.Token.Type.BACKSLASH
                    && i + 1 < tokens.size()
                    && (tokens.get(i + 1).getType() == Lexer.Token.Type.VARIABLE
                            || tokens.get(i + 1).getType() == Lexer.Token.Type.ENVIRONMENT_VARIABLE_BRACES
                            || tokens.get(i + 1).getType() == Lexer.Token.Type.ENVIRONMENT_VARIABLE)) {
                // Merge BACKSLASH + VARIABLE tokens into a single TEXT token
                String mergedText = current.getText() + tokens.get(i + 1).getText();
                phase1Tokens.add(new Lexer.Token(Lexer.Token.Type.TEXT, current.getPosition(), mergedText));

                // Skip the next token
                i++;
            } else {
                // Add the token as is
                phase1Tokens.add(current);
            }
        }

        /*
         * Phase 2: Merge adjacent TEXT and BACKSLASH tokens
         */
        List<Lexer.Token> phase2Tokens = new ArrayList<>();
        Accumulator accumulator = new Accumulator();
        int currentPosition = -1;

        for (Lexer.Token token : phase1Tokens) {
            switch (token.getType()) {
                case TEXT:
                case BACKSLASH:
                    // Start a new sequence if this is the first token being merged
                    if (accumulator.isEmpty()) {
                        currentPosition = token.getPosition();
                    }

                    // Accumulate TEXT and BACKSLASH content
                    accumulator.accumulate(token.getText());

                    break;

                default:
                    // Flush accumulated text as a TEXT token
                    if (accumulator.isNotEmpty()) {
                        phase2Tokens.add(new Lexer.Token(Lexer.Token.Type.TEXT, currentPosition, accumulator.drain()));
                    }

                    // Add non-TEXT/BACKSLASH token
                    phase2Tokens.add(token);

                    break;
            }
        }

        // Add any remaining accumulated text as a TEXT token
        if (accumulator.isNotEmpty()) {
            phase2Tokens.add(new Lexer.Token(Lexer.Token.Type.TEXT, currentPosition, accumulator.drain()));
        }

        // Create the list to hold the tokens
        parsedTokens = new ArrayList<>();

        for (Lexer.Token token : phase2Tokens) {
            // Get the token type
            Lexer.Token.Type type = token.getType();

            // Get the token position
            int position = token.getPosition();

            // Get the token text
            String text = token.getText();

            switch (type) {
                case VARIABLE: {
                    // Text format ${{ ws* foo ws* }}, so remove the ${{ and }} characters and trim

                    // Add the parsed variable token
                    parsedTokens.add(createParsedVariable(position, text));

                    break;
                }
                case ENVIRONMENT_VARIABLE_BRACES: {
                    // Text format ${foo}, so remove the ${ and } characters
                    String value = text.substring(2, token.getText().length() - 1);

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
        // Create a new builder for the parsed variable
        ParsedVariable.Builder parserVariableBuilder =
                ParsedVariable.builder().position(position).text(text);

        // Remove the ${{ and }} characters and trim
        String value = text.substring(3, text.length() - 2).trim();

        // Get the starting position of the variable value
        int startingPosition = text.indexOf(value);

        // Create a new lexer for the variable value
        VariableLexer variableLexer = new VariableLexer(value);

        // Tokenize the variable value
        List<VariableLexer.Token> tokens = variableLexer.tokenize();

        // Create a new state machine
        StateMachine<VariableLexer.Token.Type> stateMachine = new StateMachine<>();

        // Add valid transitions to the state machine
        stateMachine.addTransitions(
                null, VariableLexer.Token.Type.MODIFIER, VariableLexer.Token.Type.SCOPE, VariableLexer.Token.Type.TEXT);

        // Add valid transitions to the state machine
        stateMachine.addTransitions(
                VariableLexer.Token.Type.MODIFIER,
                VariableLexer.Token.Type.MODIFIER,
                VariableLexer.Token.Type.SCOPE,
                VariableLexer.Token.Type.TEXT);

        // Add valid transitions to the state machine
        stateMachine.addTransitions(
                VariableLexer.Token.Type.SCOPE, VariableLexer.Token.Type.SCOPE, VariableLexer.Token.Type.TEXT);

        // Process the tokens
        for (VariableLexer.Token token : tokens) {
            // Validate the transition
            if (!stateMachine.transition(token.getType())) {
                throw new SyntaxException(
                        format("invalid variable syntax at position [%s] in [%s]", token.getPosition(), text));
            }

            // Process the token
            switch (token.getType()) {
                case MODIFIER: {
                    // Get the modifier text
                    String modifier = token.getText();

                    // Validate the modifier
                    if (Modifier.isInvalid(modifier)) {
                        throw new SyntaxException(format(
                                "invalid modifier [%s] at position [%s] in [%s]",
                                modifier, token.getPosition() + startingPosition, text));
                    }

                    // Add the modifier
                    parserVariableBuilder.addModifier(Modifier.valueOf(modifier.toUpperCase()));

                    break;
                }
                case SCOPE: {
                    // Get the scope text
                    String scope = token.getText();

                    // Validate the scope text
                    if (Id.isInvalid(scope)) {
                        throw new SyntaxException(format(
                                "invalid scope [%s] at position [%s] in [%s]",
                                scope, token.getPosition() + startingPosition, text));
                    }

                    // Add the scope
                    parserVariableBuilder.addScope(scope);

                    break;
                }
                case TEXT: {
                    // Validate the variable text
                    if (Variable.isInvalid(token.getText())) {
                        throw new SyntaxException(format(
                                "invalid variable syntax at position [%s] in [%s]",
                                token.getPosition() + startingPosition, text));
                    }

                    // Set the value
                    parserVariableBuilder.value(token.getText());

                    break;
                }
                default: {
                    throw new SyntaxException(format(
                            "invalid variable syntax at position [%s] in [%s]",
                            token.getPosition() + startingPosition, text));
                }
            }
        }

        // Validate the final state
        if (stateMachine.currentState() != VariableLexer.Token.Type.TEXT) {
            throw new SyntaxException(format(
                    "invalid variable syntax at position [%s] in [%s]",
                    tokens.get(tokens.size() - 1).getPosition() + startingPosition, text));
        }

        // Build and return the parsed variable
        return parserVariableBuilder.build();
    }
}
