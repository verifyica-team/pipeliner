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

import java.util.LinkedHashMap;
import java.util.Map;

/** Class to implement EncoderDecoder */
public class EncoderDecoder {

    // Map of escape sequences for encoding
    private static final Map<String, String> ENCODING_MAP = new LinkedHashMap<>();

    // Ma of escape sequences for decoding
    private static final Map<String, String> DECODING_MAP = new LinkedHashMap<>();

    // Initialize encoding and decoding mappings
    static {
        // Define encoding mappings
        ENCODING_MAP.put("\\${{", "_EDPP_"); // For encoding \${{
        ENCODING_MAP.put("\\${", "_EDP_"); // For encoding \${
        ENCODING_MAP.put("\\$", "_ED_"); // For encoding \$
        ENCODING_MAP.put("\\\"", "_EDQ_"); // For encoding \"
        ENCODING_MAP.put("_", "_U_"); // For encoding underscores (_)

        // Define decoding mappings (reverse of encoding)
        DECODING_MAP.put("_EDPP_", "\\${{"); // For decoding _EDPP_ back to \${{
        DECODING_MAP.put("_EDP_", "\\${"); // For decoding _EDP_ back to \${
        DECODING_MAP.put("_ED_", "\\$"); // For decoding _ED_ back to \$
        DECODING_MAP.put("_EDQ_", "\\\""); // For decoding _EDQ_ back to \"
        DECODING_MAP.put("_U_", "_"); // For decoding _U_ back to _
    }

    /**
     * Private constructor to prevent instantiation of the class
     */
    private EncoderDecoder() {
        // INTENTIONALLY BLANK
    }

    /**
     * Encodes the given string by replacing escape sequences with corresponding placeholders.
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
     * Decodes the given string by replacing placeholders back to their original escape sequences.
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
