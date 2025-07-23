/*
 * Copyright (C) Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.core.statement;

import java.util.Arrays;
import java.util.List;
import org.verifyica.pipeliner.core.exception.SyntaxException;

/**
 * Represents a line of tokens in the source code.
 */
public final class Line {

    private final List<Token> tokens;
    private int index = 0;

    /**
     * Constructor
     *
     * @param tokens the list of tokens in this line
     */
    public Line(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Returns the location of the next token in the line.
     *
     * @return the location of the next token in the line
     */
    public Location location() {
        return peek().location;
    }

    /**
     * Returns the number of tokens in this line.
     *
     * @return the number of tokens
     */
    public int size() {
        return tokens.size();
    }

    /**
     * Returns the current index in the line.
     *
     * @return the current index
     */
    public Token peek() {
        return peek(0);
    }

    /**
     * Returns the token at the specified offset from the current index.
     *
     * @param offset the offset from the current index
     * @return the token at the specified offset, or null if out of bounds
     */
    public Token peek(int offset) {
        if (offset < 0 || offset >= tokens.size()) {
            return null;
        }
        return tokens.get(offset);
    }

    /**
     * Returns the next token in the line and advances the index.
     *
     * @return the next token, or null if there are no more tokens
     */
    public Token next() {
        if (tokens.isEmpty()) {
            return null;
        }
        return tokens.remove(0);
    }

    /**
     * Checks if there are no more tokens left in the line.
     *
     * @return true if the line is empty, false otherwise
     */
    public boolean isEmpty() {
        return index >= tokens.size();
    }

    /**
     * Returns the remaining tokens in the line starting from the current index.
     *
     * @return a list of remaining tokens
     */
    public List<Token> tokens() {
        return tokens.subList(index, tokens.size());
    }

    /**
     * Returns the entire line as a single string of lexemes.
     *
     * @return the concatenated string of all tokens
     */
    public String asString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Token token : tokens) {
            stringBuilder.append(token.lexeme);
        }
        return stringBuilder.toString();
    }

    /**
     * Expects the next token to be whitespace.
     */
    public void expectWhitespace() {
        expect(Token.Type.WHITESPACE);
    }

    /**
     * Expects the line to be empty, meaning no tokens are left.
     */
    public void expectEmpty() {
        Token token = peek();
        if (token != null) {
            throw new SyntaxException("Unexpected token '" + token.lexeme + "' at " + token.location);
        }
    }

    /**
     * Expects the next token to be of a specific type and returns it.
     *
     * @param type the expected token type
     * @return the expected token
     */
    public Token expect(Token.Type type) {
        Token token = next();
        if (token == null || token.type != type) {
            throw new SyntaxException("Expected " + type + " but got " + (token == null ? "EOF" : token.type));
        }
        return token;
    }

    /**
     * Expects the next token to be of a specific type and lexeme, and returns it.
     *
     * @param type the expected token type
     * @param lexeme the expected lexeme
     * @return the expected token
     */
    public Token expect(Token.Type type, String lexeme) {
        Token token = next();
        if (token == null || token.type != type) {
            throw new SyntaxException("Expected " + type + " but got " + (token == null ? "EOF" : token.type));
        }

        if (!token.lexeme.equals(lexeme)) {
            throw new SyntaxException("Expected '" + lexeme + "' but got '" + token.lexeme + "'");
        }

        return token;
    }

    /**
     * Expects the next token to be of a specific type and one of the specified lexemes.
     *
     * @param type the expected token type
     * @param lexeme the expected lexemes
     * @return the expected token
     */
    public Token expect(Token.Type type, String... lexeme) {
        Token token = next();
        if (token == null || token.type != type) {
            throw new SyntaxException("Expected " + type + " but got " + (token == null ? "EOF" : token.type));
        }

        for (String l : lexeme) {
            if (token.lexeme.equals(l)) {
                return token;
            }
        }

        String expected =
                String.join(", ", Arrays.stream(lexeme).map(s -> "'" + s + "'").toArray(String[]::new));

        throw new SyntaxException("Expected one of " + expected + " but got '" + token.lexeme + "'");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(System.lineSeparator());

        for (Token token : tokens) {
            stringBuilder.append(token.toString());
        }

        return stringBuilder.toString().trim();
    }
}
