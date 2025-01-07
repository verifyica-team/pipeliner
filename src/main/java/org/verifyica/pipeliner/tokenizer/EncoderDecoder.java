/*
 * Copyright (C) 2024-present Pipeliner project authors and contributors
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Class to implement EncoderDecoder */
public class EncoderDecoder {

    /**
     * Encoding prefix
     */
    public static final String ENCODING_PREFIX = "_";

    /**
     * Encoding suffix
     */
    public static final String ENCODING_SUFFIX = "_";

    // Tokens to encode (order is important)
    private static final List<String> TOKENS = new ArrayList<>();

    static {
        TOKENS.add("\\${{");
        TOKENS.add("\\${");
        TOKENS.add("$(");
        TOKENS.add("\\$");

        // Hack to handle encoding of string like...
        //
        // "$$$$$$$$$$ "
        // "$$$$$$$$$ "
        // "$$$$$$$$ "
        //
        // ... etc.
        for (int i = 50; i > 0; i--) {
            StringBuilder token = new StringBuilder();
            for (int j = 0; j < i; j++) {
                token.append("$");
            }
            token.append(" ");
            TOKENS.add(token.toString());
        }

        TOKENS.add("\\\"");
        TOKENS.add("\\");
        TOKENS.add("\r");
        TOKENS.add("\n");
        TOKENS.add(ENCODING_PREFIX);
    }

    // Map of tokens to encode
    private static final Map<String, String> ENCODING_MAP = new LinkedHashMap<>();

    // Map of tokens to decode
    private static final Map<String, String> DECODING_MAP = new LinkedHashMap<>();

    // Initialize encoding and decoding mappings
    static {
        // Build the encoding map
        for (int i = 0; i < TOKENS.size(); i++) {
            String token = TOKENS.get(i);
            ENCODING_MAP.put(token, ENCODING_PREFIX + i + ENCODING_SUFFIX);
        }

        // Build the decoding map (reverse of encoding)
        ENCODING_MAP.forEach((key, value) -> DECODING_MAP.put(value, key));
    }

    /**
     * Constructor
     */
    private EncoderDecoder() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to encode a string
     *
     * @param string the string to be encoded
     * @return the encoded string with placeholders
     */
    public static String encode(String string) {
        if (string == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();

        // Iterate through each character of the input string
        for (int i = 0; i < string.length(); i++) {
            boolean matchFound = false;

            // Check if the substring starting at the current index matches any of the encoding patterns
            for (Map.Entry<String, String> entry : ENCODING_MAP.entrySet()) {
                if (string.startsWith(entry.getKey(), i)) {
                    // Append the corresponding encoded value to the result
                    stringBuilder.append(entry.getValue());

                    // Skip over the characters that were replaced
                    i += entry.getKey().length() - 1;

                    // Set the flag to indicate that a match was found
                    matchFound = true;

                    break;
                }
            }

            // If no encoding match is found, append the current character as it is
            if (!matchFound) {
                stringBuilder.append(string.charAt(i));
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Method to decode a string
     *
     * @param string the string to be decoded
     * @return the decoded string with original escape sequences
     */
    public static String decode(String string) {
        if (string == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();

        // Iterate through each character of the input string
        for (int i = 0; i < string.length(); i++) {
            boolean matchFound = false;

            // Check if the substring starting at the current index matches any of the decoding patterns
            for (Map.Entry<String, String> entry : DECODING_MAP.entrySet()) {
                if (string.startsWith(entry.getKey(), i)) {
                    // Append the corresponding decoded value to the result
                    stringBuilder.append(entry.getValue());

                    // Skip over the characters that were replaced
                    i += entry.getKey().length() - 1;

                    // Set the flag to indicate that a match was found
                    matchFound = true;

                    break;
                }
            }

            // If no decoding match is found, append the current character as it is
            if (!matchFound) {
                stringBuilder.append(string.charAt(i));
            }
        }

        return stringBuilder.toString();
    }
}
