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

package org.verifyica.pipeliner.parser.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.verifyica.pipeliner.common.Accumulator;
import org.verifyica.pipeliner.common.io.CharacterStream;

/** Class to implement VariableLexer */
public class VariableLexer {

    /**
     * Constant for modifier separator
     */
    public static final char COLON = ':';

    /**
     * Constant for scope separator
     */
    public static final char PERIOD = '.';

    private final CharacterStream characterStream;
    private final Accumulator accumulator;

    /**
     * Constructor
     *
     * @param input the input
     */
    public VariableLexer(String input) {
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
        Token previousToken = null;
        Token token;

        while (true) {
            // Parse the next token
            token = parse();
            if (token == null) {
                break;
            }

            // Add the token
            tokens.add(token);

            // Track the previous token
            previousToken = token;
        }

        // Add a text token if the last token was not a text token
        if (previousToken == null || previousToken.getType() != Token.Type.TEXT) {
            tokens.add(new Token(Token.Type.TEXT, characterStream.getPosition(), ""));
        }

        return tokens;
    }

    /**
     * Method to parse the next token
     *
     * @return the next token
     */
    private Token parse() {
        // While we have characters
        while (characterStream.hasNext()) {
            // Get the next character
            char character = characterStream.peek();

            switch (character) {
                case COLON: {
                    // Consume the character
                    characterStream.next();

                    // Drain the accumulator
                    String string = accumulator.drain("");

                    // Calculate the position
                    int position = characterStream.getPosition() - string.length() - 1;

                    // Return the token
                    return new Token(Token.Type.MODIFIER, position, string);
                }
                case PERIOD: {
                    // Consume the character
                    characterStream.next();

                    // Drain the accumulator
                    String string = accumulator.drain("");

                    // Calculate the position
                    int position = characterStream.getPosition() - 1;

                    // Return the token
                    return new Token(Token.Type.SCOPE, position, string);
                }
                default: {
                    // Consume and accumulate the character
                    accumulator.accumulate(characterStream.next());

                    break;
                }
            }
        }

        if (accumulator.isNotEmpty()) {
            // Drain the accumulator
            String string = accumulator.drain();

            // Calculate the position
            int position = characterStream.getPosition() - string.length();

            // Return the token
            return new Token(Token.Type.TEXT, position, string);
        } else {
            return null;
        }
    }

    /** Class to implement Token */
    public static class Token {

        /**
         * Enum to implement token type
         */
        public enum Type {

            /**
             * Modifier token
             */
            MODIFIER,

            /**
             * Scope token
             */
            SCOPE,

            /**
             * Text token
             */
            TEXT,
        }

        private final Type type;
        private final int position;
        private final String text;
        private final int length;

        /**
         * Constructor
         *
         * @param type the type
         * @param text the text
         */
        public Token(Type type, String text) {
            this(type, -1, text);
        }

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
            return "VariableLexer.Token { type=[" + type + "] position=[" + position + "] text=[" + text + "] }";
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Token)) return false;
            Token lexerToken = (Token) object;
            return type == lexerToken.type && Objects.equals(text, lexerToken.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, text);
        }
    }
}
