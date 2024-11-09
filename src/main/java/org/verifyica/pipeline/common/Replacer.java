/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.pipeline.common;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class to implement Replacer */
public class Replacer {

    /** Constructor */
    private Replacer() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to replace variables in a string
     *
     * @param properties properties
     * @param escapeDoubleQuotes escapeDoubleQuotes
     * @param string string
     * @return the string with variables replaced
     */
    public static String replace(Map<String, String> properties, boolean escapeDoubleQuotes, String string) {
        Pattern pattern = Pattern.compile("(?<!\\\\)\\{\\{(.*?)}}");
        String previousResult;

        do {
            previousResult = string;
            Matcher matcher = pattern.matcher(string);
            StringBuilder result = new StringBuilder();

            while (matcher.find()) {
                String variableName = matcher.group(1);
                String replacement = System.getenv(variableName);

                if (replacement == null) {
                    replacement = properties.get(variableName);
                }

                if (replacement == null) {
                    replacement = matcher.group(0);
                }

                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }

            matcher.appendTail(result);
            string = result.toString();

        } while (!string.equals(previousResult));

        return escapeDoubleQuotes ? escapeDoubleQuotes(string) : string;
    }

    /**
     * Method to escape double quotes
     *
     * @param string string
     * @return the string with double quotes escaped
     */
    public static String escapeDoubleQuotes(String string) {
        return string.replace("\"", "\\\"");
    }
}
