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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Class to implement LineParser */
public class LineParser {

    private static final List<String> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>());

    /** Constructor */
    private LineParser() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to merge a list of lines merging continuation lines
     *
     * @param input the input line
     * @return a list of lines with continuation lines merged
     */
    public static List<String> parse(String input) {
        if (input == null || input.isEmpty()) {
            return EMPTY_LIST;
        }

        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        String[] lines = input.split("\\R");
        String trimmedLine;

        for (String line : lines) {
            if (line.endsWith(" \\")) {
                // Line continuation

                // Remove the trailing space and backslash characters
                trimmedLine = line.substring(0, line.length() - 2);

                // Check if the line is not empty and not a comment
                if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                    // Append the trimmed line to the current line
                    current.append(trimmedLine);
                }
            } else {
                // Line is not a continuation

                // Trim the current line
                trimmedLine = line.trim();

                // Check if the line is not empty and not a comment
                if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                    // Append the current line to the result
                    current.append(line);

                    // Add the current line to the result
                    result.add(current.toString());

                    // Reset the current line
                    current.setLength(0);
                }
            }
        }

        // Add any remaining text to the result
        if (current.length() > 0) {
            // Trim the current line
            trimmedLine = rightTrim(current.toString());

            // Check if the line is not empty and not a comment
            if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                // Add the current line to the result
                result.add(current.toString());
            }
        }

        return result;
    }

    private static String rightTrim(String input) {
        int end = input.length();
        while (end > 0 && Character.isWhitespace(input.charAt(end - 1))) {
            end--;
        }
        return input.substring(0, end);
    }
}
