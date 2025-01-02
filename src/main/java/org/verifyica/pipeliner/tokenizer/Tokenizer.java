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

package org.verifyica.pipeliner.tokenizer;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.verifyica.pipeliner.tokenizer.lexer.StringLexer;

/** Class to implement Tokenizer */
public class Tokenizer {

    /** Constructor */
    private Tokenizer() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to tokenize a string
     *
     * @param string string
     * @return a list of Tokens
     * @throws TokenizerException TokenizerException
     */
    public static List<Token> tokenize(String string) throws TokenizerException {
        List<Token> tokens = new ArrayList<>();

        if (string == null) {
            return tokens;
        }

        // Encode escape sequences to prevent tokenization
        String escapedString = EncoderDecoder.encode(string);

        // Creating a lexer
        StringLexer stringLexer = new StringLexer(CharStreams.fromString(escapedString));

        // Creating a common token stream
        CommonTokenStream commonTokenStream = new CommonTokenStream(stringLexer);

        // Creating an error listener
        ErrorListener errorListener = new ErrorListener();
        stringLexer.removeErrorListeners();
        stringLexer.addErrorListener(errorListener);

        // Filling the common token stream
        commonTokenStream.fill();

        // Check for errors
        List<String> errors = errorListener.getErrors();
        if (!errors.isEmpty()) {
            throw new TokenizerException(errors.get(0));
        }

        // Convert the Antlr tokens to our tokens
        List<org.antlr.v4.runtime.Token> antlrTokens = commonTokenStream.getTokens();
        for (org.antlr.v4.runtime.Token internalToken : antlrTokens) {
            // If the token type is -1, then it is EOF
            if (internalToken.getType() == -1) {
                break;
            }

            if (decode(internalToken.getType()) == Token.Type.TEXT) {
                // Decode escape sequence encoded strings
                String value = EncoderDecoder.decode(internalToken.getText());
                tokens.add(new Token(Token.Type.TEXT, value));
            } else {
                tokens.add(new Token(decode(internalToken.getType()), internalToken.getText()));
            }
        }

        return tokens;
    }

    /**
     * Method to validate a string
     *
     * @param string string
     * @throws TokenizerException TokenizerException
     */
    public static void validate(String string) throws TokenizerException {
        tokenize(string);
    }

    /**
     * Method to decode the Antlr token type to our enum token type
     *
     * @param type type
     * @return the enum token type enum
     */
    private static Token.Type decode(int type) {
        switch (type) {
            case StringLexer.PROPERTY: {
                return Token.Type.PROPERTY;
            }
            case StringLexer.ENVIRONMENT_VARIABLE_WITH_BRACES:
            case StringLexer.ENVIRONMENT_VARIABLE: {
                return Token.Type.ENVIRONMENT_VARIABLE;
            }
            case StringLexer.TEXT: {
                return Token.Type.TEXT;
            }
            default: {
                throw new IllegalArgumentException(format("unknown token type [%d]", type));
            }
        }
    }

    /** Class to implement ErrorListener */
    private static class ErrorListener extends ConsoleErrorListener {

        private final List<String> errors;

        /** Constructor */
        public ErrorListener() {
            errors = new ArrayList<>();
        }

        /**
         * Method to get the errors
         *
         * @return the errors
         */
        public List<String> getErrors() {
            return errors;
        }

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e) {
            errors.add(format("syntax error at line [%d] position [%d] error [%s]", line, charPositionInLine, msg));
        }
    }
}
