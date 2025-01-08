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

import java.util.List;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerLexer;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerLexerError;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerLexerErrorListener;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerParser;

/** Class to implement Tokenizer */
public class Tokenizer {

    // Local debugging flag, probably should be using a logger
    private static final boolean DEVELOPER_DEBUG = DeveloperDebug.isEnabled;

    /** Constructor */
    private Tokenizer() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to tokenize a string
     *
     * @param string string
     * @return a list of Tokens
     * @throws TokenizerException If an error occurs during tokenization
     */
    public static List<Token> tokenize(String string) throws TokenizerException {
        if (DEVELOPER_DEBUG) {
            System.out.printf("DEVELOPER_DEBUG string [%s]%n", string);
        }

        // Create a CharStream from the input
        CharStream charStream = CharStreams.fromString(string);

        // Create the lexer
        TokenizerLexer tokenizerLexer = new TokenizerLexer(charStream);

        // Create a token stream from the lexer
        CommonTokenStream commonTokenStream = new CommonTokenStream(tokenizerLexer);

        // Create the parser
        TokenizerParser tokenizerParser = new TokenizerParser(commonTokenStream);

        // Remove the default error listeners
        tokenizerLexer.removeErrorListeners();

        // Create an error listener
        TokenizerLexerErrorListener tokenizerLexerErrorListener = new TokenizerLexerErrorListener();

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

        // Parse the input starting from the `start` rule
        ParseTree parseTree = tokenizerParser.start();

        // Walk the tree using the visitor
        TokenizerVisitor TokenizerVisitor = new TokenizerVisitor();

        TokenizerVisitor.visit(parseTree);

        List<Token> tokens = TokenizerVisitor.getTokens();

        if (DEVELOPER_DEBUG) {
            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                System.out.printf(
                        "DEVELOPER_DEBUG   token[%d] type [%s] text [%s] value [%s]%n",
                        i, token.getType(), token.getText(), token.getValue());
            }
        }

        return tokens;
    }

    /**
     * Method to validate a string by tokenizing it
     *
     * @param string string
     * @throws TokenizerException If an error occurs during tokenization
     */
    public static void validate(String string) throws TokenizerException {
        tokenize(string);
    }
}
