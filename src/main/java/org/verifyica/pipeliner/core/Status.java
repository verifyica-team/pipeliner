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

package org.verifyica.pipeliner.core;

/** Enum to implement Status */
public enum Status {

    /** RUNNING */
    RUNNING("running"),

    /** DISABLED */
    DISABLED("disabled"),

    /** SKIPPED */
    SKIPPED("skipped"),

    /** SUCCESS */
    SUCCESS("success"),

    /** FAILURE */
    FAILURE("failure");

    private final String value;

    /**
     * Constructor
     *
     * @param value the value
     */
    Status(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
