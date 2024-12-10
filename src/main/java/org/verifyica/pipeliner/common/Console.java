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

    private static final Console INSTANCE = new Console();

    private boolean trace;
    private boolean timestamps;
    private boolean minimal;

    /**
     * Constructor
     */
    private Console() {
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
    public void log(String format, Object... objects) {
        log(format(format, objects));
    }

    /**
     * Method to log to the console
     *
     * @param object object
     */
    public void log(Object object) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        String prefix = timestamps ? timestamp + " " : "";
        String message = object.toString();
        String timestampMessage = prefix + object;

        if (timestamps && minimal) {
            if (message.startsWith("$") || message.startsWith(">") || message.startsWith("@error")) {
                System.out.println(timestampMessage);
                System.out.flush();
            }
        } else if (!timestamps && minimal) {
            if (message.startsWith("$") || message.startsWith(">") || message.startsWith("@error")) {
                System.out.println(timestampMessage);
                System.out.flush();
            }
        } else if (timestamps && !minimal) {
            System.out.println(timestampMessage);
            System.out.flush();
        } else if (!timestamps && !minimal) {
            System.out.println(message);
            System.out.flush();
        }
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
     * Method to logo an error print to the console
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
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static Console getInstance() {
        return INSTANCE;
    }
}
