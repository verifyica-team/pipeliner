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

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.verifyica.pipeliner.core.parser.io.LineReader;

/**
 * Lexer for processing lines of input, tokenizing them into a structured format.
 */
public class LineLexer implements AutoCloseable {

    private final LineReader lineReader;
    private boolean isEOF = false;
    private Line cachedLine;

    /**
     * Factory method to create a new {@link LineLexer} instance
     *
     * @param reader the reader to read input from
     * @return a new {@link LineLexer} instance
     */
    public static LineLexer of(Reader reader) {
        return new LineLexer(LineReader.of(reader));
    }

    /**
     * Closes the {@code LineLexer}.
     */
    public void close() {
        lineReader.close();
    }

    /**
     * Peeks at the next {@link Line} without consuming it,
     * or {@code null} if there is no more input.
     *
     * @return the next line without consuming it or {@code null} if EOF is reached
     */
    public Line peek() {
        while (cachedLine == null && !isEOF) {
            String rawLine = lineReader.readLine();
            if (rawLine == null) {
                isEOF = true;
                return null;
            }

            cachedLine = tokenize(rawLine);
        }
        return cachedLine;
    }

    /**
     * Consumes the line {@link Line}, returning it
     * or {@code null} if there is no more input.
     *
     * @return the line or {@code null} if EOF is reached
     */
    public Line consume() {
        Line line = peek();
        cachedLine = null;
        return line;
    }

    /**
     * Constructor
     *
     * @param lineReader the {@code DslLinerReader} to read input from
     */
    private LineLexer(LineReader lineReader) {
        this.lineReader = lineReader;
    }

    /**
     * Tokenizes a line into a {@link Line}, removing leading and trailing
     * whitespace tokens while preserving internal whitespace and locations.
     *
     * @param line the line to tokenize
     * @return a {@link Line} of tokens representing the line
     */
    private Line tokenize(String line) {
        List<Token> tokens = new ArrayList<>();

        char[] buf = line.toCharArray();
        int len = buf.length;
        int i = 0;

        while (i < len) {
            char ch = buf[i];

            if (ch == ' ' || ch == '\t') {
                int start = i;
                while (i < len && (buf[i] == ' ' || buf[i] == '\t')) i++;
                String lexeme = new String(buf, start, i - start);
                tokens.add(
                        new Token(Token.Type.WHITESPACE, lexeme, new Location(lineReader.getLineNumber(), start + 1)));
                continue;
            }

            // int matchLen = trieSet.longestMatch(buf, i, len);
            int matchLen = DFA.longestMatch(buf, i, len);
            if (matchLen > 0) {
                String lexeme = new String(buf, i, matchLen);
                tokens.add(new Token(Token.Type.LITERAL, lexeme, new Location(lineReader.getLineNumber(), i + 1)));
                i += matchLen;
                continue;
            }

            // Fallback: collect non-whitespace, non-trie segment as LITERAL
            int start = i;
            // while (i < len && (buf[i] != ' ' && buf[i] != '\t') && trieSet.longestMatch(buf, i, len) == 0) {
            while (i < len && (buf[i] != ' ' && buf[i] != '\t') && DFA.longestMatch(buf, i, len) == 0) {
                i++;
            }

            String lexeme = new String(buf, start, i - start);
            tokens.add(new Token(Token.Type.LITERAL, lexeme, new Location(lineReader.getLineNumber(), start + 1)));
        }

        // Remove leading whitespace tokens
        while (!tokens.isEmpty() && tokens.get(0).type == Token.Type.WHITESPACE) {
            tokens.remove(0);
        }

        // Remove trailing whitespace tokens
        while (!tokens.isEmpty() && tokens.get(tokens.size() - 1).type == Token.Type.WHITESPACE) {
            tokens.remove(tokens.size() - 1);
        }

        return !tokens.isEmpty() ? new Line(tokens) : null;
    }
}
