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

/**
 * Lexer class for processing input strings and extracting tokens.
 */
public class Lexer {

    private final String input;
    private int position;
    private Token peekedToken = null;

    /**
     * Constructor
     *
     * @param input the input string
     */
    public Lexer(String input) {
        this.input = input;
        this.position = 0;
    }

    /**
     * Returns the next token from the input string or null if the end of the input is reached.
     *
     * @return the next token or null if the end of input is reached
     */
    public Token next() {
        if (peekedToken != null) {
            Token token = peekedToken;
            peekedToken = null;
            return token;
        }

        if (position >= input.length()) {
            return null;
        }

        // Handle escaped dollar \
        if (input.charAt(position) == '\\' && position + 1 < input.length() && input.charAt(position + 1) == '$') {
            int start = position;
            position += 2;
            return new Token(Token.Type.TEXT, input.substring(start, position), start, position);
        }

        char current = input.charAt(position);

        if (current == '$') {
            // Handle ${{ ... }}
            if (match("${{")) {
                int start = position - 3;
                int end = input.indexOf("}}", position);
                if (end == -1) {
                    // Unclosed VARIABLE; consume entire rest of string
                    position = input.length();
                    return new Token(Token.Type.TEXT, input.substring(start), start, input.length());
                }
                end += 2;
                String text = input.substring(start, end);
                position = end;
                return new Token(Token.Type.VARIABLE, text, start, end);
            }

            // Handle ${...}
            if (match("${")) {
                int start = position - 2;
                int end = input.indexOf('}', position);
                if (end == -1) {
                    // Unclosed variable; consume entire rest of string as TEXT
                    position = input.length();
                    return new Token(Token.Type.TEXT, input.substring(start), start, input.length());
                }
                end++;
                String text = input.substring(start, end);
                position = end;
                return new Token(Token.Type.ENVIRONMENT_VARIABLE, text, start, end);
            }

            // Handle $NAME
            int start = position;
            position++; // skip $
            while (position < input.length()
                    && (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
                position++;
            }
            String text = input.substring(start, position);
            return new Token(Token.Type.ENVIRONMENT_VARIABLE, text, start, position);
        }

        // Handle TEXT
        int start = position;
        while (position < input.length()) {
            if (input.charAt(position) == '$'
                    || (input.charAt(position) == '\\'
                            && position + 1 < input.length()
                            && input.charAt(position + 1) == '$')) {
                break;
            }
            position++;
        }
        return new Token(Token.Type.TEXT, input.substring(start, position), start, position);
    }

    /**
     * Peeks at the next token without consuming it.
     *
     * @return the next token or null if the end of input is reached
     */
    public Token peek() {
        if (peekedToken == null) {
            peekedToken = next();
        }
        return peekedToken;
    }

    /**
     * Checks if the input starts with the given prefix at the current position and advances the position if it does.
     *
     * @param prefix the prefix to match
     * @return true if the input starts with the prefix, false otherwise
     */
    private boolean match(String prefix) {
        if (input.startsWith(prefix, position)) {
            position += prefix.length();
            return true;
        }
        return false;
    }

    /**
     * Represents a token in the input string.
     */
    public static class Token {

        /**
         * Represents the type of the token.
         */
        public enum Type {

            /**
             * An environment variable token, e.g. ${VAR} or $VAR.
             */
            ENVIRONMENT_VARIABLE,

            /**
             * A plain text token.
             */
            TEXT,

            /**
             * A variable token, e.g. ${{VAR}}.
             */
            VARIABLE
        }

        private final Type type;
        private final String text;
        private final int start;
        private final int end;

        /**
         * Constructor
         *
         * @param type the type of the token
         * @param text the text of the token
         */
        public Token(Type type, String text) {
            this(type, text, -1, -1);
        }

        /**
         * Constructor
         *
         * @param type  the type of the token
         * @param text  the text of the token
         * @param start the start index of the token in the input string
         * @param end   the end index of the token in the input string
         */
        public Token(Type type, String text, int start, int end) {
            this.type = type;
            this.text = text;
            this.start = start;
            this.end = end;
        }

        /**
         * Returns the type of the token.
         *
         * @return the type of the token
         */
        public Type getType() {
            return type;
        }

        /**
         * Returns the text of the token.
         *
         * @return the text of the token
         */
        public String getText() {
            return text;
        }

        /**
         * Returns the start index of the token in the input string.
         *
         * @return the start index of the token
         */
        public int getStart() {
            return start;
        }

        /**
         * Returns the end index of the token in the input string.
         *
         * @return the end index of the token
         */
        public int getEnd() {
            return end;
        }

        /**
         * Returns the length of the token.
         *
         * @return the length of the token
         */
        public int getLength() {
            return end - start;
        }

        @Override
        public String toString() {
            return "Token { type=[" + type + "] start=[" + start + "] end=[" + end + "] length=[" + getLength()
                    + "] text=[" + text + "] }";
        }
    }
}
