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

package org.verifyica.pipeliner.parser;

import java.util.HashMap;
import java.util.Map;

/** Enum to implement Modifier */
public enum Modifier {

    /**
     * Invalid modifier
     */
    INVALID("invalid"),
    /**
     * Mask modifier
     */
    MASK("mask");

    private static final Map<String, Modifier> MODIFIER_MAP;

    static {
        // Build the modifier map
        MODIFIER_MAP = new HashMap<>();
        for (Modifier modifier : values()) {
            MODIFIER_MAP.put(modifier.getValue(), modifier);
        }
    }

    private final String value;

    /**
     * Constructor
     *
     * @param value the value
     */
    Modifier(String value) {
        this.value = value;
    }

    /**
     * Method to get the value
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Method to decode a string to a modifier
     *
     * @param string the string
     * @return the modifier
     */
    public static Modifier decode(String string) {
        return MODIFIER_MAP.getOrDefault(string.trim(), INVALID);
    }
}
