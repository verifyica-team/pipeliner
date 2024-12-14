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
     * @param command command
     * @return an array of command tokens
     */
    public static String[] toCommandTokens(Shell shell, String command) {
        switch (shell) {
            case BASH: {
                return new String[] {"bash", "--noprofile", "--norc", "-eo", "pipefail", "-c", command};
            }
            case SH: {
                return new String[] {"sh", "-e", "-c", command};
            }
            case NONE: {
                return new String[] {command};
            }
            default: {
                return new String[] {"bash", "-e", "-c", command};
            }
        }
    }
}
