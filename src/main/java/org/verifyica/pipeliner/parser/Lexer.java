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
import java.util.List;

/** Class to implement Lexer */
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class Lexer {

    private static final char BACKSPACE = '\\';

    private static final char DOLLAR = '$';

    private static final char HASH = '#';

    private static final char LEFT_BRACE = '{';

    private static final char RIGHT_BRACE = '}';

    private static final char UNDERSCORE = '_';

    private final CharacterStream characterStream;
    private final Accumulator accumulator;

    /**
     * Constructor
     *
     * @param input the input
     */
    public Lexer(String input) {
        this.characterStream = CharacterStream.fromString(input);
        this.accumulator = new Accumulator();
    }

    /**
     * Method to tokenize the input
     *
     * @return a list of tokens
     */
    public List<LexerToken> tokenize() {
        List<LexerToken> tokens = new ArrayList<>();
        LexerToken token;

        while (true) {
            // Parse the next token
            token = parse();
            if (token == null) {
                break;
            }

            // Add the token
            tokens.add(token);
        }

        return tokens;
    }

    /**
     * Method to parse the next token
     *
     * @return the next token
     */
    private LexerToken parse() {
        LexerToken token = null;

        if (characterStream.hasNext()) {
            // Get the next character
            char character = characterStream.next();

            // Accumulate the character
            accumulator.accumulate(character);

            switch (character) {
                case BACKSPACE: {
                    token = parseBackslashSequence();
                    break;
                }
                case DOLLAR: {
                    token = parseDollarSequence();
                    break;
                }
                    /*
                    case HASH: {
                        token = parseHashSequence();
                        break;
                    }
                    */
                default: {
                    token = parseTextSequence();
                    break;
                }
            }
        }

        // Validate the accumulator is empty
        if (accumulator.isNotEmpty()) {
            throw new IllegalStateException("Accumulator should be empty");
        }

        return token;
    }

    /**
     * Method to parse a backslash sequence
     *
     * @return the token
     */
    private LexerToken parseBackslashSequence() {
        // Drain the accumulator
        String text = accumulator.drain();

        // Return a BACKSLASH token
        return new LexerToken(LexerToken.Type.BACKSLASH, characterStream.getPosition() - text.length(), text);
    }

    /**
     * Method to parse a dollar sequence
     *
     * @return the token
     */
    private LexerToken parseDollarSequence() {
        if (characterStream.hasNext() && characterStream.peek() == LEFT_BRACE) {
            // Parse an opening brace sequence
            return parseOpeningBrace();
        } else if (characterStream.hasNext()
                && (characterStream.peek() == UNDERSCORE || Character.isLetter(characterStream.peek()))) {
            // Parse an environment variable sequence
            return parseEnvironmentVariable();
        } else {
            // Parse a text sequence
            return parseTextSequence();
        }
    }

    /*
     * Method to parse a hash character sequence
     *
     * @return the token
     */
    /*
    private LexerToken parseHashSequence() {
        // While we have characters and we haven't reached a special character
        while (characterStream.hasNext()
                && characterStream.peek() != BACKSPACE
                && characterStream.peek() != DOLLAR
                && characterStream.peek() != HASH
                && characterStream.peek() != CLOSING_BRACE) {
            // Accumulate the character
            accumulator.accumulate(characterStream.next());
        }

        // If we have more characters and the next character is a closing brace
        if (characterStream.hasNext() && characterStream.peek() == CLOSING_BRACE) {
            // Accumulate the character
            accumulator.accumulate(characterStream.next());

            // If we have more characters and the next character is a closing brace
            if (characterStream.hasNext() && characterStream.peek() == CLOSING_BRACE) {
                // Accumulate the character
                accumulator.accumulate(characterStream.next());

                // Drain the accumulator
                String text = accumulator.drain();

                // Get the text value removing the hash and braces
                String value = text.substring(3, text.length() - 2).trim();

                // If the value is not empty
                if (!value.isEmpty()) {
                    // Return a HASH_PROPERTY token
                    return new LexerToken(
                            LexerToken.Type.HASH_PROPERTY, characterStream.getPosition() - text.length(), text);
                } else {
                    // Return a TEXT token (special case for #{{})
                    return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
                }
            } else {
                // Drain the accumulator
                String text = accumulator.drain();

                // Return a TEXT token
                return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
            }
        }

        // Drain the accumulator
        String text = accumulator.drain();

        // Return a TEXT token
        return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
    }
    */

    /**
     * Method to parse text sequence
     *
     * @return a text token
     */
    private LexerToken parseTextSequence() {
        // While we have characters and we haven't reached a special character
        while (characterStream.hasNext()
                && characterStream.peek() != BACKSPACE
                && characterStream.peek() != DOLLAR
                && characterStream.peek() != HASH) {
            // Accumulate the character
            accumulator.accumulate(characterStream.next());
        }

        // Drain the accumulator
        String text = accumulator.drain();

        // Return a TEXT token
        return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
    }

    /**
     * Method to parse an opening brace sequence
     *
     * @return a token
     */
    private LexerToken parseOpeningBrace() {
        // If we have more characters and the next character is an opening brace
        if (characterStream.hasNext() && characterStream.peek() == LEFT_BRACE) {
            // Accumulate the character
            accumulator.accumulate(characterStream.next());

            // If we have more characters and the next character is an opening brace
            if (characterStream.hasNext() && characterStream.peek() == LEFT_BRACE) {
                // Parse a variable sequence
                return parseVariable();
            } else {
                // Parse an environment variable with braces sequence
                return parseEnvironmentVariableWithBraces();
            }
        }

        // Parse a text sequence
        return parseTextSequence();
    }

    /**
     * Method to parse a variable sequence
     *
     * @return a token
     */
    private LexerToken parseVariable() {
        // While we have characters and we haven't reached a special character
        while (characterStream.hasNext()
                && characterStream.peek() != BACKSPACE
                && characterStream.peek() != RIGHT_BRACE) {
            // Accumulate the character
            accumulator.accumulate(characterStream.next());
        }

        // If we have more characters and the next character is a closing brace
        if (characterStream.hasNext() && characterStream.peek() == RIGHT_BRACE) {
            // Accumulate the character
            accumulator.accumulate(characterStream.next());

            // If we have more characters and the next character is a closing brace
            if (characterStream.hasNext() && characterStream.peek() == RIGHT_BRACE) {
                // Accumulate the character
                accumulator.accumulate(characterStream.next());

                // Drain the accumulator
                String text = accumulator.drain();

                // Get the text value
                String value = text.substring(3, text.length() - 2).trim();

                // If the value is not empty
                if (!value.isEmpty()) {
                    // Return a VARIABLE token
                    return new LexerToken(
                            LexerToken.Type.VARIABLE, characterStream.getPosition() - text.length(), text);
                } else {
                    // Return a TEXT token (special case for ${{}}
                    return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
                }
            } else {
                // Drain the accumulator
                String text = accumulator.drain();

                // Return a TEXT token
                return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
            }
        }

        // Drain the accumulator
        String text = accumulator.drain();

        // Return a TEXT token
        return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
    }

    /**
     * Method to parse an environment variable sequence
     *
     * @return a token
     */
    private LexerToken parseEnvironmentVariable() {
        // While we have characters and the next character is an underscore or a letter or digit
        while (characterStream.hasNext()
                && (characterStream.peek() == UNDERSCORE || Character.isLetterOrDigit(characterStream.peek()))) {
            // Accumulate the character
            accumulator.accumulate(characterStream.next());
        }

        // Drain the accumulator
        String text = accumulator.drain();

        // Return an ENVIRONMENT_VARIABLE token
        return new LexerToken(
                LexerToken.Type.ENVIRONMENT_VARIABLE, characterStream.getPosition() - text.length(), text);
    }

    /**
     * Method to parse an environment variable with braces sequence
     *
     * @return a token
     */
    private LexerToken parseEnvironmentVariableWithBraces() {
        // While we have characters and the next character is an underscore or a letter or digit
        while (characterStream.hasNext()
                && (characterStream.peek() == UNDERSCORE || Character.isLetterOrDigit(characterStream.peek()))) {
            // Accumulate the character
            accumulator.accumulate(characterStream.next());
        }

        // If we have more characters and the next character is a closing brace
        if (characterStream.hasNext() && characterStream.peek() == RIGHT_BRACE) {
            // Accumulate the character
            accumulator.accumulate(characterStream.next());

            // Drain the accumulator
            String text = accumulator.drain();

            // Return an ENVIRONMENT_VARIABLE token
            return new LexerToken(
                    LexerToken.Type.ENVIRONMENT_VARIABLE, characterStream.getPosition() - text.length(), text);
        }

        // Drain the accumulator
        String text = accumulator.drain();

        // Return a TEXT token
        return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
    }
}
