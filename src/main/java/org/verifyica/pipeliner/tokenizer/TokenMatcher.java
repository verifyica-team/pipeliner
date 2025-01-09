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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class to implement TokenMatcher */
public class TokenMatcher {

    private static final List<Token> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>());

    // Cache for compiled patterns
    private static final Map<String, Pattern> patternCache = new HashMap<>();

    /** Constructor */
    private TokenMatcher() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to find matches in a string based on a regular expression
     *
     * @param input the input string
     * @param regex the regular expression to match
     * @param type the type of token the match represents
     * @return a list of tokens
     */
    public static List<Token> findMatches(String input, String regex, Token.Type type) {
        // If the input string is null or empty, return an empty list (short circuit)
        if (input == null || input.isEmpty()) {
            return EMPTY_LIST;
        }

        List<Token> tokens = new ArrayList<>();

        // Check if the regex has already been compiled and cached
        Pattern pattern = patternCache.get(regex);

        // If not cached, compile the pattern and store it in the cache
        if (pattern == null) {
            pattern = Pattern.compile(regex);
            patternCache.put(regex, pattern);
        }

        // Create a matcher for the input string
        Matcher matcher = pattern.matcher(input);

        // Find all matches in the input string
        while (matcher.find()) {
            // Get the position of the match
            int position = matcher.start();

            // Get the matched substring
            String substring = matcher.group();

            // Add the match as a token to the list
            tokens.add(new Token(type, substring, position));
        }

        return tokens;
    }
}
