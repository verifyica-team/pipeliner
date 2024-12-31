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

/** Class to implement EncoderDecoder */
public class EncoderDecoder {

    private static final String ESC_DOLLAR = "__D__";
    private static final String ESC_DOLLAR_CURLY = "__DC__";
    private static final String ESC_DOLLAR_2CURLY = "__D2C__";
    private static final String ESC_DOLLAR_PARENTHESIS = "__DP__";
    private static final String ESC_BACKSLASH_DOUBLE_QUOTE_ = "__BDQ__";
    private static final String ESC_QUOTE_CURLY = "__QC__";

    /** Constructor */
    private EncoderDecoder() {
        // INTENTIONALLY BLANK
    }

    /**
     * Encodes occurrences of \$, \${, \${{ and \$\( to placeholder tokens
     *
     * @param string the string to encode
     * @return the encoded string
     */
    public static String encode(String string) {
        if (string == null) {
            return null;
        }

        return string.replace("$(", ESC_DOLLAR_PARENTHESIS)
                .replace("\\${{", ESC_DOLLAR_2CURLY)
                .replace("\\${", ESC_DOLLAR_CURLY)
                .replace("\\$", ESC_DOLLAR)
                .replace("\\\"", ESC_BACKSLASH_DOUBLE_QUOTE_)
                .replace("'{", ESC_QUOTE_CURLY);
    }

    /**
     * Decodes occurrences of the placeholder tokens back to \$, \${, \${{ and \$\(
     *
     * @param string the string to decode
     * @return the decoded string
     */
    public static String decode(String string) {
        if (string == null) {
            return null;
        }

        return string.replace(ESC_DOLLAR_PARENTHESIS, "$(")
                .replace(ESC_DOLLAR_2CURLY, "\\${{")
                .replace(ESC_DOLLAR_CURLY, "\\${")
                .replace(ESC_DOLLAR, "\\$")
                .replace(ESC_BACKSLASH_DOUBLE_QUOTE_, "\\\"")
                .replace(ESC_QUOTE_CURLY, "'{");
    }
}
