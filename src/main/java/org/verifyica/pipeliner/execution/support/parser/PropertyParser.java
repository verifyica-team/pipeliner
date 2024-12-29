/*
 * Copyright (C) 2024-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.execution.support.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class to implement PropertyParser */
public class PropertyParser {

    private static final String[] DELIMITER_TOKENS = {"${{", "}}", "\\${{"};

    private static final String REGULAR_EXPRESSION =
            String.join("|", Arrays.stream(DELIMITER_TOKENS).map(Pattern::quote).toArray(String[]::new));

    private static final Pattern PATTERN = Pattern.compile(REGULAR_EXPRESSION);

    /**
     * Method to parse a string
     *
     * @param string string
     * @return a list of PropertyParserToken
     * @throws PropertyParserException PropertyParserException
     */
    public static List<PropertyParserToken> parse(String string) throws PropertyParserException {
        // Split the string into tokens
        Matcher matcher = PATTERN.matcher(string);

        List<Token> tokens = new ArrayList<>();
        boolean inBegin = false;
        int lastEnd = 0;
        String value;
        Token token;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                value = string.substring(lastEnd, matcher.start());

                if (value.equals("\\${{")) {
                    token = new Token(Token.Type.TEXT, value);
                } else if (value.equals("${{")) {
                    inBegin = true;
                    token = new Token(Token.Type.BEGIN, value);
                } else if (value.equals("}}")) {
                    if (inBegin) {
                        inBegin = false;
                        token = new Token(Token.Type.END, value);
                    } else {
                        token = new Token(Token.Type.TEXT, value);
                    }
                } else {
                    if (inBegin) {
                        token = new Token(Token.Type.VALUE, value.trim());
                    } else {
                        token = new Token(Token.Type.TEXT, value);
                    }
                }

                tokens.add(token);
            }

            value = matcher.group();

            if (value.equals("\\${{")) {
                token = new Token(Token.Type.TEXT, value);
            } else if (value.equals("${{")) {
                inBegin = true;
                token = new Token(Token.Type.BEGIN, value);
            } else if (value.equals("}}")) {
                if (inBegin) {
                    inBegin = false;
                    token = new Token(Token.Type.END, value);
                } else {
                    token = new Token(Token.Type.TEXT, value);
                }
            } else {
                if (inBegin) {
                    token = new Token(Token.Type.VALUE, value.trim());
                } else {
                    token = new Token(Token.Type.TEXT, value);
                }
            }

            tokens.add(token);
            lastEnd = matcher.end();
        }

        if (lastEnd < string.length()) {
            value = string.substring(lastEnd);

            if (value.equals("\\${{")) {
                token = new Token(Token.Type.TEXT, value);
            } else if (value.equals("${{")) {
                inBegin = true;
                token = new Token(Token.Type.BEGIN, value);
            } else if (value.equals("}}")) {
                if (inBegin) {
                    inBegin = false;
                    token = new Token(Token.Type.END, value);
                } else {
                    token = new Token(Token.Type.TEXT, value);
                }
            } else {
                if (inBegin) {
                    token = new Token(Token.Type.VALUE, value.trim());
                } else {
                    token = new Token(Token.Type.TEXT, value);
                }
            }

            tokens.add(token);
        }

        return mergeTokensAndConvert(tokens);
    }

    /**
     * Method to merge tokens and convert to list of PropertyParserTokens
     *
     * @param tokens list of ParserToken
     * @return a list of PropertyParserToken
     * @throws PropertyParserException PropertyParserException
     */
    private static List<PropertyParserToken> mergeTokensAndConvert(List<Token> tokens) throws PropertyParserException {
        List<PropertyParserToken> mergedPropertyParserTokens = new ArrayList<>();
        StringBuilder textAccumulator = new StringBuilder();
        StringBuilder propertyAccumulator = new StringBuilder();
        boolean inProperty = false;

        for (Token token : tokens) {
            switch (token.getType()) {
                case TEXT: {
                    if (propertyAccumulator.length() > 0) {
                        mergedPropertyParserTokens.add(new PropertyParserToken(
                                PropertyParserToken.Type.PROPERTY, propertyAccumulator.toString()));
                        propertyAccumulator.setLength(0);
                        inProperty = false;
                    }

                    textAccumulator.append(token.getToken());
                    break;
                }
                case BEGIN: {
                    if (textAccumulator.length() > 0) {
                        mergedPropertyParserTokens.add(
                                new PropertyParserToken(PropertyParserToken.Type.TEXT, textAccumulator.toString()));
                        textAccumulator.setLength(0);
                    }

                    propertyAccumulator.append(token.getToken());
                    inProperty = true;
                    break;
                }
                case VALUE: {
                    if (inProperty) {
                        propertyAccumulator.append(token.getToken());
                    } else {
                        throw new PropertyParserException("VALUE token found without BEGIN");
                    }
                    break;
                }
                case END: {
                    if (inProperty) {
                        propertyAccumulator.append(token.getToken());
                        mergedPropertyParserTokens.add(new PropertyParserToken(
                                PropertyParserToken.Type.PROPERTY, propertyAccumulator.toString()));
                        propertyAccumulator.setLength(0);
                        inProperty = false;
                    } else {
                        throw new PropertyParserException("END token found without BEGIN");
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        }

        // If there's any accumulated TEXT, add it as a single TEXT token
        if (textAccumulator.length() > 0) {
            mergedPropertyParserTokens.add(
                    new PropertyParserToken(PropertyParserToken.Type.TEXT, textAccumulator.toString()));
        }

        // If there are any remaining PROPERTY tokens that weren't completed, throw an error
        if (propertyAccumulator.length() > 0) {
            throw new PropertyParserException("PROPERTY sequence not properly closed with END");
        }

        return mergedPropertyParserTokens;
    }

    /** Class to implement Token */
    private static class Token {

        /** Enum to represent the type of token */
        private enum Type {
            TEXT,
            BEGIN,
            VALUE,
            END
        }

        private final Type type;
        private final String token;

        /**
         * Constructor
         *
         * @param type type
         * @param token token
         * */
        private Token(Type type, String token) {
            this.type = type;
            this.token = token;
        }

        /**
         * Method to get the type of token
         *
         * @return type
         */
        public Type getType() {
            return type;
        }

        /**
         * Method to get the token
         *
         * @return token
         */
        public String getToken() {
            return token;
        }

        @Override
        public String toString() {
            return "Token{" + "type=" + type + ", token='" + token + '\'' + '}';
        }

        @Override
        public boolean equals(Object object) {
            if (object == null || getClass() != object.getClass()) return false;
            Token token1 = (Token) object;
            return type == token1.type && Objects.equals(token, token1.token);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, token);
        }
    }
}
