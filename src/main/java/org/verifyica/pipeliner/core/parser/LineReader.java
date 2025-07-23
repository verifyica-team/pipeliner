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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.verifyica.pipeliner.core.exception.UncheckedException;

/**
 * Line-based reader that tokenizes input into {@link Line} objects.
 * Each line is parsed into a list of {@link Token}, trimming leading and trailing
 * whitespace tokens but preserving internal whitespace and exact token locations.
 *
 * This reader returns {@code null} from {@code peekSequence()} and {@code nextSequence()}
 * when input is exhausted.
 */
public class LineReader {

    private static final Set<String> TOKENS = Set.of(
            "cd",
            "prop",
            "env",
            "var",
            "print",
            "print::info",
            "print::warning",
            "print::error",
            "if::true",
            "if::false",
            "sleep",
            "run",
            "run::bash",
            "run::dash",
            "run::direct",
            "run::fish",
            "run::ksh",
            "run::sh",
            "run::zsh",
            "halt::ok",
            "halt::error",
            "#",
            "//",
            "/*",
            "*/",
            "+:=",
            ":=",
            "{",
            "}",
            "[",
            "]",
            "\"",
            "'");

    private final BufferedReader reader;
    private final TrieSet trieSet;
    private boolean isEOF = false;
    private int lineNumber = 0;
    private Line cachedLine;

    /**
     * Constructor
     *
     * @param input the string input to read
     */
    public LineReader(String input) {
        this(new BufferedReader(new StringReader(input)));
    }

    /**
     * Constructor
     *
     * @param reader the reader to read input from
     */
    public LineReader(Reader reader) {
        this.reader = new BufferedReader(reader);
        this.trieSet = new TrieSet(TOKENS);
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
    public Line peekSequence() {
        while (cachedLine == null && !isEOF) {
            try {
                String rawLine = reader.readLine();
                if (rawLine == null) {
                    isEOF = true;
                    return null;
                }

                lineNumber++;

                Line line = tokenize(rawLine);
                if (!line.isEmpty()) {
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
    public Line nextSequence() {
        Line line = peekSequence();
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
                tokens.add(new Token(Token.Type.WHITESPACE, lexeme, new Location(lineNumber, start + 1)));
                continue;
            }

            int matchLen = trieSet.longestMatch(buf, i, len);
            if (matchLen > 0) {
                String lexeme = new String(buf, i, matchLen);
                tokens.add(new Token(Token.Type.LITERAL, lexeme, new Location(lineNumber, i + 1)));
                i += matchLen;
                continue;
            }

            // Fallback: collect non-whitespace, non-trie segment as LITERAL
            int start = i;
            while (i < len && (buf[i] != ' ' && buf[i] != '\t') && trieSet.longestMatch(buf, i, len) == 0) {
                i++;
            }

            String lexeme = new String(buf, start, i - start);
            tokens.add(new Token(Token.Type.LITERAL, lexeme, new Location(lineNumber, start + 1)));
        }

        // Remove leading whitespace tokens
        while (!tokens.isEmpty() && tokens.get(0).type == Token.Type.WHITESPACE) {
            tokens.remove(0);
        }

        // Remove trailing whitespace tokens
        while (!tokens.isEmpty() && tokens.get(tokens.size() - 1).type == Token.Type.WHITESPACE) {
            tokens.remove(tokens.size() - 1);
        }

        return new Line(tokens);
    }

    /**
     * Internal trie structure for matching fixed lexeme patterns.
     */
    private static class TrieSet {

        private final TrieNode root = new TrieNode();

        public TrieSet(Set<String> words) {
            for (String word : words) {
                insert(word);
            }
        }

        private void insert(String word) {
            TrieNode node = root;
            for (char ch : word.toCharArray()) {
                node = node.children.computeIfAbsent(ch, c -> new TrieNode());
            }
            node.isTerminal = true;
        }

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

        private static final class TrieNode {
            Map<Character, TrieNode> children = new HashMap<>();
            boolean isTerminal = false;
        }
    }
}
