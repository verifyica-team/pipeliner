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

package org.verifyica.pipeliner.common;

import java.util.ArrayList;
import java.util.List;

/** Class to implement QuotedStringTokenizer */
public class QuotedStringTokenizer {

    private QuotedStringTokenizer() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to tokenize the input string, splitting on whitespace and respecting quotes and escape characters
     *
     * @param input the input
     * @return a list of tokens
     */
    public static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();

        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean escape = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escape) {
                // Append the escaped character
                currentToken.append(c);
                escape = false;
            } else if (c == '\\') {
                // Mark the next character for escaping
                escape = true;
            } else if (c == '\'' && !inDoubleQuotes) {
                // Toggle single-quote mode
                if (inSingleQuotes) {
                    // Closing single quote, don't add it
                    inSingleQuotes = false;
                } else {
                    // Opening single quote, don't add it
                    inSingleQuotes = true;
                }
            } else if (c == '"' && !inSingleQuotes) {
                // Toggle double-quote mode
                if (inDoubleQuotes) {
                    // Closing double quote, don't add it
                    inDoubleQuotes = false;
                } else {
                    // Opening double quote, don't add it
                    inDoubleQuotes = true;
                }
            } else if (Character.isWhitespace(c) && !inSingleQuotes && !inDoubleQuotes) {
                // Split on whitespace when outside quotes
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            } else {
                // Regular character, append to the token
                currentToken.append(c);
            }
        }

        // Add the final token if it exists
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens;
    }
}
