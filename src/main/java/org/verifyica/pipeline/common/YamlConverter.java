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

package org.verifyica.pipeline.common;

import static java.lang.String.format;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Class to implement YamlConverter */
@SuppressWarnings("unchecked")
public class YamlConverter {

    /** Constructor */
    private YamlConverter() {
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
        return isYamlTrue(asString(object, "true"));
    }

    /**
     * Method to cast an Object to a String
     *
     * @param object object
     * @return a String
     */
    public static String asString(Object object) {
        try {
            return (String) object;
        } catch (ClassCastException e) {
            throw new YamlFormatException(
                    format("Can't cast [%s] to String", object.getClass().getName()));
        }
    }

    /**
     * Method to cast an Object to a String
     *
     * @param object object
     * @param defaultValue defaultValue
     * @return a String
     */
    public static String asString(Object object, String defaultValue) {
        if (object == null) {
            return defaultValue;
        }

        return asString(object);
    }

    /**
     * Method to cast an Object to a Map
     *
     * @param object object
     * @return a Map
     */
    public static Map<Object, Object> asMap(Object object) {
        try {
            return (Map<Object, Object>) object;
        } catch (ClassCastException e) {
            throw new YamlFormatException(format(
                    "Can't cast [%s] to Map<Object, Object>", object.getClass().getName()));
        }
    }

    /**
     * Method to cast an Object to a List
     *
     * @param object object
     * @return a List
     */
    public static List<Object> asList(Object object) {
        try {
            return (List<Object>) object;
        } catch (ClassCastException e) {
            throw new YamlFormatException(
                    format("Can't cast [%s] to List<Object>", object.getClass().getName()));
        }
    }

    /**
     * Method to determine if a string represents a YAML true
     *
     * @param string string
     * @return true if the string represents a YAML true, else false
     */
    public static boolean isYamlTrue(String string) {
        if (string == null) {
            return false;
        }

        string = string.trim().toLowerCase(Locale.US);

        switch (string) {
            case "true":
            case "yes":
            case "y":
            case "on":
            case "1":
                return true;
            default:
                return false;
        }
    }
}
