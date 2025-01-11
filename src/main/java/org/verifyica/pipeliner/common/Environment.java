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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** Class to implement Environment */
public class Environment {

    private static final Map<String, String> ENVIRONMENT_VARIABLES;
    private static boolean isLocked = false; // Locking flag

    static {
        ENVIRONMENT_VARIABLES = new TreeMap<>(System.getenv());
    }

    /** Constructor */
    private Environment() {
        // INTENTIONALLY BLANK
    }

    /**
     * Set an environment variable if not locked.
     *
     * @param name the environment variable name
     * @param value the environment variable value
     * @throws IllegalStateException if the environment is locked
     */
    public static void setenv(String name, String value) {
        if (isLocked) {
            throw new IllegalStateException("Environment is locked");
        }

        ENVIRONMENT_VARIABLES.put(name, value);
    }

    /**
     * Get environment variables
     *
     * @return the environment variables
     */
    public static Map<String, String> getenv() {
        return Collections.unmodifiableMap(ENVIRONMENT_VARIABLES);
    }

    /**
     * Get an environment variable
     *
     * @param name the environment variable name
     * @return the environment variable value
     */
    public static String getenv(String name) {
        return ENVIRONMENT_VARIABLES.get(name);
    }

    /**
     * Lock the environment variables to prevent further modifications.
     */
    public static void lock() {
        isLocked = true;
    }

    /**
     * Check if the environment is locked.
     *
     * @return true if locked, false otherwise
     */
    public static boolean isLocked() {
        return isLocked;
    }
}
