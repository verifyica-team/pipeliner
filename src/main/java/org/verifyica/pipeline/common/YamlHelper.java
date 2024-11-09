/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.pipeline.common;

import java.util.List;
import java.util.Map;

/** Class to implement YamlHelper */
@SuppressWarnings("unchecked")
public class YamlHelper {

    /** Constructor */
    private YamlHelper() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to cast an Object to a boolean
     *
     * @param object object
     * @param defaultValue defaultValue
     * @return a boolean value
     */
    public static boolean asBoolean(Object object, boolean defaultValue) {
        if (object == null) {
            return defaultValue;
        }

        if (!"true".equals(object.toString())) {
            return false;
        }

        return true;
    }

    /**
     * Method to cast an Object to a String
     *
     * @param object object
     * @return a String
     */
    public static String asString(Object object) {
        return (String) object;
    }

    /**
     * Method to cast an Object to a Map
     *
     * @param object object
     * @return a Map
     */
    public static Map<Object, Object> asMap(Object object) {
        return (Map<Object, Object>) object;
    }

    /**
     * Method to cast an Object to a List
     *
     * @param object object
     * @return a List
     */
    public static List<Object> asList(Object object) {
        return (List<Object>) object;
    }
}
