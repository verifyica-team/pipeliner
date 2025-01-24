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
    public void enableMinimal(boolean enableMinimal) {
        this.minimal = enableMinimal;
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
