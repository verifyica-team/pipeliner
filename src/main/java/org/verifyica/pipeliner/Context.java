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

import static java.lang.String.format;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.verifyica.pipeliner.core.Scope;

/**
 * Context class that holds the execution context for a pipeline.
 */
public final class Context {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private final Path DEFAULT_WORKING_DIRECTORY = Path.of(".").normalize().toAbsolutePath();

    private final Console console;
    private final Deque<Scope> scopes;
    private Path workingDirectory;
    private final StringBuilder captureBuffer;
    private boolean capturingOutput = false;

    /**
     * Constructor
     *
     * @param console the console to use for output
     */
    public Context(Console console) {
        this.console = console;
        this.scopes = new ArrayDeque<>();
        this.scopes.push(new Scope());
        this.workingDirectory = DEFAULT_WORKING_DIRECTORY;
        this.captureBuffer = new StringBuilder();
        this.capturingOutput = false;
    }

    /**
     * Get the current scope level of the context.
     *
     * @return the current scope level, which is the number of frames not including the root scope
     */
    public int getScopeLevel() {
        return scopes.size() - 1;
    }

    /**
     * Gets the global scope from the context stack.
     *
     * @return the global scope
     */
    public Scope globalScope() {
        if (scopes.isEmpty()) {
            throw new IllegalStateException("No global scope available");
        }

        return scopes.getLast();
    }

    /**
     * Enters a new scope, pushing it onto the scope stack.
     */
    public void enterScope() {
        scopes.push(new Scope());
    }

    /**
     * Returns the current scope from the context stack.
     *
     * @return the current scope
     */
    public Scope currentScope() {
        if (scopes.isEmpty()) {
            throw new IllegalStateException("No scopes available");
        }

        return scopes.peek();
    }

    /**
     * Leaves the current scope, removing it from the scope stack.
     *
     * @return the popped scope
     * @throws IllegalStateException if there are no scopes to pop
     */
    public Scope exitScope() {
        if (scopes.isEmpty()) {
            throw new IllegalStateException("No scopes to pop");
        }

        return scopes.pop();
    }

    /**
     * Gets the working directory for this context.
     *
     * @return the working directory as a Path
     */
    public Path resolveWorkingDirectory() {
        Iterator<Scope> iterator = scopes.descendingIterator();
        while (iterator.hasNext()) {
            Path path = iterator.next().getWorkingDirectory();
            if (path != null) {
                return path;
            }
        }

        return workingDirectory;
    }

    /**
     * Retrieves an environment variable by its name.
     *
     * @param name the name of the environment variable to retrieve
     * @return the value of the environment variable, or an empty string if not found
     */
    public String resolveEnvironmentVariable(String name) {
        Iterator<Scope> iterator = scopes.descendingIterator();
        while (iterator.hasNext()) {
            String value = iterator.next().getEnvironmentVariable(name);
            if (value != null) {
                return value;
            }
        }

        return "";
    }

    /**
     * Resolves all environment variables from all scopes in the context.
     *
     * @return a map of environment variables where the key is the variable name and the value is the variable value
     */
    public Map<String, String> resolveEnvironmentVariables() {
        Map<String, String> environmentVariables = new HashMap<>();
        Iterator<Scope> iterator = scopes.descendingIterator();
        while (iterator.hasNext()) {
            environmentVariables.putAll(iterator.next().getEnvironmentVariables());
        }
        return environmentVariables;
    }

    /**
     * Retrieves a variable by its name.
     *
     * @param name the name of the variable to retrieve
     * @return the value of the variable, or an empty string if not found
     */
    public String resolveVariable(String name) {
        Iterator<Scope> iterator = scopes.descendingIterator();
        while (iterator.hasNext()) {
            String value = iterator.next().getVariable(name);
            if (value != null) {
                return value;
            }
        }

        return "";
    }

    /**
     * Prints an object to the console.
     *
     * @param argument the argument to print
     */
    public void print(Object argument) {
        if (capturingOutput) {
            captureBuffer.append(argument);
        } else {
            console.print(argument);
        }
    }

    /**
     * Prints an object to the console with a new line, optionally with a timestamp.
     *
     * @param argument the argument to print
     */
    public void println(Object argument) {
        if (capturingOutput) {
            captureBuffer.append(argument).append(System.lineSeparator());
        } else {
            console.println(argument);
        }
    }

    /**
     * Prints a formatted string to the console.
     *
     * @param format the format string
     * @param arguments the arguments to format
     */
    public void println(String format, Object... arguments) {
        if (capturingOutput) {
            captureBuffer.append(format(format, arguments)).append(System.lineSeparator());
        } else {
            console.println(format(format, arguments));
        }
    }
}
