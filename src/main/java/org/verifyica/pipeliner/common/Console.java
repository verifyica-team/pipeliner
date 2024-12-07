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

import java.io.File;
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

    private static final Console INSTANCE = new Console();

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH);

    private boolean trace;
    private boolean logging;
    private boolean timestamps;
    private boolean minimal;

    private PrintStream filePrintStream;

    /**
     * Constructor
     */
    private Console() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to initialize the console
     *
     * @throws IOException IOException
     */
    public void initialize() throws IOException {
        if (logging) {
            String filename;
            File file;

            do {
                filename = "pipeline_" + simpleDateFormat.format(new Date()) + ".log";
                file = new File(filename);

                try {
                    Thread.sleep(100);
                } catch (Throwable t) {
                    // INTENTIONALLY BLANK
                }
            } while (file.exists());

            FileOutputStream fileOutputStream = new FileOutputStream(filename, false);
            filePrintStream = new PrintStream(fileOutputStream, true, StandardCharsets.UTF_8.name());

            trace("log filename [%s]", filename);
        }
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
     * Method to enable logging
     *
     * @param logging logging
     */
    public void enableLogging(boolean logging) {
        this.logging = logging;
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
        String timestamp = Timestamp.now();
        String prefix = timestamps ? timestamp + " " : "";
        String message = prefix + object;

        if (minimal && !message.startsWith("$") && !message.startsWith(">") && !message.startsWith("@error")) {
            return;
        }

        System.out.println(message);
        System.out.flush();

        if (logging) {
            filePrintStream.println(message);
            filePrintStream.flush();
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

        if (filePrintStream != null) {
            try {
                filePrintStream.close();
            } catch (Throwable t) {
                // INTENTIONALLY BLANK
            }
        }
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
