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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Class to implement LoggerFactory */
public class LoggerFactory {

    private static final Map<String, Logger> LOGGER_MAP = new ConcurrentHashMap<>();

    /** Constructor */
    private LoggerFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get a logger
     *
     * @param clazz clazz
     * @return a Logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * Method to get a logger
     *
     * @param name name
     * @return a Logger
     */
    public static Logger getLogger(String name) {
        return LOGGER_MAP.computeIfAbsent(name.trim(), n -> new Logger(name));
    }
}
