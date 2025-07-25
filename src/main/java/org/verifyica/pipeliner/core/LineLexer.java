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

package org.verifyica.pipeliner.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.verifyica.pipeliner.exception.UncheckedException;

/**
 * Lexer for processing lines of input, tokenizing them into a structured format.
 */
public class LineLexer {

    private static final Set<String> KEYWORDS = Set.of(
            "capture",
            "working-directory",
            "env",
            "var",
            "print",
            "println",
            "if",
            "sleep",
            "exec",
            "shell",
            "halt",
            "#",
            "//",
            "/*",
            "*/",
            "+:=",
            ":=",
            "::",
            "{",
            "}",
            "[",
            "]",
            "\"",
            "'",
            "|");

    private final BufferedReader reader;
    private final TrieSet trieSet;
    private boolean isEOF = false;
    private int lineNumber = 0;
    private Line cachedLine;

    /**
     * Constructor
     *
     * @param reader the reader to read input from
     */
    public LineLexer(Reader reader) {
        this.reader = new BufferedReader(reader);
        this.trieSet = new TrieSet(KEYWORDS);
    }

    /**
     * Returns the current line number (1-based).
     *
     * @return the current line number
     */
    public int lineNumber() {
        return lineNumber;
    }

    /**
     * Returns the next {@link Line} without consuming it,
     * or {@code null} if there is no more input.
     *
     * @return the next line or {@code null} if EOF is reached
     */
    public Line peek() {
        while (cachedLine == null && !isEOF) {
            try {
                String rawLine = reader.readLine();
                if (rawLine == null) {
                    isEOF = true;
                    return null;
                }

                lineNumber++;

                Line line = tokenize(rawLine);
                if (!line.isEmpty() && !line.isComment()) {
                    cachedLine = line;
                }
            } catch (IOException e) {
                throw new UncheckedException(e);
            }
        }
        return cachedLine;
    }

    /**
     * Returns the next {@link Line}, consuming it,
     * or {@code null} if there is no more input.
     *
     * @return the next line or {@code null} if EOF is reached
     */
    public Line next() {
        Line line = peek();
        cachedLine = null;
        return line;
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
                tokens.add(new Token(Type.WHITESPACE, lexeme, new Location(lineNumber, start + 1)));
                continue;
            }

            // int matchLen = trieSet.longestMatch(buf, i, len);
            int matchLen = DFA.longestMatch(buf, i, len);
            if (matchLen > 0) {
                String lexeme = new String(buf, i, matchLen);
                tokens.add(new Token(Type.LITERAL, lexeme, new Location(lineNumber, i + 1)));
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
            tokens.add(new Token(Type.LITERAL, lexeme, new Location(lineNumber, start + 1)));
        }

        // Remove leading whitespace tokens
        while (!tokens.isEmpty() && tokens.get(0).type == Type.WHITESPACE) {
            tokens.remove(0);
        }

        // Remove trailing whitespace tokens
        while (!tokens.isEmpty() && tokens.get(tokens.size() - 1).type == Type.WHITESPACE) {
            tokens.remove(tokens.size() - 1);
        }

        return new Line(tokens);
    }

    /**
     * Internal trie structure for matching fixed lexeme patterns.
     */
    private static class TrieSet {

        private final TrieNode root = new TrieNode();

        /**
         * Constructor
         *
         * @param words the set of words to insert into the trie
         */
        public TrieSet(Set<String> words) {
            for (String word : words) {
                insert(word);
            }
        }

        /**
         * Inserts a word into the trie.
         *
         * @param word the word to insert
         */
        private void insert(String word) {
            TrieNode node = root;
            for (char ch : word.toCharArray()) {
                node = node.children.computeIfAbsent(ch, c -> new TrieNode());
            }
            node.isTerminal = true;
        }

        /**
         * Finds the longest match for a sequence of characters in the trie.
         *
         * @param buf the character buffer to search
         * @param start the starting index in the buffer
         * @param end the ending index in the buffer
         * @return the length of the longest match found
         */
        public int longestMatch(char[] buf, int start, int end) {
            TrieNode node = root;
            int maxLength = 0;

            for (int i = start; i < end; i++) {
                node = node.children.get(buf[i]);
                if (node == null) break;
                if (node.isTerminal) {
                    maxLength = i - start + 1;
                }
            }

            return maxLength;
        }

        /**
         * Represents a node in the trie.
         */
        private static final class TrieNode {

            /**
             * The children of this node, mapping characters to their respective child nodes.
             */
            Map<Character, TrieNode> children;

            /**
             * Indicates whether this node represents a complete word.
             */
            boolean isTerminal = false;

            public TrieNode() {
                children = new HashMap<>();
            }
        }
    }
}
