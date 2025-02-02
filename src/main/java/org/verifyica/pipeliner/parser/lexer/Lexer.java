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

package org.verifyica.pipeliner.parser.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.verifyica.pipeliner.common.Accumulator;
import org.verifyica.pipeliner.common.io.CharacterStream;

/** Class to implement Lexer */
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
        this.characterStream = new CharacterStream(input);
        this.accumulator = new Accumulator();
    }

    /**
     * Method to tokenize the input string
     *
     * @return a list of tokens
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        Token token;

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
    private Token parse() {
        Token token = null;

        // While we have characters
        if (characterStream.hasNext()) {
            // Get the next character
            char character = characterStream.next();

            // Accumulate the character
            accumulator.accumulate(character);

            switch (character) {
                case BACKSPACE: {
                    // Parse a backslash sequence
                    token = parseBackslashSequence();
                    break;
                }
                case DOLLAR: {
                    // Parse a dollar sequence
                    token = parseDollarSequence();
                    break;
                }
                default: {
                    // Parse a text sequence
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
    private Token parseBackslashSequence() {
        // Drain the accumulator
        String text = accumulator.drain();

        // Return a BACKSLASH token
        return new Token(Token.Type.BACKSLASH, characterStream.getPosition() - text.length(), text);
    }

    /**
     * Method to parse a dollar sequence
     *
     * @return the token
     */
    private Token parseDollarSequence() {
        if (characterStream.hasNext() && characterStream.peek() == LEFT_BRACE) {
            // Parse an opening brace sequence
            return parseOpeningBraceSequence();
        } else if (characterStream.hasNext()
                && (characterStream.peek() == UNDERSCORE || Character.isLetter(characterStream.peek()))) {
            // Parse an environment variable sequence
            return parseVariableCSequence();
        } else {
            // Parse a text sequence
            return parseTextSequence();
        }
    }

    /**
     * Method to parse text sequence
     *
     * @return a text token
     */
    private Token parseTextSequence() {
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
        return new Token(Token.Type.TEXT, characterStream.getPosition() - text.length(), text);
    }

    /**
     * Method to parse an opening brace sequence
     *
     * @return a token
     */
    private Token parseOpeningBraceSequence() {
        // If we have more characters and the next character is an opening brace
        if (characterStream.hasNext() && characterStream.peek() == LEFT_BRACE) {
            // Accumulate the character
            accumulator.accumulate(characterStream.next());

            // If we have more characters and the next character is an opening brace
            if (characterStream.hasNext() && characterStream.peek() == LEFT_BRACE) {
                // Parse a property sequence
                return parseVariableASequence();
            } else {
                // Parse an environment variable with braces sequence
                return parseVariableBSequence();
            }
        }

        // Parse a text sequence
        return parseTextSequence();
    }

    /**
     * Method to parse a variable A sequence
     *
     * @return a token
     */
    private Token parseVariableASequence() {
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
                    // Return a VARIABLE_A token
                    return new Token(Token.Type.VARIABLE, characterStream.getPosition() - text.length(), text);
                } else {
                    // Return a TEXT token (special case for ${{}}
                    return new Token(Token.Type.TEXT, characterStream.getPosition() - text.length(), text);
                }
            } else {
                // Drain the accumulator
                String text = accumulator.drain();

                // Return a TEXT token
                return new Token(Token.Type.TEXT, characterStream.getPosition() - text.length(), text);
            }
        }

        // Drain the accumulator
        String text = accumulator.drain();

        // Return a TEXT token
        return new Token(Token.Type.TEXT, characterStream.getPosition() - text.length(), text);
    }

    /**
     * Method to parse a variable B sequence
     *
     * @return a token
     */
    private Token parseVariableBSequence() {
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

            // Return an VARIABLE_B token
            return new Token(
                    Token.Type.ENVIRONMENT_VARIABLE_BRACES, characterStream.getPosition() - text.length(), text);
        }

        // Drain the accumulator
        String text = accumulator.drain();

        // Return a TEXT token
        return new Token(Token.Type.TEXT, characterStream.getPosition() - text.length(), text);
    }

    /**
     * Method to parse an environment variable sequence
     *
     * @return a token
     */
    private Token parseVariableCSequence() {
        // While we have characters and the next character is an underscore or a letter or digit
        while (characterStream.hasNext()
                && (characterStream.peek() == UNDERSCORE || Character.isLetterOrDigit(characterStream.peek()))) {
            // Accumulate the character
            accumulator.accumulate(characterStream.next());
        }

        // Drain the accumulator
        String text = accumulator.drain();

        // Return an VARIABLE_C token
        return new Token(Token.Type.ENVIRONMENT_VARIABLE, characterStream.getPosition() - text.length(), text);
    }

    /** Class to implement Token */
    public static class Token {

        /**
         * Enum to implement token type
         */
        public enum Type {

            /**
             * Backslash token
             */
            BACKSLASH,

            /**
             * Text token
             */
            TEXT,

            /**
             * Variable token
             *
             * <p>${{ ws* foo ws* }}</p>
             */
            VARIABLE,

            /**
             * Environment variable token with braces
             *
             * <p>${foo}</p>
             */
            ENVIRONMENT_VARIABLE_BRACES,

            /**
             * Environment variable token
             *
             * <p>$foo</p>
             */
            ENVIRONMENT_VARIABLE,
        }

        private final Type type;
        private final int position;
        private final String text;
        private final int length;

        /**
         * Constructor
         *
         * @param type the type
         * @param position the position
         * @param text the text
         */
        public Token(Type type, int position, String text) {
            this.type = type;
            this.position = position;
            this.text = text;
            this.length = text.length();
        }

        /**
         * Method to get the type
         *
         * @return the type
         */
        public Type getType() {
            return type;
        }

        /**
         * Method to get the position
         *
         * @return the position
         */
        public int getPosition() {
            return position;
        }

        /**
         * Method to get the text
         *
         * @return the text
         */
        public String getText() {
            return text;
        }

        /**
         * Method to get the text length
         *
         * @return the text length
         */
        public int getLength() {
            return length;
        }

        @Override
        public String toString() {
            return "Lexer.Token { type=[" + type + "] position=[" + position + "] text=[" + text + "] }";
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Token)) return false;
            Token token = (Token) object;
            return type == token.type && Objects.equals(text, token.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, text);
        }
    }
}
