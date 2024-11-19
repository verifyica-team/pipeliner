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

package org.verifyica.pipeliner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Class to implement Converter */
public class Converter {

    /** Constructor */
    private Converter() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to convert a file to a pipeline
     *
     * @param filename filename
     * @throws IOException IOException
     */
    private void convert(String filename) throws IOException {
        int jobIndex = 1;
        int stepIndex = 1;
        List<String> workingDirectories = new ArrayList<>();

        File file = new File(filename);

        log("pipeline:");
        log(2, "name: pipeline-" + toPipelineName(file.getName()));
        log(2, "id: pipeline-" + toPipelineId(file.getName()));
        log(2, "enabled: true");
        log(2, "jobs:");
        log(4, "- name: pipeline-job-" + jobIndex);
        log(4, "  id: pipeline-job-" + jobIndex);
        log(4, "  enabled: true");
        log(4, "  steps:");

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }

                line = line.trim();
                if (!line.startsWith("#")) {
                    line = line.trim();

                    if (line.startsWith("cd ")) {
                        workingDirectories.add(line.substring(3));
                    } else {
                        log(8, "- name: pipeline-job-" + jobIndex + "-step-" + stepIndex);
                        log(8, "  id: pipeline-job-" + jobIndex + "-step-" + stepIndex);
                        log(8, "  enabled: true");

                        String workingDirectory = flatten(workingDirectories);
                        if (!workingDirectory.equals(".")) {
                            log(8, "  working-directory: " + workingDirectory);
                        }

                        log(8, "  run: " + line);

                        workingDirectories.clear();
                        stepIndex++;
                    }
                }
            }
        }
    }

    /**
     * Method to flatten a list of working directories to a string
     *
     * @param workingDirectories workingDirectories
     * @return list of working directories flattened to a string
     */
    private String flatten(List<String> workingDirectories) {
        StringBuilder stringBuilder = new StringBuilder();

        Iterator<String> iterator = workingDirectories.iterator();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next());

            if (iterator.hasNext()) {
                stringBuilder.append(File.separator);
            }
        }

        String workingDirectory = stringBuilder.toString();
        return !workingDirectory.isEmpty() ? workingDirectory : ".";
    }

    /**
     * Method to log a message
     *
     * @param spaces spaces
     * @param object object
     */
    private void log(int spaces, Object object) {
        for (int i = 0; i < spaces; i++) {
            System.out.print(" ");
        }
        log(object);
    }

    /**
     * Method to log a message
     *
     * @param object object
     */
    private void log(Object object) {
        System.out.println(object);
    }

    /**
     * Method to convert a string to a pipeline name
     *
     * @param string string
     * @return a string converted to a pipeline name
     */
    private static String toPipelineName(String string) {
        if (string == null) {
            return null;
        }
        return string.replaceAll("[^A-Za-z0-9-.]", "-");
    }

    /**
     * Method to convert a string to a pipeline id
     *
     * @param string string
     * @return a string converted to a pipeline id
     */
    private static String toPipelineId(String string) {
        if (string == null) {
            return null;
        }
        return string.replaceAll("[^A-Za-z0-9-]", "-");
    }

    /**
     * Main method
     *
     * @param args args
     * @throws IOException IOException
     */
    public static void main(String[] args) throws IOException {
        if (args == null || args.length != 1) {
            System.exit(1);
        }

        if ("".equals(args[0].trim())) {
            System.exit(1);
        }

        new Converter().convert(args[0].trim());
    }
}
