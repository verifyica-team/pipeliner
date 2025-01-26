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

import java.util.Map;
import java.util.TreeMap;

/** Class to implement MapBuilder */
public class MapBuilder {

    private final Map<String, String> map;

    /**
     * Constructor
     */
    private MapBuilder() {
        this.map = new TreeMap<>();
    }

    /**
     * Method to put key-value pair in the map
     *
     * @param key key
     * @param value value
     * @return MapBuilder
     */
    public MapBuilder put(String key, String value) {
        map.put(key, value);
        return this;
    }

    /**
     * Method to build the map
     *
     * @return map
     */
    public Map<String, String> build() {
        return map;
    }

    /**
     * Method to get an instance of MapBuilder
     *
     * @return MapBuilder
     */
    public static MapBuilder builder() {
        return new MapBuilder();
    }
}
