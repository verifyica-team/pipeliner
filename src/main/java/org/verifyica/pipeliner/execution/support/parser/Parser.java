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

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class to implement PropertyParser */
public class Parser {

    private static final String[] DELIMITER_TOKENS = {"${{", "}}", "\\${{"};

    private static final String REGULAR_EXPRESSION =
            String.join("|", Arrays.stream(DELIMITER_TOKENS).map(Pattern::quote).toArray(String[]::new));

    private static final Pattern PATTERN = Pattern.compile(REGULAR_EXPRESSION);

    /** Constructor */
    private Parser() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to parse a string
     *
     * @param string string
     * @return a list of PropertyParserToken
     * @throws ParserException PropertyParserException
     */
    public static List<Token> parse(String string) throws ParserException {
        Matcher matcher = PATTERN.matcher(string);

        List<InternalToken> internalTokens = new ArrayList<>();
        boolean inBegin = false;
        boolean inValue = false;
        int lastEnd = 0;
        String value;
        InternalToken internalToken;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                value = string.substring(lastEnd, matcher.start());

                if (value.equals("\\${{")) {
                    internalToken = new InternalToken(InternalToken.Type.TEXT, value);
                } else if (value.equals("${{")) {
                    inBegin = true;
                    internalToken = new InternalToken(InternalToken.Type.BEGIN, value);
                } else if (value.equals("}}")) {
                    if (inBegin) {
                        if (!inValue) {
                            throw new ParserException(format("invalid string [%s] property is empty", string));
                        }
                        inBegin = false;
                        inValue = false;
                        internalToken = new InternalToken(InternalToken.Type.END, value);
                    } else {
                        internalToken = new InternalToken(InternalToken.Type.TEXT, value);
                    }
                } else {
                    if (inBegin) {
                        if (value.trim().isEmpty()) {
                            throw new ParserException(format("invalid string [%s] property is empty", string));
                        }
                        inValue = true;
                        internalToken = new InternalToken(InternalToken.Type.VALUE, value.trim());
                    } else {
                        internalToken = new InternalToken(InternalToken.Type.TEXT, value);
                    }
                }

                internalTokens.add(internalToken);
            }

            value = matcher.group();

            if (value.equals("\\${{")) {
                internalToken = new InternalToken(InternalToken.Type.TEXT, value);
            } else if (value.equals("${{")) {
                inBegin = true;
                internalToken = new InternalToken(InternalToken.Type.BEGIN, value);
            } else if (value.equals("}}")) {
                if (inBegin) {
                    if (!inValue) {
                        throw new ParserException(format("invalid string [%s] property is empty", string));
                    }
                    inBegin = false;
                    inValue = false;
                    internalToken = new InternalToken(InternalToken.Type.END, value);
                } else {
                    internalToken = new InternalToken(InternalToken.Type.TEXT, value);
                }
            } else {
                if (inBegin) {
                    if (value.trim().isEmpty()) {
                        throw new ParserException(format("invalid string [%s] property is empty", string));
                    }
                    inValue = true;
                    internalToken = new InternalToken(InternalToken.Type.VALUE, value.trim());
                } else {
                    internalToken = new InternalToken(InternalToken.Type.TEXT, value);
                }
            }

            internalTokens.add(internalToken);
            lastEnd = matcher.end();
        }

        if (lastEnd < string.length()) {
            value = string.substring(lastEnd);

            if (value.equals("\\${{")) {
                internalToken = new InternalToken(InternalToken.Type.TEXT, value);
            } else if (value.equals("${{")) {
                inBegin = true;
                internalToken = new InternalToken(InternalToken.Type.BEGIN, value);
            } else if (value.equals("}}")) {
                if (inBegin) {
                    if (!inValue) {
                        throw new ParserException(format("invalid string [%s] property is empty", string));
                    }
                    inBegin = false;
                    inValue = false;
                    internalToken = new InternalToken(InternalToken.Type.END, value);
                } else {
                    internalToken = new InternalToken(InternalToken.Type.TEXT, value);
                }
            } else {
                if (inBegin) {
                    if (value.trim().isEmpty()) {
                        throw new ParserException(format("invalid string [%s] property is empty", string));
                    }
                    inValue = true;
                    internalToken = new InternalToken(InternalToken.Type.VALUE, value.trim());
                } else {
                    internalToken = new InternalToken(InternalToken.Type.TEXT, value);
                }
            }

            internalTokens.add(internalToken);
        }

        if (inBegin) {
            if (inValue) {
                throw new ParserException(format("invalid string [%s] property is not complete", string));
            } else {
                throw new ParserException(format("invalid string [%s] property is not complete", string));
            }
        }

        return mergeAndConvertInternalTokens(internalTokens);
    }

    /**
     * Method to merge tokens and convert to list of PropertyParserTokens
     *
     * @param internalTokens list of ParserToken
     * @return a list of PropertyParserToken
     * @throws ParserException PropertyParserException
     */
    private static List<Token> mergeAndConvertInternalTokens(List<InternalToken> internalTokens)
            throws ParserException {
        List<Token> mergedTokens = new ArrayList<>();
        StringBuilder textAccumulator = new StringBuilder();
        StringBuilder propertyAccumulator = new StringBuilder();
        boolean inProperty = false;

        for (InternalToken internalToken : internalTokens) {
            switch (internalToken.getType()) {
                case TEXT: {
                    if (propertyAccumulator.length() > 0) {
                        mergedTokens.add(new Token(Token.Type.PROPERTY, propertyAccumulator.toString()));
                        propertyAccumulator.setLength(0);
                        inProperty = false;
                    }

                    textAccumulator.append(internalToken.getToken());
                    break;
                }
                case BEGIN: {
                    if (textAccumulator.length() > 0) {
                        mergedTokens.add(new Token(Token.Type.TEXT, textAccumulator.toString()));
                        textAccumulator.setLength(0);
                    }

                    propertyAccumulator.append(internalToken.getToken());
                    inProperty = true;
                    break;
                }
                case VALUE: {
                    if (inProperty) {
                        propertyAccumulator.append(internalToken.getToken());
                    } else {
                        throw new ParserException("VALUE token found without BEGIN");
                    }
                    break;
                }
                case END: {
                    if (inProperty) {
                        propertyAccumulator.append(internalToken.getToken());
                        mergedTokens.add(new Token(Token.Type.PROPERTY, propertyAccumulator.toString()));
                        propertyAccumulator.setLength(0);
                        inProperty = false;
                    } else {
                        throw new ParserException("END token found without BEGIN");
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
            mergedTokens.add(new Token(Token.Type.TEXT, textAccumulator.toString()));
        }

        // If there are any remaining PROPERTY tokens that weren't completed, throw an error
        if (propertyAccumulator.length() > 0) {
            throw new ParserException("PROPERTY sequence not properly closed with END");
        }

        return mergedTokens;
    }

    /** Class to implement InternalToken */
    private static class InternalToken {

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
        private InternalToken(Type type, String token) {
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
            InternalToken internalToken1 = (InternalToken) object;
            return type == internalToken1.type && Objects.equals(token, internalToken1.token);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, token);
        }
    }
}
