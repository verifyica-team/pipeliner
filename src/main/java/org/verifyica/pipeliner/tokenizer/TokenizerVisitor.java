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
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerBaseVisitor;
import org.verifyica.pipeliner.tokenizer.lexer.TokenizerParser;

/** Class to implement TokenizerVisitor */
public class TokenizerVisitor extends TokenizerBaseVisitor<Void> {

    // Local debugging flag, probably should be using a logger
    private static final boolean DEVELOPER_DEBUG = false;

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
        if (DEVELOPER_DEBUG) {
            System.out.printf("DEVELOPER_DEBUG visitBackslash [%s]%n", ctx.getText());
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
        if (DEVELOPER_DEBUG) {
            System.out.printf("DEVELOPER_DEBUG visitVariable [%s]%n", ctx.getText());
        }

        String text = ctx.getText();

        if (text.startsWith("${{")) {
            processAccumulatedText();
            tokens.add(new Token(
                    Token.Type.PROPERTY,
                    text,
                    text.substring(3, text.length() - 2).trim()));
        } else if (text.startsWith("${")) {
            processAccumulatedText();
            tokens.add(new Token(Token.Type.ENVIRONMENT_VARIABLE, text, text.substring(2, text.length() - 1)));
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
        if (DEVELOPER_DEBUG) {
            System.out.printf("DEVELOPER_DEBUG visitBackslashDoubleQuote [%s]%n", ctx.getText());
        }

        stringBuilder.append(ctx.getText());

        return null;
    }

    @Override
    public Void visitQuote(TokenizerParser.QuoteContext ctx) {
        if (DEVELOPER_DEBUG) {
            System.out.printf("DEVELOPER_DEBUG visitQuote [%s]%n", ctx.getText());
        }

        inQuote = !inQuote;
        stringBuilder.append(ctx.getText());

        return null;
    }

    @Override
    public Void visitDollar(TokenizerParser.DollarContext ctx) {
        if (DEVELOPER_DEBUG) {
            System.out.printf("DEVELOPER_DEBUG visitDollar [%s]%n", ctx.getText());
        }

        stringBuilder.append(ctx.getText());

        return null;
    }

    @Override
    public Void visitLeftParenthesis(TokenizerParser.LeftParenthesisContext ctx) {
        if (DEVELOPER_DEBUG) {
            System.out.printf("DEVELOPER_DEBUG visitLeftParenthesis [%s]%n", ctx.getText());
        }

        stringBuilder.append(ctx.getText());

        return null;
    }

    @Override
    public Void visitRightParenthesis(TokenizerParser.RightParenthesisContext ctx) {
        if (DEVELOPER_DEBUG) {
            System.out.printf("DEVELOPER_DEBUG visitRightParenthesis [%s]%n", ctx.getText());
        }

        stringBuilder.append(ctx.getText());

        return null;
    }

    @Override
    public Void visitText(TokenizerParser.TextContext ctx) {
        if (DEVELOPER_DEBUG) {
            System.out.printf("DEVELOPER_DEBUG visitText [%s]%n", ctx.getText());
        }

        stringBuilder.append(ctx.getText());

        return null;
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        if (DEVELOPER_DEBUG) {
            System.out.printf(
                    "DEVELOPER_DEBUG visitTerminal [%s] [%s] [%s]%n",
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
        if (DEVELOPER_DEBUG) {
            System.out.printf("DEVELOPER_DEBUG processAccumulatedText [%s]%n", stringBuilder);
        }

        if (stringBuilder.length() > 0) {
            tokens.add(new Token(Token.Type.TEXT, stringBuilder.toString(), stringBuilder.toString()));
            stringBuilder.setLength(0);
        }
    }

    /**
     * Method to split on first space into a string array
     *
     * @param string string
     * @return a string array
     */
    private static String[] splitOnFirstSpace(String string) {
        // Find the index of the first space
        int index = string.indexOf(' ');

        if (index == -1) {
            // No space was found, return the entire input as the first token
            return new String[] {string};
        }

        // Split the input into two tokens (before the space and after the space)
        String firstToken = string.substring(0, index);
        String secondToken = string.substring(index + 1);

        // Check if the second token is empty
        if (!secondToken.isEmpty()) {
            // The second token is not empty, return first token
            return new String[] {firstToken};
        }

        // The second token is not empty, return both tokens
        return new String[] {firstToken, secondToken};
    }
}
