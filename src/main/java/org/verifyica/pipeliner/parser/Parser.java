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

import java.util.ArrayList;
import java.util.List;

/**
 * Parser class for processing input strings and extracting tokens.
 */
public class Parser {

    private final Lexer lexer;
    private Parser.Token peekedToken;

    /**
     * Constructor
     *
     * @param input the input string to be parsed
     */
    public Parser(String input) {
        this.lexer = new Lexer(input);
    }

    /**
     * Returns the next token from the lexer. You can treat this as a streaming interface.
     *
     * @return the next token or null if the end of input is reached
     */
    public Token next() {
        if (peekedToken != null) {
            Token token = peekedToken;
            peekedToken = null;
            return token;
        }

        return readToken();
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
     * Parses all tokens from the input and returns them as a list.
     * Useful for batch validation or debugging.
     *
     * @return a list of tokens parsed from the input
     */
    public List<Token> parseAll() {
        List<Token> tokens = new ArrayList<>();
        Token token;
        while ((token = next()) != null) {
            tokens.add(token);
        }
        return tokens;
    }

    /**
     * Reads the next token from the lexer.
     *
     * @return the next token or null if the end of input is reached
     */
    private Token readToken() {
        Lexer.Token lexerToken = lexer.next();
        if (lexerToken == null) {
            return null;
        }

        String text = lexerToken.getText();
        Token.Type type;

        switch (lexerToken.getType()) {
            case VARIABLE: {
                if (text.matches("\\$\\{\\{\\s*}}")) {
                    type = Token.Type.TEXT;
                } else {
                    type = Token.Type.VARIABLE;
                }
                break;
            }
            case ENVIRONMENT_VARIABLE: {
                if (text.matches("\\$\\{\\s*}")) {
                    type = Token.Type.TEXT;
                } else {
                    type = Token.Type.ENVIRONMENT_VARIABLE;
                }
                break;
            }
            case TEXT:
            default: {
                type = Token.Type.TEXT;
                break;
            }
        }

        return new Token(type, text, lexerToken.getStart(), lexerToken.getEnd());
    }

    /**
     * Represents a token parsed from the input.
     */
    public static class Token {

        /**
         * Represents the type of the token.
         */
        public enum Type {

            /**
             * An environment variable token, e.g., ${VAR} or $VAR.
             */
            ENVIRONMENT_VARIABLE,

            /**
             * A plain text token.
             */
            TEXT,

            /**
             * A variable token, e.g., ${{VAR}}.
             */
            VARIABLE,
        }

        private final Type type;
        private final String text;
        private final int start;
        private final int end;
        private final String value;

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
         * @param type the type of the token
         * @param text the text of the token
         * @param start the start index of the token in the input string
         * @param end the end index of the token in the input string
         */
        public Token(Type type, String text, int start, int end) {
            this.type = type;
            this.text = text;
            this.start = start;
            this.end = end;

            switch (type) {
                case VARIABLE: {
                    this.value = text.substring(3, text.length() - 2).trim();
                    break;
                }
                case ENVIRONMENT_VARIABLE: {
                    if (text.startsWith("${") && text.endsWith("}")) {
                        this.value = text.substring(2, text.length() - 1);
                    } else {
                        this.value = text.substring(1);
                    }
                    break;
                }
                case TEXT:
                default: {
                    this.value = text;
                    break;
                }
            }
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
         * Returns the value of the token.
         *
         * @return the value of the token, which is the content of the variable or environment variable
         */
        public String getValue() {
            return value;
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
            return "Token { type=[" + type + "] text=[" + text + "] start=[" + start + "] end=[" + end + "] length=["
                    + getLength() + "] value=[" + value + "] }";
        }
    }
}
