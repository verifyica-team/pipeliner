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

package org.verifyica.pipeliner.logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.support.Precondition;

/** Class to implement LoggerFactory */
public final class LoggerFactory {

    private static final Logger ROOT_LOGGER;
    private static Level LEVEL;

    static {
        String value = System.getenv(Constants.PIPELINER_TRACE);
        if (Constants.TRUE.equalsIgnoreCase(value) || Constants.ONE.equalsIgnoreCase(value)) {
            LEVEL = Level.TRACE;
        } else {
            LEVEL = Level.INFO;
        }

        ROOT_LOGGER = new Logger("<ROOT>", LEVEL);
    }

    private final Map<String, Logger> loggers = new ConcurrentHashMap<>();

    /**
     * Constructor
     */
    private LoggerFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get or create a Logger
     *
     * @param name the logger name
     * @return a logger
     */
    private Logger getOrCreateLogger(String name) {
        return loggers.computeIfAbsent(name, string -> new Logger(string, LEVEL));
    }

    /**
     * Method to set the logging level
     *
     * @param level the level
     */
    public static void setLevel(Level level) {
        Precondition.notNull(level, "level is null");

        ROOT_LOGGER.setLevel(level);
        LEVEL = level;
    }
    /**
     * Method to get a Logger for a Class
     *
     * @param clazz the class
     * @return a Logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return clazz != null ? getLogger(clazz.getName()) : ROOT_LOGGER;
    }

    /**
     * Method to get a Logger by name
     *
     * @param name the logger name
     * @return a logger
     */
    public static Logger getLogger(String name) {
        Logger logger;

        if (name != null && !name.isBlank()) {
            logger = SingletonHolder.SINGLETON.getOrCreateLogger(name.trim());
        } else {
            logger = ROOT_LOGGER;
        }

        return logger;
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final LoggerFactory SINGLETON = new LoggerFactory();
    }
}
