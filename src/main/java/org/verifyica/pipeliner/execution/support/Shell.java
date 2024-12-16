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

package org.verifyica.pipeliner.execution.support;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Enum to implement ShellType */
public enum Shell {

    /**
     * Invalid
     */
    INVALID,
    /**
     * Default
     */
    DEFAULT,
    /**
     * Bash
     */
    BASH,
    /**
     * sh
     */
    SH,
    /**
     * None
     */
    NONE;

    /**
     * Method to decode a string to ShellType
     *
     * @param string string
     * @return a ShellType
     */
    public static Shell decode(String string) {
        switch (string) {
            case "bash": {
                return BASH;
            }
            case "sh": {
                return SH;
            }
            case "none": {
                return NONE;
            }
            default: {
                return INVALID;
            }
        }
    }

    /**
     * Method to get command tokens
     *
     * @param shell shell
     * @param commandLine commandLine
     * @return an array of command line tokens
     */
    public static String[] toCommandTokens(Shell shell, String commandLine) {
        switch (shell) {
            case BASH: {
                return new String[] {"bash", "--noprofile", "--norc", "-eo", "pipefail", "-c", commandLine};
            }
            case SH: {
                return new String[] {"sh", "-e", "-c", commandLine};
            }
            case NONE: {
                return split(commandLine);
            }
            default: {
                return new String[] {"bash", "-e", "-c", commandLine};
            }
        }
    }

    /**
     * Method to split a command line into an array of command tokens
     *
     * @param commandLine commandLine
     * @return an array of command line tokens
     */
    private static String[] split(String commandLine) {
        // Regular expression for matching tokens
        String regex = "\"([^\"]*)\"|'([^']*)'|\\S+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(commandLine);

        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // Double-quoted token
                tokens.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                // Single-quoted token
                tokens.add(matcher.group(2));
            } else {
                // Unquoted token
                tokens.add(matcher.group());
            }
        }

        return tokens.toArray(new String[0]);
    }
}
