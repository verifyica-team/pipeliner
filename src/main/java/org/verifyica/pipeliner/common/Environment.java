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

import java.util.HashMap;
import java.util.Map;

/** Class to implement Environment */
public class Environment {

    private static final Map<String, String> ENVIRONMENT_VARIABLES = new HashMap<>(System.getenv());

    /** Constructor */
    private Environment() {
        // INTENTIONALLY BLANK
    }

    /**
     * Set an environment variable
     *
     * @param name name
     * @param value value
     */
    public static void set(String name, String value) {
        ENVIRONMENT_VARIABLES.put(name, value);
    }

    /**
     * Get environment variables
     *
     * @return the environment variables
     */
    public static Map<String, String> getenv() {
        return ENVIRONMENT_VARIABLES;
    }

    /**
     * Get an environment variable
     *
     * @param name name
     * @return the environment variable
     */
    public static String getenv(String name) {
        return ENVIRONMENT_VARIABLES.get(name);
    }
}
