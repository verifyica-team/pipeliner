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

package org.verifyica.pipeline.common;

/** Class to implement Console */
public class Console {

    private boolean suppressTimestamps;
    private boolean trace;

    /**
     * Constructor
     */
    public Console() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to set suppressTimestamps
     *
     * @param suppressTimestamps suppressTimestamps
     */
    public void setSuppressTimestamps(boolean suppressTimestamps) {
        this.suppressTimestamps = suppressTimestamps;
    }

    /**
     * Method to set tracee
     *
     * @param trace trace
     */
    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    /**
     * Method to log to the console
     *
     * @param format format
     * @param objects objects
     */
    public void log(String format, Object... objects) {
        String prefix = suppressTimestamps ? "" : Timestamp.now() + " ";
        System.out.printf(prefix + format + "%n", objects);
    }

    /**
     * Method to log to the console
     *
     * @param format format
     * @param objects objects
     */
    public void trace(String format, Object... objects) {
        if (trace) {
            String prefix = suppressTimestamps ? "" : Timestamp.now() + " ";
            System.out.printf(prefix + "@trace " + format + "%n", objects);
        }
    }
}
