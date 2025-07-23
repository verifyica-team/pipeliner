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

package org.verifyica.pipeliner.core;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/**
 * Context class that holds the execution context for a pipeline.
 */
public final class Context {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private final Path DEFAULT_WORKING_DIRECTORY = Path.of(".").normalize().toAbsolutePath();

    private final Console console;
    private final Deque<Frame> frames;
    private Path workingDirectory;
    private boolean isTimestampsEnabled;

    /**
     * Constructor
     *
     * @param console the console to use for output
     */
    public Context(Console console) {
        this.console = console;
        this.frames = new ArrayDeque<>();
        this.frames.push(new Frame());
        this.workingDirectory = DEFAULT_WORKING_DIRECTORY;
    }

    /**
     * Enables timestamps for this context.
     */
    public void enableTimestamps() {
        isTimestampsEnabled = true;
    }

    /**
     * Pushes a new frame onto the context stack.
     */
    public void pushFrame() {
        frames.push(new Frame());
    }

    /**
     * Returns the current frame from the context stack.
     *
     * @return the current frame
     */
    public Frame currentFrame() {
        if (frames.isEmpty()) {
            throw new IllegalStateException("No frames available");
        }

        return frames.peek();
    }

    /**
     * Pops the current frame from the context stack.
     *
     * @return the popped frame
     * @throws IllegalStateException if there are no frames to pop
     */
    public Frame popFrame() {
        if (frames.isEmpty()) {
            throw new IllegalStateException("No frames to pop");
        }

        return frames.pop();
    }

    /**
     * Gets the working directory for this context.
     *
     * @return the working directory as a Path
     */
    public Path resolveWorkingDirectory() {
        for (Frame frame : frames) {
            Path path = frame.getWorkingDirectory();
            if (path != null) {
                return path;
            }
        }

        return workingDirectory;
    }

    /**
     * Retrieves a property by its name.
     *
     * @param name the name of the property to retrieve
     * @return the value of the property, or an empty string if not found
     */
    public String resolveProperty(String name) {
        for (Frame frame : frames) {
            String value = frame.getProperty(name);
            if (value != null) {
                return value;
            }
        }

        return "";
    }

    /**
     * Retrieves an environment variable by its name.
     *
     * @param name the name of the environment variable to retrieve
     * @return the value of the environment variable, or an empty string if not found
     */
    public String resolveEnvironmentVariable(String name) {
        for (Frame frame : frames) {
            String value = frame.getEnvironmentVariable(name);
            if (value != null) {
                return value;
            }
        }

        return "";
    }

    /**
     * Retrieves a variable by its name.
     *
     * @param name the name of the variable to retrieve
     * @return the value of the variable, or an empty string if not found
     */
    public String resolveVariable(String name) {
        for (Frame frame : frames) {
            String value = frame.getVariable(name);
            if (value != null) {
                return value;
            }
        }

        return "";
    }

    /**
     * Prints an object to the console, optionally with a timestamp.
     *
     * @param argument the argument to print
     */
    public void println(Object argument) {
        console.println(argument);
    }

    /**
     * Prints a formatted message to the console, optionally with a timestamp.
     *
     * @param format the format string
     * @param arguments the arguments to format
     */
    public void println(String format, Object... arguments) {
        String message = String.format(format, arguments);
        if (isTimestampsEnabled) {
            message = DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " " + message;
        }
        System.out.println(message);
    }
}
