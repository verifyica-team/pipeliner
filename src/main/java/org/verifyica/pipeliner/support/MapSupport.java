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

package org.verifyica.pipeliner.support;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class for working with Maps, providing a way to handle optional values
 * and perform operations on them without the need for null checks.
 */
public class MapSupport {

    private MapSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Creates an OptionalValue from a Map and a key. If the value is present,
     *
     * @param map the map to retrieve the value from
     * @param key the key to look for in the map
     * @return an OptionalValue containing the value associated with the key, or null if not present
     * @param <T> the type of the value associated with the key
     */
    public static <T> OptionalValue<T> ifPresent(Map<String, Object> map, String key) {
        return new OptionalValue<>(map.get(key), key);
    }

    /**
     * Represents an optional value that may or may not be present in a Map.
     *
     * @param <T> the type of the value
     */
    public static class OptionalValue<T> {

        private final Object value;
        private final String key;

        /**
         * Constructs an OptionalValue with the given value and key.
         *
         * @param value the value, which may be null
         * @param key the key associated with the value
         */
        OptionalValue(Object value, String key) {
            this.value = value;
            this.key = key;
        }

        /**
         * Checks if the value is present (not null). If present, calls the provided consumer with the value.
         *
         * @param consumer the consumer to call with the value if it is present
         */
        public void ifPresent(Consumer<T> consumer) {
            if (value != null) {
                @SuppressWarnings("unchecked")
                T casted = (T) value;
                consumer.accept(casted);
            }
        }

        /**
         * Maps the value to another type using the provided converter function.
         * If the value is null, returns a new OptionalValue with null.
         *
         * @param converter the function to convert the value to another type
         * @return a new OptionalValue containing the converted value or null if the original value was null
         * @param <R> the type of the converted value
         */
        public <R> OptionalValue<R> map(Function<T, R> converter) {
            if (value == null) {
                return new OptionalValue<>(null, key);
            }

            try {
                @SuppressWarnings("unchecked")
                T casted = (T) value;
                return new OptionalValue<>(converter.apply(casted), key);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("key [" + key + "] has incorrect type", e);
            }
        }
    }
}
