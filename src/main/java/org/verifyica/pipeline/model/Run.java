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

package org.verifyica.pipeline.model;

/** Class to implement Run */
public class Run {

    /** Capture type */
    public enum CaptureType {
        /** None */
        NONE,
        /** Overwrite */
        OVERWRITE,
        /** Append */
        APPEND
    }

    private final String command;
    private final String executableCommand;
    private final CaptureType captureType;
    private final String captureVariable;

    /**
     * Constructor
     *
     * @param command command
     */
    public Run(String command) {
        this.command = command.trim();
        this.executableCommand = parseExecutableCommand(command).trim();
        this.captureType = parseCaptureType(command);
        this.captureVariable = parseCaptureVariable(command);
    }

    /**
     * Method to get the raw command
     *
     * @return the raw command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Method to get the executable command
     *
     * @return the executable command
     */
    public String getExecutableCommand() {
        return executableCommand;
    }

    /**
     * Method to get the capture type
     *
     * @return the capture type
     */
    public CaptureType getCaptureType() {
        return captureType;
    }

    /**
     * Method to get the capture variable
     *
     * @return the capture variable
     */
    public String getCaptureVariable() {
        return captureVariable;
    }

    private static String parseExecutableCommand(String command) {
        String pattern = ".*>>\\s+\\$\\w+$";
        if (command.matches(pattern)) {
            return command.substring(0, command.lastIndexOf(">>"));
        }

        pattern = ".*>\\s+\\$\\w+$";
        if (command.matches(pattern)) {
            return command.substring(0, command.lastIndexOf(">"));
        }

        return command;
    }

    private static CaptureType parseCaptureType(String command) {
        String pattern = ".*>>\\s+\\$\\w+$";
        if (command.matches(pattern)) {
            return CaptureType.APPEND;
        }

        pattern = ".*>\\s+\\$\\w+$";
        if (command.matches(pattern)) {
            return CaptureType.OVERWRITE;
        }

        return CaptureType.NONE;
    }

    private static String parseCaptureVariable(String command) {
        String pattern = ".*>>\\s+\\$\\w+$";
        if (command.matches(pattern)) {
            return command.substring(command.lastIndexOf("$") + 1).trim();
        }

        pattern = ".*>\\s+\\$\\w+$";
        if (command.matches(pattern)) {
            return command.substring(command.lastIndexOf("$") + 1).trim();
        }

        return null;
    }
}
