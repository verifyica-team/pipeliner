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

package org.verifyica.pipeliner.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableSupport {

    private VariableSupport() {
        // INTENTIONALLY BLANK
    }

    public static String replace(Map<String, String> variables, String string) {
        String result = replaceConstants(variables, false, string);

        return result;
    }

    /**
     * Method to merge an array of Maps into a single Map
     *
     * @param maps maps
     * @return a merged Map
     */
    @SafeVarargs
    public static Map<String, String> merge(Map<String, String>... maps) {
        Map<String, String> mergedMap = new LinkedHashMap<>();

        for (Map<String, String> map : maps) {
            mergedMap.putAll(map);
        }

        return mergedMap;
    }

    /**
     * Method to replace property variables
     *
     * @param properties properties
     * @param escapeDoubleQuotes escapeDoubleQuotes
     * @param string string
     * @return the string with property variables replaced
     */
    private static String replaceConstants(Map<String, String> properties, boolean escapeDoubleQuotes, String string) {
        Pattern pattern = Pattern.compile("(?<!\\\\)\\$\\{\\{\\s*(.*?)\\s*}}");
        String previousResult;

        do {
            previousResult = string;
            Matcher matcher = pattern.matcher(string);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String variableName = matcher.group(1).trim();
                String replacement = properties.get(variableName);

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
     * Method to replace environment variables
     *
     * @param properties properties
     * @param string string
     * @return the string with environment variables replaced
     */
    private static String replaceEnvironmentVariables(
            Map<String, String> properties, boolean escapeDoubleQuotes, String string) {
        Pattern pattern = Pattern.compile("(?<!\\\\)\\$(\\w+)");
        String previousResult;

        do {
            previousResult = string;
            Matcher matcher = pattern.matcher(string);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String variableName = matcher.group(1).trim();
                String replacement = properties.get(variableName);

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
    private static String escapeDoubleQuotes(String string) {
        return string.replace("\"", "\\\"");
    }
}
