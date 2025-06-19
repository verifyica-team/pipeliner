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

/**
 * Enum representing the verbosity levels for the Pipeliner engine.
 * <p>
 * The verbosity levels are:
 * <ul>
 *     <li>NORMAL: no flag</li>
 *     <li>QUIET: -q</li>
 *     <li>QUIETER: -qq</li>
 * </ul>
 */
public enum Verbosity {

    /**
     * Verbosity level for no output
     */
    NONE(0),

    /**
     * Verbosity level for normal output
     */
    NORMAL(1);

    private final int level;

    /**
     * Constructor
     *
     * @param level the verbosity level
     */
    Verbosity(int level) {
        this.level = level;
    }

    /**
     * Gets the verbosity level.
     *
     * @return the verbosity level
     */
    public int level() {
        return level;
    }

    /**
     * Checks if the verbosity level is none.
     *
     * @return true if the verbosity level is none, otherwise false
     */
    public boolean isNone() {
        return level == 0;
    }

    /**
     * Checks if the verbosity level is quieter than normal.
     *
     * @return true if the verbosity level not normal, otherwise false
     */
    public boolean isNormal() {
        return level == NORMAL.level;
    }
}
