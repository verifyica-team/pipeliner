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

package org.verifyica.pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.verifyica.pipeline.common.Timestamp;

/** Class to implement Step */
public class Step {

    private String name;
    private Map<String, String> properties;
    private boolean enabled;
    private String directory;
    private String command;
    private int exitCode;

    /** Constructor */
    public Step() {
        initialize();
    }

    /**
     * Method to initialize the step
     */
    private void initialize() {
        name = UUID.randomUUID().toString();
        enabled = true;
        properties = new LinkedHashMap<>();
        directory = ".";
    }

    /**
     * Method to set the name
     *
     * @param name name
     */
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    /**
     * Method to get the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Method to set the map of properties
     *
     * @param properties properties
     */
    public void setProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    /**
     * Method to get the map of properties
     *
     * @return the map of properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Method to set enabled
     *
     * @param enabled enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Method to get enabled
     *
     * @return true if enabled, else false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Method to set the directory
     *
     * @param directory directory
     */
    public void setDirectory(String directory) {
        if (directory != null && !directory.trim().isEmpty()) {
            this.directory = directory;
        }
    }

    /**
     * Method to get the directory
     *
     * @return the directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Method to set the command
     *
     * @param command command
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Method to get the command
     *
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Method to set the exit code
     *
     * @param exitCode exitCode
     */
    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Method to get the exit code
     *
     * @return the exit code
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Method to run the step
     *
     * @param pipeline pipeline
     * @param job job
     * @param outPrintStream outPrintStream
     * @param errorPrintStream errorPrintStream
     */
    public void run(Pipeline pipeline, Job job, PrintStream outPrintStream, PrintStream errorPrintStream) {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.putAll(pipeline.getProperties());
        properties.putAll(job.getProperties());
        properties.putAll(getProperties());

        outPrintStream.println(Timestamp.now() + " $ " + replace(getCommand(), properties));

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-e", "-c", replace(getCommand(), properties));
        processBuilder.directory(new File(replace(getDirectory(), properties)));

        try {
            Process process;

            try {
                process = processBuilder.start();
            } catch (Throwable t) {
                processBuilder.command("sh", "-e", "-c", replace(getCommand(), properties));
                process = processBuilder.start();
            }

            try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String line;
                String[] tokens;

                while ((line = outputReader.readLine()) != null) {
                    tokens = line.split("\\R");
                    for (String token : tokens) {
                        outPrintStream.println(Timestamp.now() + " > " + token);
                    }
                }

                while ((line = errorReader.readLine()) != null) {
                    tokens = line.split("\\R");
                    for (String token : tokens) {
                        errorPrintStream.println(Timestamp.now() + " > " + token);
                    }
                }
            }

            setExitCode(process.waitFor());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(errorPrintStream);
            setExitCode(1);
        }
    }

    @Override
    public String toString() {
        return "Step {" + "name='"
                + name + '\'' + ", directory='"
                + directory + '\'' + ", command='"
                + command + '\'' + '}';
    }

    /**
     * Method to replace environment variables and properties in a string
     *
     * @param string string
     * @param properties properties
     * @return the string with environment variables and properties replaced
     */
    private static String replace(String string, Map<String, String> properties) {
        Pattern pattern = Pattern.compile("(?<!\\\\)\\{\\{(.*?)}}");
        String previousResult;

        do {
            previousResult = string;
            Matcher matcher = pattern.matcher(string);
            StringBuilder result = new StringBuilder();

            while (matcher.find()) {
                String variableName = matcher.group(1);
                String replacement = System.getenv(variableName);

                if (replacement == null) {
                    replacement = properties.get(variableName);
                }

                if (replacement == null) {
                    replacement = matcher.group(0);
                }

                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }

            matcher.appendTail(result);
            string = result.toString();

        } while (!string.equals(previousResult));

        return escapeDoubleQuotes(string);
    }

    /**
     * Method to escape double quotes
     *
     * @param string string
     * @return the string with double quotes escaped
     */
    private static String escapeDoubleQuotes(String string) {
        return string.replace("\"", "\\\"");
    }
}
