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

    /**
     * The list of tokens in this line.
     */
    private final List<Token> tokens;

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
     * Returns the last token in the line without consuming it.
     *
     * @return the last token
     */
    public Token peekEnd() {
        return peekEnd(size() - 1);
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
     * Returns the token at the specified offset from the end of the line without consuming it.
     *
     * @param offset the offset from the end of the line
     * @return the token at the specified offset from the end, or null if out of bounds
     */
    public Token peekEnd(int offset) {
        int index = tokens.size() - 1 - offset;
        if (index < 0 || index >= tokens.size()) {
            return null;
        }
        return tokens.get(index);
    }

    /**
     * Consumes and returns the next token in the line.
     *
     * @return the next token
     */
    public Token consume() {
        if (tokens.isEmpty()) {
            throw new SyntaxException("No tokens left to consume");
        }
        return tokens.remove(0);
    }

    /**
     * Consumes and returns the last token in the line.
     *
     * @return the last token, or null if the line is empty
     */
    public Token consumeEnd() {
        if (tokens.isEmpty()) {
            throw new SyntaxException("No tokens left to consume");
        }
        return tokens.remove(tokens.size() - 1);
    }

    /**
     * Checks if the line represents a comment.
     *
     * @return true if the line is a comment, false otherwise
     */
    public boolean isComment() {
        return !tokens.isEmpty() && ("#".equals(tokens.get(0).lexeme) || "//".equals(tokens.get(0).lexeme));
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
     * Returns a copy of the remaining tokens in the line starting from the current index.
     *
     * @return a list of remaining tokens
     */
    public List<Token> tokens() {
        return tokens.subList(0, tokens.size());
    }

    @Override
    public String toString() {
        return "Line{location=" + location() + ", tokens=" + tokens + '}';
    }
}
