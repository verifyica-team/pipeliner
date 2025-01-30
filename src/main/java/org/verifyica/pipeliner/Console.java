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

package org.verifyica.pipeliner;

import static java.lang.String.format;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement Console */
public class Console {

    private static final Logger LOGGER = LoggerFactory.getLogger(Console.class);

    private static final DateTimeFormatter DATE_TIME_FORMATER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private boolean timestamps;
    private boolean quiet;
    private boolean quieter;

    /**
     * Constructor
     */
    public Console() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to enable timestamps
     *
     * @param enableTimestamp enable timestamps
     */
    public void enableTimestamps(boolean enableTimestamp) {
        this.timestamps = enableTimestamp;
    }

    /**
     * Method to enable minimal
     *
     * @param enableMinimal enable minimal
     */
    public void enableQuiet(boolean enableMinimal) {
        this.quiet = enableMinimal;
    }

    /**
     * Method to enable extra minimal
     *
     * @param extraMinimal enable extra minimal
     */
    public void enableQuieter(boolean extraMinimal) {
        this.quieter = extraMinimal;
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
        String timestampMessage = (timestamps ? LocalDateTime.now().format(DATE_TIME_FORMATER) + " " : "") + object;

        if (quieter) {
            if (message.startsWith(">") || message.startsWith("@error")) {
                System.out.println(timestampMessage);
            }
        } else if (quiet) {
            if (message.startsWith("$") || message.startsWith(">") || message.startsWith("@error")) {
                System.out.println(timestampMessage);
            }
        } else {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("emit %s", message);
            } else {
                System.out.println(timestamps ? timestampMessage : message);
            }
        }

        System.out.flush();
    }
}
