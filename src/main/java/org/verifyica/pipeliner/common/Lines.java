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

public class Lines {

    private static final Pattern PATTERN = Pattern.compile("^(['\"])(\\s*)\\1$");

    private Lines() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to merge a list of lines
     *
     * @param lines lines
     * @return a list of merged lines
     */
    public static List<String> merge(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String str : lines) {
            if (str.endsWith(" \\")) {
                current.append(str.substring(0, str.length() - 2));
            } else {
                if (current.length() > 0) {
                    current.append(" ");
                    current.append(str.trim());
                    result.add(current.toString().trim());
                    current.setLength(0);
                } else {
                    result.add(str);
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
