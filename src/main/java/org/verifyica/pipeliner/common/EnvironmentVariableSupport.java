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

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Class to implement EnvironmentVariableSupport */
public class EnvironmentVariableSupport {

    private static final String SANITIZE_NAME_REGEX = "[^A-Z0-9_]";

    private static final String SANITIZE_NAME_REPLACEMENT = "_";

    /** Constructor */
    private EnvironmentVariableSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get sanitized System environment variables
     *
     * @return sanitized System environment variables
     */
    public static Map<String, String> getSystemEnvironmentVariables() {
        Map<String, String> environmentVariables = new TreeMap<>();

        System.getenv().forEach((key, value) -> environmentVariables.put(toSanitizedEnvironmentVariable(key), value));

        return environmentVariables;
    }

    /**
     * Method to convert a string to an sanitized environment variable
     *
     * @param string string
     * @return a sanitized environment variable name
     */
    public static String toSanitizedEnvironmentVariable(String string) {
        return string.trim().toUpperCase(Locale.US).replaceAll(SANITIZE_NAME_REGEX, SANITIZE_NAME_REPLACEMENT);
    }
}
