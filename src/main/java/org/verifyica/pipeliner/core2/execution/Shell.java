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

package org.verifyica.pipeliner.core2.execution;

/**
 * ShellType
 */
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
    SH;

    public static Shell decode(String string) {
        if (string == null || string.trim().isEmpty()) {
            return INVALID;
        }

        switch (string.trim()) {
            case "bash": {
                return BASH;
            }
            case "sh": {
                return SH;
            }
            default: {
                return DEFAULT;
            }
        }
    }

    public static String[] toCommandTokens(Shell shell, String command) {
        switch (shell) {
            case BASH: {
                return new String[] {"bash", "--noprofile", "--norc", "-eo", "pipefail", "-c", command};
            }
            case SH: {
                return new String[] {"sh", "-e", "-c", command};
            }
            default: {
                return new String[] {"bash", "-e", "-c", command};
            }
        }
    }
}
