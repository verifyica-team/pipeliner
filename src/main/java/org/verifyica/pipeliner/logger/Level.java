/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Class to implement Level */
public final class Level {

    /** ERROR log level */
    public static final Level ERROR = new Level(100, "ERROR");

    /** WARN log level */
    public static final Level WARN = new Level(200, "WARN");

    /** INFO log level */
    public static final Level INFO = new Level(300, "INFO");

    /** DEBUG log level */
    public static final Level DEBUG = new Level(400, "DEBUG");

    /** TRACE log level */
    public static final Level TRACE = new Level(500, "TRACE");

    /** ALL log level */
    public static final Level ALL = new Level(Integer.MAX_VALUE, "ALL");

    private static final Map<String, Level> LEVEL_MAP = new HashMap<>();

    static {
        LEVEL_MAP.put(ERROR.toString(), ERROR);
        LEVEL_MAP.put(WARN.toString(), WARN);
        LEVEL_MAP.put(INFO.toString(), INFO);
        LEVEL_MAP.put(DEBUG.toString(), DEBUG);
        LEVEL_MAP.put(TRACE.toString(), TRACE);
        LEVEL_MAP.put(ALL.toString(), ALL);
    }

    private final int id;
    private final String string;

    /**
     * Constructor
     *
     * @param id the level id
     * @param string the level string
     */
    private Level(int id, String string) {
        this.id = id;
        this.string = string;
    }

    /**
     * Method to get the Level as an int
     *
     * @return the Level as an int
     */
    public int toInt() {
        return id;
    }

    /**
     * Method to get the Level as a string
     *
     * @return the level as a string
     */
    @Override
    public String toString() {
        return string;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Level level = (Level) object;
        return this.id == level.id && Objects.equals(string, level.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, string);
    }

    /**
     * Method to map a Level string to a Level
     *
     * @param input the input string
     * @return the decoded Level, or INFO if the not valid
     */
    public static Level decode(String input) {
        Level level = null;

        if (input != null && !input.trim().isEmpty()) {
            level = LEVEL_MAP.get(input.trim());
        }

        return level != null ? level : INFO;
    }
}
