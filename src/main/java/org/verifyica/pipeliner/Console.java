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

package org.verifyica.pipeliner;

import static java.lang.String.format;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement Console */
public class Console {

    private static final Logger LOGGER = LoggerFactory.getLogger(Console.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private boolean enableTimestamps;

    private Verbosity verbosity;

    /** Enum to represent verbosity */
    public enum Verbosity {

        /**
         * Normal verbosity
         */
        NORMAL,

        /**
         * Quiet verbosity
         */
        QUIET,

        /**
         * Quieter verbosity
         */
        QUIETER
    }

    /**
     * Constructor
     */
    public Console() {
        verbosity = Verbosity.NORMAL;
    }

    /**
     * Method to set the verbosity
     *
     * @param verbosity the verbosity
     */
    public void setVerbosity(Verbosity verbosity) {
        if (verbosity != null) {
            this.verbosity = verbosity;
        }
    }

    /**
     * Method to get verbosity
     *
     * @return verbosity
     */
    public Verbosity getVerbosity() {
        return verbosity;
    }

    /**
     * Method to enable timestamps
     *
     * @param enableTimestamps enable timestamps
     */
    public void setEnabledTimestamps(boolean enableTimestamps) {
        this.enableTimestamps = enableTimestamps;
    }

    /**
     * Method to return whether timestamps are enabled
     *
     * @return true if timestamps are enabled, else false
     */
    public boolean getTimestampsEnabled() {
        return enableTimestamps;
    }

    /**
     * Method to emit a message to the console
     *
     * @param format the format
     * @param objects the objects
     */
    public void emit(String format, Object... objects) {
        emit(format(format, objects));
    }

    /**
     * Method to emit a message toe the console
     *
     * @param object the object
     */
    public void emit(Object object) {
        String message = object.toString();
        String timestampMessage =
                (enableTimestamps ? LocalDateTime.now().format(DATE_TIME_FORMATTER) + " " : "") + object;

        if (verbosity == Verbosity.QUIETER) {
            if (message.startsWith(">") || message.startsWith("@error")) {
                System.out.println(timestampMessage);
            }
        } else if (verbosity == Verbosity.QUIET) {
            if (message.startsWith("$") || message.startsWith(">") || message.startsWith("@error")) {
                System.out.println(timestampMessage);
            }
        } else {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("emit %s", message);
            } else {
                System.out.println(enableTimestamps ? timestampMessage : message);
            }
        }

        System.out.flush();
    }
}
