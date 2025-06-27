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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Utility class to check if a string represents a YAML-style boolean true.
 */
public final class Enabled {

    /**
     * Set of strings that represent true values in YAML.
     */
    private static final Set<String> YAML_TRUE_VALUES = new HashSet<>(Arrays.asList("true", "yes", "on"));

    /**
     * Set of strings that represent false values in YAML.
     */
    private static final Set<String> YAML_FALSE_VALUES = new HashSet<>(Arrays.asList("false", "no", "off"));

    /**
     * Constructor
     */
    private Enabled() {
        // Utility class, prevent instantiation
    }

    /**
     * Method to check if an enabled value is valid
     *
     * @param value the value
     * @return true if the value is a YAML-style true or false, false otherwise
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        } else if (YAML_TRUE_VALUES.contains(value.trim().toLowerCase(Locale.ENGLISH))) {
            return true;
        } else {
            return YAML_FALSE_VALUES.contains(value.trim().toLowerCase(Locale.ENGLISH));
        }
    }

    /**
     * Method to check if an enabled value is invalid
     *
     * @param value the value
     * @return true if the value is not a YAML-style true, false otherwise
     */
    public static boolean isInvalid(String value) {
        return !isValid(value);
    }
}
