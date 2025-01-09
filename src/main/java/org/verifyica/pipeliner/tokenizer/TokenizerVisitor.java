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

package org.verifyica.pipeliner.tokenizer;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerBaseVisitor;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerParser;

/** Class to implement TokenizerVisitor */
public class TokenizerVisitor extends TokenizerBaseVisitor<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenizerVisitor.class);

    private final List<Token> tokens;
    private final StringBuilder stringBuilder;
    private boolean inQuote;

    /** Constructor */
    public TokenizerVisitor() {
        tokens = new ArrayList<>();
        stringBuilder = new StringBuilder();
    }

    public List<Token> getTokens() {
        return tokens;
    }

    @Override
    public Void visitBackslash(TokenizerParser.BackslashContext ctx) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("visitBackslash [%s]", ctx.getText());
        }

        stringBuilder.append(ctx.getText());

        return null;
    }

    @Override
    public Void visitStart(TokenizerParser.StartContext ctx) {
        return super.visitStart(ctx);
    }

    @Override
    public Void visitLine(TokenizerParser.LineContext ctx) {
        return super.visitLine(ctx);
    }

    @Override
    public Void visitVariable(TokenizerParser.VariableContext ctx) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("visitVariable [%s]", ctx.getText());
        }

        // Get the text
        String text = ctx.getText();

        if (text.startsWith("${{") && text.endsWith("}}")) {
            // The text is a property

            // Process the accumulated text
            processAccumulatedText();

            if (text.equals("${{}}")) {
                // The parser grammar recognizes "${{}}" as a property, but we are treating it as a text
                stringBuilder.append(text);
            } else {
                // Add the token
                tokens.add(new Token(
                        Token.Type.PROPERTY,
                        text,
                        text.substring(3, text.length() - 2).trim()));
            }
        } else if (text.startsWith("${") && text.endsWith("}")) {
            if (inQuote) {
                stringBuilder.append(text);
            } else {
                processAccumulatedText();
                tokens.add(new Token(Token.Type.ENVIRONMENT_VARIABLE, text, text.substring(2, text.length() - 1)));
            }
        } else if (text.startsWith("\\")) {
            stringBuilder.append(text);
        } else if (text.startsWith("\\$")) {
            stringBuilder.append(text);
        } else {
            if (inQuote) {
                stringBuilder.append(text);
            } else {
                processAccumulatedText();

                String[] subTokens = splitOnFirstSpace(text);
                tokens.add(new Token(Token.Type.ENVIRONMENT_VARIABLE, subTokens[0], subTokens[0].substring(1)));

                if (subTokens.length > 1) {
                    stringBuilder.append(subTokens[1]);
                }
            }
        }

        return null;
    }

    @Override
    public Void visitBackslashDoubleQuote(TokenizerParser.BackslashDoubleQuoteContext ctx) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("visitBackslashDoubleQuote [%s]", ctx.getText());
        }

        stringBuilder.append(ctx.getText());

        return null;
    }

    @Override
    public Void visitQuote(TokenizerParser.QuoteContext ctx) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("visitQuote [%s]", ctx.getText());
        }

        inQuote = !inQuote;
        stringBuilder.append(ctx.getText());

        return null;
    }

    @Override
    public Void visitDollar(TokenizerParser.DollarContext ctx) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("visitDollar [%s]", ctx.getText());
        }

        stringBuilder.append(ctx.getText());

        return null;
    }

    @Override
    public Void visitLeftParenthesis(TokenizerParser.LeftParenthesisContext ctx) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("visitLeftParenthesis [%s]", ctx.getText());
        }

        stringBuilder.append(ctx.getText());

        return null;
    }

    @Override
    public Void visitRightParenthesis(TokenizerParser.RightParenthesisContext ctx) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("visitRightParenthesis [%s]", ctx.getText());
        }

        stringBuilder.append(ctx.getText());

        return null;
    }

    @Override
    public Void visitText(TokenizerParser.TextContext ctx) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("visitText [%s]", ctx.getText());
        }

        stringBuilder.append(ctx.getText());

        return null;
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "visitTerminal [%s] [%s] [%s]",
                    node.getSymbol().getType(), node.getSymbol().getText(), node.getText());
        }

        if (node.getSymbol().getType() == -1) {
            processAccumulatedText();
        } else {
            stringBuilder.append(node.getText());
        }

        return null;
    }

    /** Method to process accumulated text */
    private void processAccumulatedText() {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("processAccumulatedText [%s]", stringBuilder);
        }

        if (stringBuilder.length() > 0) {
            tokens.add(new Token(Token.Type.TEXT, stringBuilder.toString(), stringBuilder.toString()));
            stringBuilder.setLength(0);
        }
    }

    /**
     * Method to split on first space into a string array
     *
     * @param input the input string
     * @return a string array
     */
    private static String[] splitOnFirstSpace(String input) {
        // Find the index of the first space
        int index = input.indexOf(' ');

        if (index == -1) {
            // No space was found, return the entire input as the first token
            return new String[] {input};
        }

        // Split the input into two tokens (before the space and the space plus any remaining characters)
        String firstToken = input.substring(0, index);
        String secondToken = input.substring(index);

        // The second token is not empty, return both tokens
        return new String[] {firstToken, secondToken};
    }
}
