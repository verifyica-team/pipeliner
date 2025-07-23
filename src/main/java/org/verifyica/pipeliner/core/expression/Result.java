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

package org.verifyica.pipeliner.core.expression;

/**
 * Represents the result of an expression evaluation.
 */
public class Result {

    private final Object value;

    /**
     * Constructor
     *
     * @param value the value of the result, can be null
     */
    public Result(Object value) {
        this.value = value;
    }

    /**
     * Gets the raw value of the result.
     *
     * @return the raw value, can be null
     */
    public Object raw() {
        return value;
    }

    /**
     * Converts the result to a string.
     *
     * @return the value, can be null
     */
    public String asString() {
        return value != null ? value.toString() : null;
    }

    /**
     * Converts the result to an integer.
     *
     * @return the integer value of the result
     */
    public long asLong() {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(asString());
    }

    /**
     * Converts the result to a boolean.
     *
     * @return the boolean value of the result
     */
    public boolean asBoolean() {
        return Boolean.parseBoolean(asString());
    }

    @Override
    public String toString() {
        return "Result{" + "value=" + value + '}';
    }
}
