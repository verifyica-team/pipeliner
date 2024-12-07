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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to implement RecursiveReplacer */
public class RecursiveReplacer {

    /** Constructor */
    private RecursiveReplacer() {
        // INTENTIONALLY BLANK
    }

    /**
     * Class to replace values in a string
     *
     * @param map map
     * @param regularExpression regularException
     * @param string string
     * @return a string with values replaced
     */
    public static String replace(Map<String, String> map, String regularExpression, String string) {
        if (string == null || map == null) {
            return string;
        }

        Pattern pattern = Pattern.compile(regularExpression);
        String current = string;
        String previous;

        do {
            previous = current;
            Matcher matcher = pattern.matcher(current);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String variableName = matcher.group(1).trim();
                String replacement = map.get(variableName);

                if (replacement == null) {
                    replacement = matcher.group(0);
                }

                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }

            matcher.appendTail(result);
            current = result.toString();

        } while (!current.equals(previous));

        return current;
    }
}
