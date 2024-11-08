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

package org.verifyica.pipeline.logger;

import static java.lang.String.format;

import org.verifyica.pipeline.common.Timestamp;

/** Class to implement Logger */
public class Logger {

    private final String name;

    /**
     * Constructor
     *
     * @param name name
     */
    Logger(String name) {
        this.name = name;
    }

    /**
     * Method to emit a trace message
     *
     * @param object object
     */
    public void trace(Object object) {
        System.out.printf("%s T %s %s", name, Timestamp.now(), object);
    }

    /**
     * Method to emit a trace message
     *
     * @param format format
     * @param objects objects
     */
    public void trace(String format, Object... objects) {
        trace(format(format, objects));
    }
}
