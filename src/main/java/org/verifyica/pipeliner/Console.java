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

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Console class that provides methods to print messages to the console.
 */
public class Console {

    /**
     * DateTimeFormatter for timestamps
     */
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    /**
     * The output stream to print to.
     */
    private final PrintStream printStream;

    /**
     * Flag to control whether timestamps should be included in output.
     */
    private boolean enableTimestamps = false;

    /**
     * Constructs a new Console that prints to {@code System.out} with default verbosity.
     */
    public Console() {
        this.printStream = System.out;
    }

    /**
     * Enables or disables timestamp output for all printed messages.
     *
     * @param enableTimestamps {@code true} to prepend timestamps, {@code false} otherwise
     */
    public void setEnableTimestamps(boolean enableTimestamps) {
        this.enableTimestamps = enableTimestamps;
    }

    /**
     * Prints an empty line.
     */
    public void println() {
        printStream.println();
    }

    /**
     * Prints a plain message followed by a newline.
     *
     * @param object the message to print
     */
    public void println(Object object) {
        // Print the formatted message
        println("%s", object);
    }

    /**
     * Prints a formatted message followed by a newline.
     * If timestamps are enabled, they are prepended to the output.
     *
     * @param format the format string
     * @param args   the arguments for the format string
     */
    public void println(String format, Object... args) {
        // Get the timestamp prefix if timestamps are enabled
        String timestampPrefix = enableTimestamps ? currentTimestamp() + " " : "";

        // Print the message with the timestamp prefix
        printStream.printf(timestampPrefix + format + "%n", args);
    }

    /**
     * Prints a message without appending a newline.
     * If timestamps are enabled, they are prepended to the output.
     *
     * @param object the message to print
     */
    public void print(Object object) {
        // Get the timestamp prefix if timestamps are enabled
        String timestampPrefix = enableTimestamps ? currentTimestamp() + " " : "";

        // Print the message with the timestamp prefix
        printStream.print(timestampPrefix + object);
    }

    /**
     * Returns the current timestamp formatted using the {@link #TIMESTAMP_FORMAT}.
     *
     * @return the formatted timestamp string
     */
    private static String currentTimestamp() {
        // Return the current timestamp formatted
        return TIMESTAMP_FORMAT.format(LocalDateTime.now());
    }
}
