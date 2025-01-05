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
import org.antlr.v4.runtime.Vocabulary;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerLexer;

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
        TokenizerLexer tokenizerLexer = new TokenizerLexer(CharStreams.fromString(escapedString));

        // Get the Vocabulary
        Vocabulary vocabulary = tokenizerLexer.getVocabulary();

        // Create a common token stream
        CommonTokenStream commonTokenStream = new CommonTokenStream(tokenizerLexer);

        // Create an error listener and add it to the lexer
        ErrorListener errorListener = new ErrorListener();
        tokenizerLexer.removeErrorListeners();
        tokenizerLexer.addErrorListener(errorListener);

        // Fill the common token stream
        commonTokenStream.fill();

        // Check for errors
        List<String> errors = errorListener.getErrors();
        if (!errors.isEmpty()) {
            throw new TokenizerException(errors.get(0));
        }

        // Convert the Antlr tokens to Tokenizer tokens
        List<org.antlr.v4.runtime.Token> antlrTokens = commonTokenStream.getTokens();
        for (org.antlr.v4.runtime.Token antlrToken : antlrTokens) {
            // If the token type is -1, then it is EOF so break
            if (antlrToken.getType() == -1) {
                break;
            }

            String text = antlrToken.getText();
            String value = text;

            switch (antlrToken.getType()) {
                case TokenizerLexer.PROPERTY: {
                    value = value.substring(3, value.length() - 2).trim();
                    tokens.add(new Token(Token.Type.PROPERTY, text, value));
                    break;
                }
                case TokenizerLexer.ENVIRONMENT_VARIABLE_WITH_BRACES: {
                    value = value.substring(2, value.length() - 1);
                    tokens.add(new Token(Token.Type.ENVIRONMENT_VARIABLE, text, value));
                    break;
                }
                case TokenizerLexer.ENVIRONMENT_VARIABLE: {
                    value = value.substring(1);
                    tokens.add(new Token(Token.Type.ENVIRONMENT_VARIABLE, text, value));
                    break;
                }
                case TokenizerLexer.TEXT: {
                    value = EncoderDecoder.decode(text);
                    tokens.add(new Token(Token.Type.TEXT, value, value));
                    break;
                }
                default: {
                    throw new IllegalArgumentException(format(
                            "unknown token type [%d] symbol [%s]",
                            antlrToken.getType(), vocabulary.getSymbolicName(antlrToken.getType())));
                }
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
                int characterPositionInLine,
                String message,
                RecognitionException recognitionException) {
            errors.add(format(
                    "syntax error at line [%d] position [%d] error [%s]", line, characterPositionInLine, message));
        }
    }
}
