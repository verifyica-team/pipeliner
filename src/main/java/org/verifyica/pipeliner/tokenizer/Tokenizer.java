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
import org.verifyica.pipeliner.common.PeekIterator;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerLexer;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerLexerError;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerLexerErrorListener;

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

        List<String> substrings = Splitter.split(string);
        for (String substring : substrings) {
            if (substring.startsWith("'")) {
                tokens.add(new Token(Token.Type.TEXT, substring, substring));
            } else {
                tokens.addAll(phase2tokenize(substring));
            }
        }

        return mergeTokens(tokens);
    }

    /**
     * Phase 2 method to tokenize a string
     *
     * @param string string
     * @return a list of Tokens
     * @throws TokenizerException TokenizerException
     */
    private static List<Token> phase2tokenize(String string) throws TokenizerException {
        List<Token> tokens = new ArrayList<>();

        if (string == null) {
            return tokens;
        }

        // Creating the lexer
        TokenizerLexer tokenizerLexer = new TokenizerLexer(CharStreams.fromString(string));

        // Create a common token stream
        CommonTokenStream commonTokenStream = new CommonTokenStream(tokenizerLexer);

        // Create an error listener
        TokenizerLexerErrorListener tokenizerLexerErrorListener = new TokenizerLexerErrorListener();

        // Remove the default error listeners
        tokenizerLexer.removeErrorListeners();

        // Add the custom error listener
        tokenizerLexer.addErrorListener(tokenizerLexerErrorListener);

        // Fill the common token stream
        commonTokenStream.fill();

        // Check for errors
        List<TokenizerLexerError> tokenizerLexerErrors = tokenizerLexerErrorListener.getErrors();
        if (!tokenizerLexerErrors.isEmpty()) {
            TokenizerLexerError tokenizerLexerError = tokenizerLexerErrors.get(0);
            int position = tokenizerLexerError.getPosition();

            // Throw a TokenizerException with error details
            throw new TokenizerException(format("syntax error in string [%s] at position [%d]", string, position));
        }

        // Convert the Antlr tokens to Tokenizer tokens
        PeekIterator<org.antlr.v4.runtime.Token> peekTokenIterator = new PeekIterator<>(commonTokenStream.getTokens());
        while (peekTokenIterator.hasNext()) {
            org.antlr.v4.runtime.Token antlrToken = peekTokenIterator.next();

            // If the token type is -1, then it is EOF, so break
            if (antlrToken.getType() == -1) {
                break;
            }

            // Decode the text since it may have been encoded
            String text = antlrToken.getText();
            String value = text;

            switch (antlrToken.getType()) {
                case TokenizerLexer.PROPERTY: {
                    // Get the value of the property
                    value = value.substring(3, value.length() - 2).trim();
                    tokens.add(new Token(Token.Type.PROPERTY, text, value));
                    break;
                }
                case TokenizerLexer.ENVIRONMENT_VARIABLE_WITH_BRACES: {
                    // Get the value of the environment variable
                    value = value.substring(2, value.length() - 1);
                    tokens.add(new Token(Token.Type.ENVIRONMENT_VARIABLE, text, value));
                    break;
                }
                case TokenizerLexer.ENVIRONMENT_VARIABLE: {
                    // Get the value of the environment variable
                    value = value.substring(1);
                    tokens.add(new Token(Token.Type.ENVIRONMENT_VARIABLE, text, value));
                    break;
                }
                case TokenizerLexer.BACKSLASH: {
                    if (peekTokenIterator.hasNext()) {
                        // Peek at the next token
                        org.antlr.v4.runtime.Token nextAntlrToken = peekTokenIterator.peek();

                        // If the next token is a PROPERTY, ENVIRONMENT_VARIABLE, or ENVIRONMENT_VARIABLE_WITH_BRACES
                        if (nextAntlrToken.getType() == TokenizerLexer.PROPERTY
                                || nextAntlrToken.getType() == TokenizerLexer.ENVIRONMENT_VARIABLE
                                || nextAntlrToken.getType() == TokenizerLexer.ENVIRONMENT_VARIABLE_WITH_BRACES) {

                            // Add the current token as TEXT
                            tokens.add(new Token(Token.Type.TEXT, text, value));

                            // Get the next token
                            nextAntlrToken = peekTokenIterator.next();

                            // Add the next token as TEXT
                            tokens.add(new Token(Token.Type.TEXT, nextAntlrToken.getText(), nextAntlrToken.getText()));
                        }
                    } else {
                        // No next token, add current token as TEXT
                        tokens.add(new Token(Token.Type.TEXT, text, text));
                    }
                }
                case TokenizerLexer.ESCAPED_DOLLAR: {
                    if (peekTokenIterator.hasNext()) {
                        // Peek at the next token
                        org.antlr.v4.runtime.Token nextAntlrToken = peekTokenIterator.peek();

                        // If the next token is a PROPERTY, ENVIRONMENT_VARIABLE, or ENVIRONMENT_VARIABLE_WITH_BRACES
                        if (nextAntlrToken.getType() == TokenizerLexer.PROPERTY
                                || nextAntlrToken.getType() == TokenizerLexer.ENVIRONMENT_VARIABLE
                                || nextAntlrToken.getType() == TokenizerLexer.ENVIRONMENT_VARIABLE_WITH_BRACES) {

                            // Add the current token as TEXT
                            tokens.add(new Token(Token.Type.TEXT, text, value));

                            // Get the next token
                            nextAntlrToken = peekTokenIterator.next();

                            // Add the next token as TEXT
                            tokens.add(new Token(Token.Type.TEXT, nextAntlrToken.getText(), nextAntlrToken.getText()));
                        }
                    } else {
                        // No next token, add current token as TEXT
                        tokens.add(new Token(Token.Type.TEXT, text, text));
                    }
                }
                default: {
                    // Use the text and value as is
                    tokens.add(new Token(Token.Type.TEXT, text, value));
                    break;
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

    private static List<Token> mergeTokens(List<Token> tokens) {
        List<Token> mergedTokens = new ArrayList<>();
        StringBuilder mergedText = new StringBuilder();

        // Iterate over the tokens
        for (Token token : tokens) {
            if (token.getType() == Token.Type.PROPERTY || token.getType() == Token.Type.ENVIRONMENT_VARIABLE) {
                // If the token is PROPERTY or ENVIRONMENT_VARIABLE, flush the mergedText if it's not empty
                if (mergedText.length() > 0) {
                    mergedTokens.add(new Token(Token.Type.TEXT, mergedText.toString(), mergedText.toString()));
                    mergedText.setLength(0); // Reset the StringBuilder for the next sequence of TEXT tokens
                }
                // Add the PROPERTY or ENVIRONMENT_VARIABLE token as it is
                mergedTokens.add(token);
            } else {
                // If it's TEXT, concatenate it to the mergedText StringBuilder
                mergedText.append(token.getText());
            }
        }

        // If there are any remaining TEXT tokens at the end, add them to the result
        if (mergedText.length() > 0) {
            mergedTokens.add(new Token(Token.Type.TEXT, mergedText.toString(), mergedText.toString()));
        }

        return mergedTokens;
    }
}
