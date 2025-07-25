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

package org.verifyica.pipeliner.util;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to validate an environment variable name.
 */
public class EnvironmentVariableName {

    private static final String REGEX = "^[a-zA-Z_][a-zA-Z0-9_]*$";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private static final Matcher MATCHER = PATTERN.matcher("");

    /**
     * Set of reserved environment variable names that should not be used in the pipeline.
     */
    private static final Set<String> RESERVED_ENVIRONMENT_VARIABLES = Set.of(
            "_", // The running command
            "PWD", // Current directory
            "HOME", // User's home directory
            "USER", // Username of the current user
            "LOGNAME", // Login name
            "SHELL", // User's default shell
            "PATH", // System path
            "TERM", // Terminal type
            "LANG", // Language/locale settings
            "DISPLAY", // X11 display identifier
            "XAUTHORITY", // X11 authentication file
            "LD_LIBRARY_PATH", // Library path
            "TMPDIR", // Temporary directory
            "SSH_CLIENT", // SSH client info (if in SSH session)
            "SSH_TTY", // SSH terminal
            "SSH_CONNECTION", // SSH connection info
            "JAVA_HOME"); // Java home directory
    /*
    Constants.PIPELINER,
    Constants.PIPELINER_VERSION,
    Constants.PIPELINER_HOME,
    Constants.PIPELINER_IPC_IN,
    Constants.PIPELINER_IPC_IN_FILE_PREFIX,
    Constants.PIPELINER_IPC_OUT,
    Constants.PIPELINER_IPC_OUT_FILE_PREFIX,
    Constants.PIPELINER_SHUTDOWN_HOOKS_ENABLED)
    */

    /**
     * Constructor
     */
    private EnvironmentVariableName() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to return if an environment variable name is valid
     *
     * @param name the name
     * @return true if the environment variable name is valid, , false otherwise
     */
    public static boolean isValid(String name) {
        return MATCHER.reset(name).matches();
    }

    /**
     * Method to return if an environment variable name is invalid
     *
     * @param name the
     * @return true if the environment variable name is invalid, , false otherwise
     */
    public static boolean isInvalid(String name) {
        return !isValid(name);
    }

    /**
     * Method to check if an environment variable name is reserved
     *
     * @param name the name of the environment variable
     * @return true if the name is reserved, false otherwise
     */
    public static boolean isReserved(String name) {
        return RESERVED_ENVIRONMENT_VARIABLES.contains(name);
    }
}
