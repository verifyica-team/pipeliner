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

import static java.lang.String.format;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Class to implement Console */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Console {

    private static final DateTimeFormatter DATE_TIME_FORMATER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private boolean trace;
    private boolean timestamps;
    private boolean minimal;

    /**
     * Constructor
     */
    public Console() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to enable timestamps
     *
     * @param timestamps timestamps
     */
    public void enableTimestamps(boolean timestamps) {
        this.timestamps = timestamps;
    }

    /**
     * Method to enable trace
     *
     * @param trace trace
     */
    public void enableTrace(boolean trace) {
        this.trace = trace;
    }

    /**
     * Method to enable minimal
     *
     * @param minimal minimal
     */
    public void enableMinimal(boolean minimal) {
        this.minimal = minimal;
    }

    /**
     * Method to return if trace is enabled
     *
     * @return true if trace is enabled, else false
     */
    public boolean isTraceEnabled() {
        return trace;
    }

    /**
     * Method to log to the console
     *
     * @param format format
     * @param objects objects
     */
    public void info(String format, Object... objects) {
        log(format(format, objects));
    }

    /**
     * Method to log to the console
     *
     * @param object object
     */
    public void info(Object object) {
        log(object);
    }

    /**
     * Method to log a warning print to the console
     *
     * @param object object
     */
    public void warning(Object object) {
        log(format("@warning %s", object));
    }

    /**
     * Method to log a warning print to the console
     *
     * @param format format
     * @param objects objects
     */
    public void warning(String format, Object... objects) {
        log("@warning " + format, objects);
    }

    /**
     * Method to log an error print to the console
     *
     * @param object object
     */
    public void error(Object object) {
        log(format("@error %s", object));
    }

    /**
     * Method to log an error print to the console
     *
     * @param format format
     * @param objects objects
     */
    public void error(String format, Object... objects) {
        log("@error " + format, objects);
    }

    /**
     * Method to log a trace print to the console
     *
     * @param object object
     */
    public void trace(Object object) {
        if (trace) {
            log("@trace " + object);
        }
    }

    /**
     * Method to log a trace print to the console
     *
     * @param format format
     * @param objects objects
     */
    public void trace(String format, Object... objects) {
        if (trace) {
            log("@trace " + format, objects);
        }
    }

    /**
     * Method to close the console
     */
    public void close() {
        System.out.flush();
    }

    /**
     * Method to close the console and exit
     *
     * @param exitCode exitCode
     */
    public void closeAndExit(int exitCode) {
        close();
        System.exit(exitCode);
    }

    /**
     * Method to log to the console
     *
     * @param format format
     * @param objects objects
     */
    private void log(String format, Object... objects) {
        log(format(format, objects));
    }

    /**
     * Method to log to the console
     *
     * @param object object
     */
    private void log(Object object) {
        String message = object.toString();
        String timestampMessage = (timestamps ? LocalDateTime.now().format(DATE_TIME_FORMATER) + " " : "") + object;

        if (minimal) {
            if (message.startsWith("$") || message.startsWith(">") || message.startsWith("@error")) {
                System.out.println(timestampMessage);
                System.out.flush();
            }
        } else {
            System.out.println(timestamps ? timestampMessage : message);
            System.out.flush();
        }
    }
}
