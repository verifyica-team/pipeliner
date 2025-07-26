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

import java.util.Set;
import java.util.TreeSet;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * A parser that matches a literal from a predefined set of allowed strings.
 */
public class LiteralInSetParser {

    private final Set<String> allowed;

    /**
     * Constructor
     *
     * @param allowed the set of valid literal strings
     */
    private LiteralInSetParser(Set<String> allowed) {
        this.allowed = allowed;
    }

    /**
     * Parses the next token in the line if it matches one of the allowed literals.
     *
     * @param line the line to parse
     * @return the lexeme of the token if it matches, or throws a SyntaxException if it does not
     */
    public String parse(Line line) {
        Token token = line.peek();

        if (token == null) {
            throw new SyntaxException(line, "Expected one of " + allowedValues() + " but found end of line");
        }

        line.consume();

        if (!allowed.contains(token.lexeme)) {
            throw new SyntaxException(
                    line,
                    token.location + ": Expected one of " + allowedValues() + " but found '" + token.lexeme + "'");
        }

        return token.lexeme;
    }

    /**
     * Returns a string representation of the allowed literals.
     *
     * @return a comma-separated list of allowed literals enclosed in single quotes
     */
    private String allowedValues() {
        StringBuilder stringBuilder = new StringBuilder();

        TreeSet<String> treeSet = new TreeSet<>(allowed);
        for (Object literal : treeSet) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("'").append(literal).append("'");
        }

        return stringBuilder.toString();
    }

    /**
     * Creates a new LiteralInSetParser from a set of strings.
     *
     * @param allowed the allowed literals
     * @return a parser instance
     */
    public static LiteralInSetParser of(Set<String> allowed) {
        return new LiteralInSetParser(allowed);
    }
}
