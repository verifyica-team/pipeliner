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

package org.verifyica.pipeliner.model.support;

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
    DASH;

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
            default: {
                return INVALID;
            }
        }
    }
}
