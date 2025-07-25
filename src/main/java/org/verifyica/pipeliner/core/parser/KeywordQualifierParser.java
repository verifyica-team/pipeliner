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

/**
 * Utility class for parsing keyword qualifiers from a statement.
 */
public class KeywordQualifierParser {

    private static final String QUALIFIER_SEPARATOR = "::";

    /**
     * Constructor
     */
    private KeywordQualifierParser() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Parses a sequence of keyword qualifiers from a statement.
     *
     * @param line the statement to parse
     * @return a list of qualifiers found in the statement
     */
    public static List<Token> parse(Line line) {
        List<Token> qualifiers = new ArrayList<>();

        while (true) {
            Token separator = line.peek();
            Token qualifier = line.peek(1);

            if (separator == null || qualifier == null) {
                break;
            }

            if (separator.type != Token.Type.LITERAL || !separator.lexeme.equals(QUALIFIER_SEPARATOR)) {
                break;
            }

            if (qualifier.type != Token.Type.LITERAL) {
                break;
            }

            line.consume();
            qualifiers.add(line.consume());
        }

        return qualifiers;
    }
}
