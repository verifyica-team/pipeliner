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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class to implement MultiLineMerger */
public class MultiLineMerger {

    private static final Pattern PATTERN = Pattern.compile("^(['\"])(\\s*)\\1$");

    /** Constructor */
    private MultiLineMerger() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to merge a list of lines which may be multi-line
     *
     * @param lines lines
     * @return a list of merged lines
     */
    public static List<String> merge(List<String> lines) {
        List<String> result = new ArrayList<>();

        if (lines == null || lines.isEmpty()) {
            return result;
        }

        StringBuilder current = new StringBuilder();

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
                    // current.append(" ");
                    current.append(line);
                    result.add(current.toString().trim());

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
