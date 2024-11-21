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

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.verifyica.pipeliner.model.Job;
import org.verifyica.pipeliner.model.Pipeline;
import org.verifyica.pipeliner.model.PipelineFactory;
import org.verifyica.pipeliner.model.Run;
import org.verifyica.pipeliner.model.Step;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import picocli.CommandLine;

/** Class to implement Converter */
public class Converter implements Runnable {

    @CommandLine.Option(names = "--version", description = "show version")
    private boolean showVersion;

    @CommandLine.Option(names = "--reverse", description = "reverse conversion")
    private boolean reverse;

    @CommandLine.Parameters(description = "arguments")
    private List<String> args;

    /** Constructor */
    private Converter() {
        // INTENTIONALLY BLANK
    }

    public void run() {
        if (showVersion) {
            System.out.println("@info Verifyica Converter " + Version.getVersion());
            System.out.println("@info https://github.com/verifyica-team/pipeliner");
            System.out.flush();

            System.exit(0);
        }

        if (args == null || args.size() != 1) {
            System.exit(1);
        }

        try {
            File file = new File(args.get(0));
            String absoluteFilename = file.getAbsolutePath();

            if (!file.exists()) {
                throw new IllegalArgumentException(format("filename [%s] doesn't exist", absoluteFilename));
            }

            if (!file.isFile()) {
                throw new IllegalArgumentException(format("filename [%s] is a directory", absoluteFilename));
            }

            if (!file.canRead()) {
                throw new IllegalArgumentException(format("filename [%s] is not readable", absoluteFilename));
            }

            if (reverse) {
                reverseConvert(file);
            } else {
                convert(file);
            }
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            System.exit(1);
        }
    }

    private void reverseConvert(File file) throws MarkedYAMLException, IOException {
        String pipelinerWorkingDirectory = file.getAbsoluteFile().getParent();

        Console console = new Console();
        Pipeline pipeline = new PipelineFactory(console).createPipeline(file.getAbsolutePath());

        System.out.println("# pipeline name=[" + pipeline.getName() + "]");

        if (!pipeline.isEnabled()) {
            System.out.println("# pipeline name=[" + pipeline.getName() + "] enabled=[false]");
            return;
        }

        for (Job job : pipeline.getJobs()) {
            System.out.println("# job name=[" + job.getName() + "]");

            if (!job.isEnabled()) {
                System.out.println("# job name=[" + job.getName() + "] enabled=[false]");
            } else {
                for (Step step : job.getSteps()) {
                    System.out.println("# step name=[" + step.getName() + "]");

                    if (!step.isEnabled()) {
                        System.out.println("# step name=[" + step.getName() + "] enabled=[false]");
                    } else {
                        if (".".equals(step.getWorkingDirectory())) {
                            System.out.println("cd " + pipelinerWorkingDirectory);
                        } else {
                            if (step.getWorkingDirectory().startsWith("/")) {
                                System.out.println("cd " + step.getWorkingDirectory());
                            } else {
                                System.out.println("cd " + pipelinerWorkingDirectory + File.separator
                                        + step.getWorkingDirectory());
                            }
                        }

                        for (Run run : step.getRuns()) {
                            String command = run.getCommand();
                            command = command.replaceAll(
                                    Pattern.quote("$PIPELINER_HOME"), System.getenv("PIPELINER_HOME"));
                            System.out.println(command);
                        }

                        System.out.println("cd " + pipelinerWorkingDirectory);
                    }
                }
            }
        }
    }

    /**
     * Method to convert a file to a pipeline
     *
     * @param file file
     * @throws IOException IOException
     */
    private void convert(File file) throws IOException {
        int jobIndex = 1;
        int stepIndex = 1;
        List<String> workingDirectories = new ArrayList<>();

        log("pipeline:");
        log(2, "name: pipeline-" + toPipelineName(file.getName()));
        log(2, "id: pipeline-" + toPipelineId(file.getName()));
        log(2, "enabled: true");
        log(2, "jobs:");
        log(4, "- name: pipeline-job-" + jobIndex);
        log(4, "  id: pipeline-job-" + jobIndex);
        log(4, "  enabled: true");
        log(4, "  steps:");

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
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
     */
    public static void main(String[] args) {
        new CommandLine(new Converter()).execute(args);
    }
}
