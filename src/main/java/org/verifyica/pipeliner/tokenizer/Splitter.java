/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class to implement Splitter */
public class Splitter {

    /**
     * Regular expression that matches...
     *
     * 1. A single quote (')
     * 2. Anything that is not a single quote (non-quoted string)
     * 3. A string enclosed by single quotes, keeping the quotes as separate tokens
     */
    private static final String REGEX = "(')|([^']+)|('[^']*')";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    /** Constructor */
    private Splitter() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to split a string into tokens, where...
     *
     * Anything between single quotes is considered a single token
     * Anything starting with a single quote and not terminated by a single quote is considered a single token
     *
     * @param string string
     * @return list of tokens
     */
    public static List<String> split(String string) {
        List<String> tokens = new ArrayList<>();

        // Create a matcher for the input string
        Matcher matcher = PATTERN.matcher(string);

        // Iterate through the matches
        while (matcher.find()) {
            // Group 1: Single quote
            if (matcher.group(1) != null) {
                // Add the single quote as a token
                tokens.add(matcher.group(1));
            }
            // Group 2: Non-quoted part (content outside single quotes)
            else if (matcher.group(2) != null) {
                // Add non-quoted content as a token
                tokens.add(matcher.group(2));
            }
            // Group 3: Quoted part (content inside single quotes)
            else if (matcher.group(3) != null) {
                // Add quoted content as a token
                tokens.add(matcher.group(3));
            }
        }

        // Merge tokens
        return mergeTokens(tokens);
    }

    /**
     * Method to merge tokens that are inside single quotes or
     * start with single quote without a closing single quote
     *
     * @param tokens tokens
     * @return list of merged tokens
     */
    private static List<String> mergeTokens(List<String> tokens) {
        List<String> result = new ArrayList<>();
        List<String> insideQuotes = new ArrayList<>();
        boolean insideSingleQuote = false;

        // Iterate through the tokens
        for (String token : tokens) {
            if (token.equals("'")) {
                // If inside quotes, end the quoted section and add it
                if (insideSingleQuote) {
                    // Add the closing single quote
                    insideQuotes.add(token);

                    // Merge the tokens inside single quotes
                    result.add(String.join("", insideQuotes));

                    // Clear the collected quoted tokens
                    insideQuotes.clear();

                    // Reset the flag
                    insideSingleQuote = false;
                } else {
                    // Set the flag to indicate the start of a quoted section
                    insideSingleQuote = true;

                    // Add the starting single quote
                    insideQuotes.add(token);
                }
            } else {
                // Collect tokens inside or outside the quotes
                if (insideSingleQuote) {
                    // Add token inside quotes
                    insideQuotes.add(token);
                } else {
                    // Add token outside quotes
                    result.add(token);
                }
            }
        }

        // If we are still inside a single quote, add the collected quoted tokens
        if (insideSingleQuote) {
            // Add the collected quoted tokens
            result.add(String.join("", insideQuotes));
        }

        return result;
    }
}
