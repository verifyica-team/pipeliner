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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * A parser that consumes lines from a {@link LineLexer} until a delimiter line is found.
 * Returns the raw lines (as strings) excluding the delimiter.
 */
public class DelimitedBlockParser {

    private final String delimiter;

    /**
     * Constructs the parser with a line-delimiting string.
     *
     * @param delimiter the line (exact match) that marks the end of the block
     */
    public DelimitedBlockParser(String delimiter) {
        this.delimiter = Objects.requireNonNull(delimiter);
    }

    /**
     * Parses all lines until a delimiter is encountered.
     * The delimiter line is consumed but not included in the result.
     *
     * @param lineLexer the line lexer providing lines
     * @return list of line strings excluding the delimiter
     */
    public List<String> parse(LineLexer lineLexer) {
        List<String> lines = new ArrayList<>();

        while (true) {
            Line line = lineLexer.peek();
            if (line == null) {
                throw new SyntaxException("Expected delimiter '" + delimiter + "' but reached end of input");
            }

            String raw = line.asString();
            if (raw.equals(delimiter)) {
                lineLexer.consume();
                break;
            }

            lines.add(raw);
            lineLexer.consume();
        }

        return lines;
    }

    /**
     * Factory method to create a new instance of DelimitedBlockParser.
     *
     * @param delimiter the line (exact match) that marks the end of the block
     * @return a new DelimitedBlockParser instance
     */
    public static DelimitedBlockParser of(String delimiter) {
        return new DelimitedBlockParser(delimiter);
    }
}
