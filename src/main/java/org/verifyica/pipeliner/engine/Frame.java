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

/**
 * Represents a frame in the execution context of a pipeline, job, or step.
 */
public class Frame {

    /**
     * Enum representing the type of frame.
     */
    public enum Type {
        /**
         * Pipeline framee type
         */
        PIPELINE("pipeline"),

        /**
         * Job frame type
         */
        JOB("job"),

        /**
         * Step frame type
         */
        STEP("step");

        private final String value;

        /**
         * Constructor for Type enum
         *
         * @param value the string representation of the type
         */
        Type(String value) {
            this.value = value;
        }

        /**
         * Get the string representation of the type.
         *
         * @return the string value of the type
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * The type of the frame
     */
    private Type type;

    /**
     * The name of the frame
     */
    private String name;

    /**
     * The description of the frame, e.g., "My Pipeline to build and test the project", "Build Job for the project", etc.
     */
    private String description;

    /**
     * The working directory for the frame, e.g., "/path/to/dir"
     */
    private String workingDirectory;

    /**
     * The shell to use for executing commands in the frame, e.g., "bash", "zsh", etc.
     */
    private String shell;

    /**
     * The timeout in minutes for the frame, e.g., "30" for 30 minutes.
     */
    private String timeoutMinutes;

    /**
     * Constructor
     *
     * @param type the type of the frame, e.g., "pipeline", "job", or "step"
     */
    public Frame(Type type) {
        this.type = type;
    }

    /**
     * Get the type of the frame.
     *
     * @return the type of the frame
     */
    public Type getType() {
        return type;
    }

    /**
     * Set the name of the frame.
     *
     * @param name the name of the frame
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the name of the frame.
     *
     * @return the name of the frame
     */
    public String getName() {
        return name;
    }

    /**
     * Set the description of the frame.
     *
     * @param description the description of the frame
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the description of the frame.
     *
     * @return the description of the frame
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the working directory for the frame.
     *
     * @param workingDirectory the working directory
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Get the working directory for the frame.
     *
     * @return the working directory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Set the shell for the frame.
     *
     * @param shell the shell to use, e.g., "/bin/bash", "cmd.exe"
     */
    public void setShell(String shell) {
        this.shell = shell;
    }

    /**
     * Get the shell for the frame.
     *
     * @return the shell to use
     */
    public String getShell() {
        return shell;
    }

    /**
     * Set the timeout in minutes for the frame.
     *
     * @param timeoutMinutes the timeout in minutes, e.g., 30
     */
    public void setTimeoutMinutes(String timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }

    /**
     * Get the timeout in minutes for the frame.
     *
     * @return the timeout in minutes, or null if not set
     */
    public String getTimeoutMinutes() {
        return timeoutMinutes;
    }

    /**
     * Returns a string representation of the frame suitable for console output.
     *
     * @return a string representation of the frame
     */
    public String toConsoleString() {
        StringBuilder stringBuilder = new StringBuilder("@");

        stringBuilder.append(type.getValue());

        if (name != null && !name.trim().isEmpty()) {
            stringBuilder.append(" name=[").append(name).append("]");
        }

        if (description != null && !description.trim().isEmpty()) {
            stringBuilder.append(" description=[").append(description).append("]");
        }

        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return "Frame {" + " type=["
                + type + "]"
                + " name=[" + name + "]"
                + " description=[" + description + "]"
                + " }";
    }
}
