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
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerLexer;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerLexerError;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerLexerErrorListener;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerParser;

/** Class to implement LegacyTokenizer */
public class LegacyTokenizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyTokenizer.class);

    /** Constructor */
    private LegacyTokenizer() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to tokenize a string
     *
     * @param input the input string
     * @return a list of Tokens
     * @throws TokenizerException If an error occurs during tokenization
     */
    public static List<Token> tokenize(String input) throws TokenizerException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("tokenizer class [%s]", LegacyTokenizer.class.getName());
            LOGGER.trace("string [%s]", input);
        }

        // Create a CharStream from the input
        CharStream charStream = CharStreams.fromString(input);

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
            throw new TokenizerException(format("syntax error in string [%s] at position [%d]", input, position));
        }

        // Parse the input starting from the `start` rule
        ParseTree parseTree = tokenizerParser.start();

        // Walk the tree using the visitor
        TokenizerVisitor tokenizerVisitor = new TokenizerVisitor();

        tokenizerVisitor.visit(parseTree);

        List<Token> tokens = tokenizerVisitor.getTokens();

        if (LOGGER.isTraceEnabled()) {
            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                LOGGER.trace(
                        "token[%d] type [%s] text [%s] value [%s]",
                        i, token.getType(), token.getText(), token.getValue());
            }
        }

        return tokens;
    }

    /**
     * Method to validate a string by tokenizing it
     *
     * @param input the input string
     * @throws TokenizerException If an error occurs during tokenization
     */
    public static void validate(String input) throws TokenizerException {
        tokenize(input);
    }
}
