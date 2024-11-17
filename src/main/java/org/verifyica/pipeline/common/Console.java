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

import static java.lang.String.format;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Class to implement Console */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Console {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH);

    private boolean suppressTimestamps;
    private boolean trace;
    private boolean logging;

    private PrintStream filePrintStream;

    /**
     * Constructor
     */
    public Console() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to initialize the console
     *
     * @throws IOException IOException
     */
    public void initialize() throws IOException {
        if (logging) {
            String filename = "pipeliner_" + simpleDateFormat.format(new Date()) + ".log";

            FileOutputStream fileOutputStream = new FileOutputStream(filename, false);
            filePrintStream = new PrintStream(fileOutputStream, true, StandardCharsets.UTF_8.name());

            trace("log filename [%s]", filename);
        }
    }

    /**
     * Method to enable suppressTimestamps
     *
     * @param suppressTimestamps suppressTimestamps
     */
    public void suppressTimestamps(boolean suppressTimestamps) {
        this.suppressTimestamps = suppressTimestamps;
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
     * Method to enable logging
     *
     * @param logging logging
     */
    public void enableLogging(boolean logging) {
        this.logging = logging;
    }

    /**
     * Method to log to the console
     *
     * @param format format
     * @param objects objects
     */
    public void log(String format, Object... objects) {
        String prefix = suppressTimestamps ? "" : Timestamp.now() + " ";
        String message = format(prefix + format, objects);

        System.out.println(message);
        System.out.flush();

        if (logging) {
            filePrintStream.println(message);
            filePrintStream.flush();
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

        if (filePrintStream != null) {
            try {
                filePrintStream.close();
            } catch (Throwable t) {
                // INTENTIONALLY BLANK
            }
        }
    }
}
