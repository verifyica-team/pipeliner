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

package org.verifyica.pipeliner;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** Class to implement Environment */
public class Environment {

    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    private static final Map<String, String> ENVIRONMENT_VARIABLES;

    static {
        ENVIRONMENT_VARIABLES = new TreeMap<>(System.getenv());
        ENVIRONMENT_VARIABLES.put(Constants.PIPELINER_VERSION, Version.getVersion());
        ENVIRONMENT_VARIABLES.put(Constants.PIPELINER_TMP, System.getProperty(JAVA_IO_TMPDIR));
    }

    /**
     * Constructor
     */
    private Environment() {
        // INTENTIONALLY BLANK
    }

    /**
     * Set an environment variable
     *
     * @param name the environment variable name
     * @param value the environment variable value
     * @throws IllegalStateException if the environment is locked
     */
    public static void setenv(String name, String value) {
        ENVIRONMENT_VARIABLES.put(name, value);
    }

    /**
     * Set environment variables
     *
     * @param environmentVariables the environment variables
     */
    public static void setenvs(Map<String, String> environmentVariables) {
        ENVIRONMENT_VARIABLES.putAll(environmentVariables);
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
}
