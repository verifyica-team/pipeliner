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

package org.verifyica.pipeliner.engine;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.parser.Parser;

/**
 * Class representing the execution context of a pipeline, job, or step.
 */
public class Context {

    /**
     * Default working directory for the context.
     */
    private static final String DEFAULT_WORKING_DIRECTORY = ".";

    /**
     * Default shell for the context.
     */
    private static final String BASH_SHELL = "bash";

    /**
     * Default timeout in minutes for the context.
     */
    private static final String DEFAULT_TIMEOUT_MINUTES = "360";

    /**
     * Comparator to sort variables by name in alphabetical order, then by length (longest first).
     */
    private static final Comparator<String> BY_ALPHA_THEN_LONGEST =
            Comparator.<String>naturalOrder().thenComparing((s1, s2) -> Integer.compare(s2.length(), s1.length()));

    /**
     * The console for the context
     */
    private final Console console;

    /**
     * The map of environment variables for the context.
     */
    private final Map<String, String> environmentVariables;

    /**
     * The map of variables for the context.
     */
    private final Map<String, String> variables;

    /**
     * The stack of frames for the context.
     */
    private final Deque<Frame> frames;

    /**
     * Constructor
     *
     * @param console the console
     */
    public Context(Console console) {
        this.console = console;
        this.environmentVariables = new TreeMap<>(BY_ALPHA_THEN_LONGEST);
        this.variables = new TreeMap<>(BY_ALPHA_THEN_LONGEST);
        this.frames = new ArrayDeque<>();
    }

    /**
     * Get the console for the context.
     *
     * @return the console for the context
     */
    public Console getConsole() {
        return console;
    }

    /**
     * Get the map of environment variables for the context.
     *
     * @return the map of environment variables
     */
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * Get the scoped variables for the context.
     *
     * @return the map of scoped variables
     */
    public Map<String, String> getVariables() {
        return variables;
    }

    /**
     * Get the stack of frames for the context.
     *
     * @return the stack of frames
     */
    public Deque<Frame> getFrames() {
        return frames;
    }

    /**
     * Push a new frame onto the stack.
     *
     * @param frame the frame to push onto the stack
     */
    public void pushFrame(Frame frame) {
        frames.push(frame);
    }

    /**
     * Get the current frame from the stack.
     *
     * @return the current frame, or null if the stack is empty
     */
    public Frame getFrame() {
        return frames.peek();
    }

    /**
     * Pop the current frame from the stack.
     *
     * @return the popped frame, or null if the stack is empty
     */
    public Frame popFrame() {
        return frames.pop();
    }

    /**
     * Resolve variables in the context.
     * The maximum recursive iterations is set to 10 to prevent infinite loops.
     *
     * @param input the input string containing variables to resolve
     * @return the resolved string with variables replaced
     */
    public String resolveVariables(String input) {
        final int maxIterations = 10;
        String previous = input;
        String resolved;
        int iteration = 0;

        do {
            resolved = resolveVariables(previous, false);
            if (resolved.equals(previous)) {
                break;
            }
            previous = resolved;
            iteration++;
        } while (iteration < maxIterations);

        return resolved;
    }

    /**
     * Resolve all variables in the context, including environment variables.
     * The maximum recursive iterations is set to 10 to prevent infinite loops.
     *
     * @param input the input string containing variables to resolve
     * @return the resolved string with variables replaced
     */
    public String resolveAllVariables(String input) {
        final int maxIterations = 10;
        String previous = input;
        String resolved;
        int iteration = 0;

        do {
            resolved = resolveVariables(previous, true);
            if (resolved.equals(previous)) {
                break;
            }
            previous = resolved;
            iteration++;
        } while (iteration < maxIterations);

        return resolved;
    }

    /**
     * Retrieves the working directory from the context's frame stack.
     * It checks each frame in reverse order to find the most recent working directory.
     *
     * @return the working directory as a string
     */
    public String getWorkingDirectory() {
        // Set the default working directory
        String workingDirectory = DEFAULT_WORKING_DIRECTORY;

        // Get a descending iterator for the frames
        Iterator<Frame> frameIterator = frames.descendingIterator();

        while (frameIterator.hasNext()) {
            // Get the next frame from the iterator
            Frame frame = frameIterator.next();

            // Check if the frame has a working directory set
            if (frame.getWorkingDirectory() != null) {
                // If a working directory is found, use it
                workingDirectory = frame.getWorkingDirectory();

                // A working directory has been found, so break the loop
                break;
            }
        }

        // Resolve and return the working directory
        return resolveAllVariables(workingDirectory);
    }

    /**
     * Retrieves the shell from the context's frame stack.
     * It checks each frame in reverse order to find the most recent shell.
     *
     * @return the shell as a string, or "bash" if no shell is set
     */
    public String getShell() {
        // Set the default shell to bash
        String shell = BASH_SHELL;

        // Get a descending iterator for the frames
        Iterator<Frame> frameIterator = frames.descendingIterator();

        while (frameIterator.hasNext()) {
            // Get the next frame from the iterator
            Frame frame = frameIterator.next();

            // Check if the frame has a shell set
            if (frame.getShell() != null) {
                // If a shell is found, use it
                shell = frame.getShell();

                // A shell has been found, so break the loop
                break;
            }
        }

        // Resolve the shell variable
        shell = resolveAllVariables(shell);

        // If the shell is set to "default"
        if ("default".equals(shell)) {
            // Set the default shell to bash
            shell = BASH_SHELL;
        }

        // Resolve and return the shell
        return shell;
    }

    /**
     * Retrieves the timeout minutes from the context's frame stack.
     * It checks each frame in reverse order to find the most recent timeout minutes.
     *
     * @return the timeout minutes, defaulting to 360 minutes if not set
     */
    public String getTimeoutMinutes() {
        // Set the default timeout
        String timeoutMinutes = DEFAULT_TIMEOUT_MINUTES;

        // Get a descending iterator for the frames
        Iterator<Frame> frameIterator = frames.descendingIterator();

        while (frameIterator.hasNext()) {
            // Get the next frame from the iterator
            Frame frame = frameIterator.next();

            // Check if the frame has timeout minutes set
            if (frame.getTimeoutMinutes() != null) {
                // If timeout minutes are found, use them
                timeoutMinutes = frame.getTimeoutMinutes();

                // Timeout minutes have been found, so break the loop
                break;
            }
        }

        // Resolve and return the timeout minutes
        return resolveAllVariables(timeoutMinutes);
    }

    /**
     * Resolve variables in the input string.
     *
     * @param input the input string containing variables to resolve
     * @param resolveEnvironmentVariables whether to resolve environment variables
     * @return the resolved string with variables replaced
     */
    private String resolveVariables(String input, boolean resolveEnvironmentVariables) {
        StringBuilder stringBuilder = new StringBuilder();
        Parser.Token token;

        // Create a parser to tokenize the input string
        Parser parser = new Parser(input);

        // Parse the input string and iterate through the tokens
        while ((token = parser.next()) != null) {
            switch (token.getType()) {
                case ENVIRONMENT_VARIABLE: {
                    if (resolveEnvironmentVariables) {
                        String key = token.getValue();
                        String value = environmentVariables.getOrDefault(key, "");
                        stringBuilder.append(value);
                    } else {
                        stringBuilder.append(token.getText());
                    }
                    break;
                }
                case VARIABLE: {
                    String key = token.getValue();
                    String value = variables.getOrDefault(key, "");
                    stringBuilder.append(value);
                    break;
                }
                case TEXT:
                default: {
                    stringBuilder.append(token.getText());
                    break;
                }
            }
        }

        return stringBuilder.toString();
    }
}
