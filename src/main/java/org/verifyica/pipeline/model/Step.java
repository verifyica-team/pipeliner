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

package org.verifyica.pipeline.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.verifyica.pipeline.common.Timestamp;

public class Step {

    private String id;
    private List<Property> property;
    private boolean enabled;
    private String directory;
    private String command;
    private int exitCode;

    public Step() {
        initialize();
    }

    private void initialize() {
        id = UUID.randomUUID().toString();
        enabled = true;
        directory = ".";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setProperty(List<Property> property) {
        this.property = new ArrayList<>(new LinkedHashSet<>(property));
    }

    public List<Property> getProperty() {
        if (property == null) {
            return new ArrayList<>();
        } else {
            return property.stream()
                    .filter(property -> {
                        String name = property.getName();
                        return name != null && !name.trim().isEmpty();
                    })
                    .collect(Collectors.toList());
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setDirectory(String Directory) {
        this.directory = Directory;
    }

    public String getDirectory() {
        return directory;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void execute(Pipeline pipeline, Job job, PrintStream outPrintStream, PrintStream errorPrintStream) {
        Properties properties = mergeProperties(pipeline.getProperty(), job.getProperty(), getProperty());

        outPrintStream.println(Timestamp.now() + " $ " + replace(getCommand(), properties));

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", replace(getCommand(), properties));
        processBuilder.directory(new File(replace(getDirectory(), properties)));

        try {
            Process process = processBuilder.start();

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
        return "Step {" + "id='"
                + id + '\'' + ", directory='"
                + directory + '\'' + ", command='"
                + command + '\'' + '}';
    }

    private static Properties mergeProperties(
            List<Property> pipelineProperties, List<Property> jobProperties, List<Property> stepProperties) {
        Properties properties = new Properties();

        pipelineProperties.forEach(property -> properties.setProperty(property.getName(), property.getValue()));
        jobProperties.forEach(property -> properties.setProperty(property.getName(), property.getValue()));
        stepProperties.forEach(property -> properties.setProperty(property.getName(), property.getValue()));

        return properties;
    }

    private static String replace(String string, Properties properties) {
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
                    replacement = properties.getProperty(variableName);
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

    private static String escapeDoubleQuotes(String string) {
        return string.replace("\"", "\\\"");
    }
}
