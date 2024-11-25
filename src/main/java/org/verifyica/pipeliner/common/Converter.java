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

package org.verifyica.pipeliner.common;

import java.util.List;
import java.util.Map;

/** Class to implement Converter */
@SuppressWarnings("unchecked")
public class Converter {

    /** Constructor */
    public Converter() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to convert an Object to a boolean
     *
     * @param object object
     * @return a boolean
     */
    public boolean toBoolean(Object object) {
        String string = object.toString();

        switch (string) {
            case "true":
            case "yes":
            case "y":
            case "on":
            case "1": {
                return true;
            }
            case "false":
            case "no":
            case "n":
            case "off":
            case "0": {
                return false;
            }
            default: {
                throw new IllegalArgumentException("not a boolean");
            }
        }
    }

    /**
     * Method to cast an Object to a String
     *
     * @param object object
     * @return a String
     */
    public String toString(Object object) {
        return (String) object;
    }

    /**
     * Method to cast an Object to a String
     *
     * @param object object
     * @param defaultValue defaultValue
     * @return a String
     */
    public String toString(Object object, String defaultValue) {
        if (object == null) {
            return defaultValue;
        }

        return toString(object);
    }

    /**
     * Method to cast an Object to a Map
     *
     * @param object object
     * @return a Map
     */
    public Map<String, Object> toMap(Object object) {
        return (Map<String, Object>) object;
    }

    /**
     * Method to cast an Object to a List
     *
     * @param object object
     * @return a List
     */
    public List<Object> toList(Object object) {
        return (List<Object>) object;
    }
}
