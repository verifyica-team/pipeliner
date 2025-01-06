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

        // \${{ encoding
        ENCODING_MAP.put("\\${{", "_0_");

        // \${ encoding
        ENCODING_MAP.put("\\${", "_1_");

        // \$ encoding
        ENCODING_MAP.put("\\$", "_2_");

        // \" encoding
        ENCODING_MAP.put("\\\"", "_3_");

        // _ encoding
        ENCODING_MAP.put("_", "_4_");

        // Define decoding mappings (reverse of encoding)

        // _0_ decoding
        DECODING_MAP.put("_0_", "\\${{");

        // _1_ decoding
        DECODING_MAP.put("_1_", "\\${");

        // _2_ decoding
        DECODING_MAP.put("_2_", "\\$");

        // _3_ decoding
        DECODING_MAP.put("_3_", "\\\"");

        // _4_ decoding
        DECODING_MAP.put("_4_", "_");
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
