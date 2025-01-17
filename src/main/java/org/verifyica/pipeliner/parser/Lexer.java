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

    private static final char UNDERSCORE = '_';

    private static final char OPENING_BRACE = '{';

    private static final char CLOSING_BRACE = '}';

    private final CharacterStream characterStream;
    private final Accumulator accumulator;

    /**
     * Constructor
     *
     * @param input the input
     */
    public Lexer(String input) {
        this.characterStream = CharacterStream.of(input);
        this.accumulator = new Accumulator();
    }

    /**
     * Method to tokenize the input string
     *
     * @return a list of tokens
     */
    public List<LexerToken> tokenize() {
        List<LexerToken> tokens = new ArrayList<>();

        LexerToken token;
        while ((token = parseToken()) != null) {
            tokens.add(token);
        }

        return tokens;
    }

    /**
     * Method to parse the next token
     *
     * @return the next token
     */
    private LexerToken parseToken() {
        LexerToken lexerToken = null;

        if (characterStream.hasNext()) {
            char character = characterStream.next();
            accumulator.accumulate(character);

            switch (character) {
                case BACKSPACE: {
                    lexerToken = parseBackslash();
                    break;
                }
                case DOLLAR: {
                    lexerToken = parseDollar();
                    break;
                }
                    /*
                    case HASH: {
                        lexerToken = parseHash();
                        break;
                    }
                    */
                default: {
                    lexerToken = parseText();
                    break;
                }
            }
        }

        if (accumulator.isNotEmpty()) {
            throw new IllegalStateException("Accumulator should be empty");
        }

        return lexerToken;
    }

    /**
     * Method to parse a backslash character sequence
     *
     * @return the token
     */
    private LexerToken parseBackslash() {
        String text = accumulator.drain();
        return new LexerToken(LexerToken.Type.BACKSLASH, characterStream.getPosition() - text.length(), text);
    }

    /**
     * Method to parse a dollar character sequence
     *
     * @return the token
     */
    private LexerToken parseDollar() {
        if (characterStream.hasNext() && characterStream.peek() == OPENING_BRACE) {
            return parseOpeningBrace();
        } else if (characterStream.hasNext()
                && (characterStream.peek() == UNDERSCORE || Character.isLetter(characterStream.peek()))) {
            return parseEnvironmentVariable();
        } else {
            return parseText();
        }
    }

    /**
     * Method to parse a hash character sequence
     *
     * @return the token
     */
    private LexerToken parseHash() {
        while (characterStream.hasNext()
                && characterStream.peek() != BACKSPACE
                && characterStream.peek() != DOLLAR
                && characterStream.peek() != HASH
                && characterStream.peek() != CLOSING_BRACE) {
            accumulator.accumulate(characterStream.next());
        }

        if (characterStream.hasNext() && characterStream.peek() == CLOSING_BRACE) {
            accumulator.accumulate(characterStream.next());
            if (characterStream.hasNext() && characterStream.peek() == CLOSING_BRACE) {
                accumulator.accumulate(characterStream.next());
                String text = accumulator.drain();
                String value = text.substring(3, text.length() - 2).trim();
                if (!value.isEmpty()) {
                    return new LexerToken(
                            LexerToken.Type.HASH_PROPERTY, characterStream.getPosition() - text.length(), text);
                } else {
                    return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
                }
            } else {
                String text = accumulator.drain();
                return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
            }
        } else {
            String text = accumulator.drain();
            return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
        }
    }

    /**
     * Method to parse text sequence
     *
     * @return a text token
     */
    private LexerToken parseText() {
        while (characterStream.hasNext()
                && characterStream.peek() != BACKSPACE
                && characterStream.peek() != DOLLAR
                && characterStream.peek() != HASH) {
            accumulator.accumulate(characterStream.next());
        }

        String text = accumulator.drain();
        return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
    }

    /**
     * Method to parse an opening brace sequence
     *
     * @return a token
     */
    private LexerToken parseOpeningBrace() {
        if (characterStream.hasNext() && characterStream.peek() == OPENING_BRACE) {
            accumulator.accumulate(characterStream.next());
            if (characterStream.hasNext() && characterStream.peek() == OPENING_BRACE) {
                return parseProperty();
            } else {
                return parseEnvironmentVariableWithBraces();
            }
        }

        return parseText();
    }

    /**
     * Method to parse a property sequence
     *
     * @return a token
     */
    private LexerToken parseProperty() {
        while (characterStream.hasNext()
                && characterStream.peek() != BACKSPACE
                && characterStream.peek() != CLOSING_BRACE) {
            accumulator.accumulate(characterStream.next());
        }

        if (characterStream.hasNext() && characterStream.peek() == CLOSING_BRACE) {
            accumulator.accumulate(characterStream.next());
            if (characterStream.hasNext() && characterStream.peek() == CLOSING_BRACE) {
                accumulator.accumulate(characterStream.next());
                String text = accumulator.drain();
                String value = text.substring(3, text.length() - 2).trim();
                if (!value.isEmpty()) {
                    return new LexerToken(
                            LexerToken.Type.PROPERTY, characterStream.getPosition() - text.length(), text);
                } else {
                    return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
                }
            } else {
                String text = accumulator.drain();
                return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
            }
        } else {
            String text = accumulator.drain();
            return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
        }
    }

    /**
     * Method to parse an environment variable sequence
     *
     * @return a token
     */
    private LexerToken parseEnvironmentVariable() {
        while (characterStream.hasNext()
                && (characterStream.peek() == UNDERSCORE || Character.isLetterOrDigit(characterStream.peek()))) {
            accumulator.accumulate(characterStream.next());
        }

        String text = accumulator.drain();
        return new LexerToken(
                LexerToken.Type.ENVIRONMENT_VARIABLE, characterStream.getPosition() - text.length(), text);
    }

    /**
     * Method to parse an environment variable 2 sequence
     *
     * @return a token
     */
    private LexerToken parseEnvironmentVariableWithBraces() {
        while (characterStream.hasNext()
                && (characterStream.peek() == UNDERSCORE || Character.isLetterOrDigit(characterStream.peek()))) {
            accumulator.accumulate(characterStream.next());
        }

        if (characterStream.hasNext() && characterStream.peek() == CLOSING_BRACE) {
            accumulator.accumulate(characterStream.next());
            String text = accumulator.drain();
            return new LexerToken(
                    LexerToken.Type.ENVIRONMENT_VARIABLE, characterStream.getPosition() - text.length(), text);
        } else {
            String text = accumulator.drain();
            return new LexerToken(LexerToken.Type.TEXT, characterStream.getPosition() - text.length(), text);
        }
    }
}
