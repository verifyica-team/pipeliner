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

import org.verifyica.pipeliner.core.parser.Token.Type;

/**
 * A parser that merges all remaining tokens into a single string,
 * skipping leading and trailing whitespace tokens.
 * Returns {@code null} if there are no non-whitespace tokens.
 */
public class MergeParser {

    private static final MergeParser SINGLETON = new MergeParser();

    /**
     * Constructor
     */
    private MergeParser() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Parses the line, skipping leading and trailing whitespace tokens,
     * and concatenates the lexemes of the remaining tokens.
     *
     * @param line the line of tokens to parse
     * @return a single string containing the merged lexemes,
     *         or {@code null} if there are no non-whitespace tokens
     */
    public String parse(Line line) {
        if (line == null || line.isEmpty()) {
            return null;
        }

        int start = 0;
        int end = line.size() - 1;

        // Skip leading whitespace
        while (start <= end && line.tokens().get(start).type == Type.WHITESPACE) {
            start++;
        }

        // Skip trailing whitespace
        while (end >= start && line.tokens().get(end).type == Type.WHITESPACE) {
            end--;
        }

        if (start > end) {
            // No non-whitespace tokens
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (int i = start; i <= end; i++) {
            result.append(line.tokens().get(i).lexeme);
        }

        // Consume tokens up to and including 'end'
        for (int i = 0; i <= end; i++) {
            line.consume();
        }

        return result.toString();
    }

    /**
     * Factory method to get the singleton instance of MergeParser.
     *
     * @return the singleton instance of MergeParser
     */
    public static MergeParser singleton() {
        return SINGLETON;
    }
}
