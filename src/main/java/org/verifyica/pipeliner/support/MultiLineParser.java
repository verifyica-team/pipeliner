/*
 * Copyright (C) Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to implement a multi-line parser that merges continuation lines
 */
public class MultiLineParser {

    private static final char CR = '\r';

    private static final char LF = '\n';

    private static String EMPTY = "";

    private static final String LINE_CONTINUATION_SUFFIX = " \\";

    private static final int LINE_CONTINUATION_SUFFIX_LENGTH = LINE_CONTINUATION_SUFFIX.length();

    private static final List<String> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>());

    /**
     * Constructor
     */
    private MultiLineParser() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to parse the input string into a list of lines, merging continuation lines
     *
     * @param input the input string
     * @return a list of parsed lines
     */
    public static List<String> parse(String input) {
        if (input == null || input.isEmpty()) {
            return EMPTY_LIST;
        }

        List<String> parsedLines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        List<String> lines = split(input);
        String trimmedLine;

        // For each line in the input
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
     * Method to split the input string into lines
     *
     * @param input the input
     * @return a list of lines
     */
    private static List<String> split(String input) {
        List<String> lines = new ArrayList<>();
        char[] characters = input.toCharArray();
        Accumulator accumulator = new Accumulator();

        for (int i = 0; i < characters.length; i++) {
            // Get the character
            char c = characters[i];

            switch (c) {
                case CR: {
                    // Handle CRLF (\r\n)
                    if (i + 1 < characters.length && characters[i + 1] == LF) {
                        // Skip '\n'
                        i++;
                    }

                    // Drain the accumulator and add the line
                    lines.add(accumulator.drain(EMPTY));

                    break;
                }
                case LF: {
                    // Handle LF (\n)

                    // Drain the accumulator and add the line
                    lines.add(accumulator.drain(EMPTY));

                    break;
                }
                default: {
                    // Accumulate the character
                    accumulator.accumulate(c);

                    break;
                }
            }
        }

        // Check if there is any remaining text in the accumulator
        if (accumulator.isNotEmpty()) {

            // Drain the accumulator and add the line
            lines.add(accumulator.drain());
        }

        return lines;
    }

    /**
     * Method to trim trailing whitespace
     *
     * @param input the input
     * @return the input with trailing whitespace trimmed
     */
    private static String trimTrailingWhitespace(String input) {
        // Get the length of the input
        int end = input.length();

        // While the end is greater than 0 and the character at the end - 1 is whitespace
        while (end > 0 && Character.isWhitespace(input.charAt(end - 1))) {
            // Decrement the end index
            end--;
        }

        // Get the substring from the start to the end index
        return input.substring(0, end);
    }
}
