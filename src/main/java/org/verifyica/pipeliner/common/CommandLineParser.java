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

/** Class to implement CommandLineParser */
public class CommandLineParser {

    private static final String LINE_CONTINUATION_SUFFIX = " \\";

    private static int LINE_CONTINUATION_SUFFIX_LENGTH = LINE_CONTINUATION_SUFFIX.length();

    private static final List<String> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>());

    /**
     * Constructor
     */
    private CommandLineParser() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to merge a list of command lines merging continuation lines
     *
     * @param input the input line
     * @return a list of command lines with continuation lines merged
     */
    public static List<String> parse(String input) {
        if (input == null || input.isEmpty()) {
            return EMPTY_LIST;
        }

        List<String> parsedLines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        String[] lines = input.split("\\R");
        String trimmedLine;

        for (String line : lines) {
            // If line ends in a line continuation suffix
            if (line.endsWith(LINE_CONTINUATION_SUFFIX)) {
                // Remove the line continuation suffix
                trimmedLine = line.substring(0, line.length() - LINE_CONTINUATION_SUFFIX_LENGTH);

                // Check if the line is not empty and not a comment
                if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                    // Append the trimmed line to the current line
                    current.append(trimmedLine);
                }
            } else {
                // The line doesn't end in a line continuation suffix

                // Trim the current line
                trimmedLine = line.trim();

                // Check if the line is not empty and not a comment
                if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                    // Append the current line to the result
                    current.append(line);

                    // Add the current line to the result
                    parsedLines.add(current.toString());

                    // Reset the current line
                    current.setLength(0);
                }
            }
        }

        // Add any remaining text to the result
        if (current.length() > 0) {
            // Trim the current line
            trimmedLine = trimTrailingWhitespace(current.toString());

            // Check if the line is not empty and not a comment
            if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                // Add the current line to the result
                parsedLines.add(current.toString());
            }
        }

        return parsedLines;
    }

    /**
     * Method to trim trailing whitespace
     *
     * @param input the input
     * @return the input with trailing whitespace trimmed
     */
    private static String trimTrailingWhitespace(String input) {
        int end = input.length();
        while (end > 0 && Character.isWhitespace(input.charAt(end - 1))) {
            end--;
        }
        return input.substring(0, end);
    }
}
