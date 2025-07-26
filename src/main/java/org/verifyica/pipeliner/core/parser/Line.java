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

package org.verifyica.pipeliner.core.parser;

import java.util.List;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * Represents a line of tokens in the source code.
 */
public final class Line {

    private final int lineNumber;

    /**
     * The list of tokens in this line.
     */
    private final List<Token> tokens;

    /**
     * Constructor
     *
     * @param tokens the list of tokens in this line
     */
    public Line(int lineNumber, List<Token> tokens) {
        this.lineNumber = lineNumber;
        this.tokens = tokens;
    }

    /**
     * Returns the line number of this line.
     *
     * @return the line number of this line
     */
    public int lineNumber() {
        return lineNumber;
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
     * Returns the token at the specified index in the line.
     *
     * @param index the index of the token to retrieve
     * @return the token at the specified index
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    public Token get(int index) {
        if (index < 0 || index >= tokens.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return tokens.get(index);
    }

    /**
     * Returns the current token in the line without consuming it.
     *
     * @return the current index
     */
    public Token peek() {
        return peek(0);
    }

    /**
     * Returns the token in the line at the specified offset from the current index without consuming it.
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
     * Consumes and returns the next token in the line.
     *
     * @return the next token
     */
    public Token consume() {
        if (tokens.isEmpty()) {
            throw new SyntaxException(this, "No tokens left to consume");
        }
        return tokens.remove(0);
    }

    /**
     * Checks if there are no more tokens left in the line.
     *
     * @return true if the line is empty, false otherwise
     */
    public boolean isEmpty() {
        return tokens.isEmpty();
    }

    /**
     * Returns a list of tokens in the line.
     *
     * @return a list of tokens
     */
    public List<Token> tokens() {
        return tokens;
    }

    /**
     * Return a line as a string by concatenating the lexemes of all tokens.
     *
     * @return a string representation of the line
     */
    public String asString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Token token : tokens) {
            stringBuilder.append(token.lexeme);
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return "Line{lineNumber=" + lineNumber + ", tokens=" + tokens + '}';
    }
}
