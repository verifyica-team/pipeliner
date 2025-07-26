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

import java.util.function.Consumer;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * Parses a value from a Line of tokens.
 */
public class ValueParser {

    private static final ValueParser SINGLETON = new ValueParser();

    /**
     * Constructor
     */
    private ValueParser() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Parses a value from the provided Line.
     *
     * @param line the Line to parse from
     * @return the parsed value as a String
     */
    public String parse(Line line) {
        Token token = line.peek();
        if (token == null) {
            throw new SyntaxException(line, "Expected value but found end of line");
        }

        return line.consume().lexeme;
    }

    /**
     * Parses a value from the provided Line and applies a consumer to the parsed value.
     *
     * @param line the Line to parse from
     * @param consumer a Consumer that will process the parsed value
     * @return the parsed value as a String
     */
    public String parse(Line line, Consumer<String> consumer) {
        Token token = line.peek();
        if (token == null) {
            throw new SyntaxException(line, "Expected value but found end of line");
        }

        String lexeme = line.consume().lexeme;
        consumer.accept(lexeme);
        return lexeme;
    }

    /**
     * Factory method to get a singleton instance of ValueParser.
     *
     * @return the singleton instance of ValueParser
     */
    public static ValueParser singleton() {
        return SINGLETON;
    }
}
