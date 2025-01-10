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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class to implement Lines */
public class LineParser {

    private static final List<String> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>());

    private static final Pattern PATTERN = Pattern.compile("^(['\"])(\\s*)\\1$");

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
    public static List<String> parseLines(String input) {
        if (input == null || input.isEmpty()) {
            return EMPTY_LIST;
        }

        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        String[] lines = input.split("\\R");

        for (String line : lines) {
            if (line.endsWith(" \\")) {
                // Line continuation

                // Remove the trailing backslash and any whitespace
                String trimmedLine = line.substring(0, line.length() - 2);

                // Check if the line is a comment
                if (!trimmedLine.trim().startsWith("#")) {
                    // Append the trimmed line to the current line
                    current.append(trimmedLine);
                }
            } else {
                // Line is not a continuation

                // If the current line is not empty
                if (current.length() > 0) {
                    // Append the current line to the result
                    current.append(line);

                    // Add the current line to the result
                    result.add(current.toString());

                    // Reset the current line
                    current.setLength(0);
                } else {
                    result.add(line);
                }
            }
        }

        if (current.length() > 0) {
            result.add(current.toString());
        }

        Matcher matcher = PATTERN.matcher("");

        for (int i = 0; i < result.size(); ) {
            String line = result.get(i);
            if (line.startsWith("#")) {
                result.remove(i);
                continue;
            }

            matcher.reset(line);
            if (matcher.matches()) {
                result.remove(i);
            } else {
                i++;
            }
        }

        return result;
    }
}
