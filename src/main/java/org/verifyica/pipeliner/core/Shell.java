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

package org.verifyica.pipeliner.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Enum to implement Shell */
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
     * ZSH
     */
    ZSH,

    /**
     * FISH
     */
    FISH,

    /**
     * KSH
     */
    KSH,

    /**
     * Dash
     */
    DASH,

    /**
     * None
     */
    NONE;

    private static final Pattern SHELL_REGEX_PATTERN = Pattern.compile("\"([^\"]*)\"|'([^']*)'|\\S+");

    /**
     * Method to decode a string to ShellType
     *
     * @param input the input string
     * @return a ShellType
     */
    public static Shell decode(String input) {
        switch (input) {
            case "default": {
                return DEFAULT;
            }
            case "bash": {
                return BASH;
            }
            case "sh": {
                return SH;
            }
            case "zsh": {
                return ZSH;
            }
            case "fish": {
                return FISH;
            }
            case "ksh": {
                return KSH;
            }
            case "dash": {
                return DASH;
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
     * @param shell the shell
     * @param command the command
     * @return an array of command line tokens
     */
    public static String[] getProcessBuilderCommandArguments(Shell shell, String command) {
        switch (shell) {
            case DEFAULT:
            case BASH: {
                return new String[] {"bash", "--noprofile", "--norc", "-eo", "pipefail", "-c", command};
            }
            case SH: {
                return new String[] {"sh", "-e", "-c", command};
            }
            case ZSH: {
                return new String[] {"zsh", "--no-rcs", "-o", "pipefail", "-c", command};
            }
            case FISH: {
                return new String[] {"fish", "--private", "-c", command};
            }
            case KSH: {
                return new String[] {"ksh", "-c", "set -o pipefail; " + command};
            }
            case DASH: {
                return new String[] {"dash", "-e", "-c", command};
            }
            case NONE: {
                return split(command);
            }
            default: {
                return new String[] {"bash", "-e", "-c", command};
            }
        }
    }

    /**
     * Method to split a command into an array of command tokens
     *
     * @param command the command
     * @return an array of command line tokens
     */
    private static String[] split(String command) {
        Matcher matcher = SHELL_REGEX_PATTERN.matcher(command);

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
