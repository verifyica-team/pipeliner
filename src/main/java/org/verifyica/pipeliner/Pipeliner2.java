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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.MessageSupplier;
import org.verifyica.pipeliner.common.Validator;
import org.verifyica.pipeliner.common.ValidatorException;
import org.verifyica.pipeliner.common.Version;
import org.verifyica.pipeliner.core2.execution.ExecutableFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Class to implement CLI */
public class Pipeliner2 implements Runnable {

    private static final String PIPELINER_TIMESTAMPS = "PIPELINER_TIMESTAMPS";

    private static final String PIPELINER_TRACE = "PIPELINER_TRACE";

    private static final String PIPELINER_LOG = "PIPELINER_LOG";

    private static final String PIPELINER_MINIMAL = "PIPELINER_MINIMAL";

    private final Console console;

    @Option(names = "--version", description = "show version")
    private boolean showVersion;

    @Option(names = "--timestamps", description = "enable timestamps")
    private boolean timestamps;

    @Option(names = "--trace", description = "enable trace logging")
    private boolean trace;

    @Option(names = "--log", description = "enable file logging")
    private boolean log;

    @Option(names = "--minimal", description = "enable minimal output")
    private boolean minimal;

    @Parameters(description = "filenames")
    private List<String> filenames;

    @Option(names = "-E", description = "specify environment variables in key=value format", split = ",")
    private Map<String, String> commandLineEnvironmentVariables = new HashMap<>();

    @Option(names = "-P", description = "specify property variables in key=value format", split = ",")
    private Map<String, String> commandLineProperties = new HashMap<>();

    private Validator validator;
    private List<File> files;
    // private List<Pipeline> pipelines;

    // Deprecated options

    @Option(names = "--suppress-timestamps", description = "DEPRECATED")
    private Boolean suppressTimestamps;

    /** Constructor */
    public Pipeliner2() {
        validator = new Validator();
        console = new Console();
        files = new ArrayList<>();
        // pipelines = new ArrayList<>();
    }

    @Override
    public void run() {
        if (timestamps) {
            console.enableTimestamps(timestamps);
        } else {
            String environmentVariable = System.getenv(PIPELINER_TIMESTAMPS);
            if (environmentVariable != null) {
                timestamps = "true".equals(environmentVariable.trim()) || "1".equals(environmentVariable.trim());
                console.enableTimestamps(timestamps);
            }
        }

        if (trace) {
            console.enableTrace(trace);
        } else {
            String environmentVariable = System.getenv(PIPELINER_TRACE);
            if (environmentVariable != null) {
                trace = "true".equals(environmentVariable.trim()) || "1".equals(environmentVariable.trim());
                console.enableTrace(trace);
            }
        }

        if (log) {
            console.enableLogging(log);
        } else {
            String environmentVariable = System.getenv(PIPELINER_LOG);
            if (environmentVariable != null) {
                log = "true".equals(environmentVariable.trim()) || "1".equals(environmentVariable.trim());
                console.enableLogging(log);
            }
        }

        if (minimal) {
            console.enableMinimal(minimal);
        } else {
            String environmentVariable = System.getenv(PIPELINER_MINIMAL);
            if (environmentVariable != null) {
                log = "true".equals(environmentVariable.trim()) || "1".equals(environmentVariable.trim());
                console.enableMinimal(log);
            }
        }

        try {
            console.initialize();

            if (suppressTimestamps != null) {
                console.log("@info Verifyica Pipeliner " + Version.getVersion()
                        + " (https://github.com/verifyica-team/pipeliner)");
                console.error("option [--suppress-timestamps] has been deprecated. Timestamps are disabled by default");
                console.closeAndExit(1);
            }

            if (showVersion) {
                if (minimal) {
                    System.out.print(Version.getVersion());
                } else {
                    console.log("@info Verifyica Pipeliner " + Version.getVersion()
                            + " (https://github.com/verifyica-team/pipeliner)");
                }
                console.closeAndExit(0);
            }

            console.log("@info Verifyica Pipeliner " + Version.getVersion()
                    + " (https://github.com/verifyica-team/pipeliner)");

            // Validate command line environment variables

            if (commandLineEnvironmentVariables != null) {
                try {
                    for (String commandLineEnvironmentVariable : commandLineEnvironmentVariables.keySet()) {
                        validator
                                .notNull(
                                        commandLineEnvironmentVariable,
                                        MessageSupplier.of("environment variable is null"))
                                .notBlank(
                                        commandLineEnvironmentVariable,
                                        MessageSupplier.of("environment variable is blank"))
                                .isValidEnvironmentVariable(
                                        commandLineEnvironmentVariable,
                                        MessageSupplier.of(
                                                "environment variable [%s] is invalid",
                                                commandLineEnvironmentVariable));
                    }
                } catch (ValidatorException e) {
                    console.error("command line " + e.getMessage());
                    console.closeAndExit(1);
                }
            }

            // Validate command line properties

            if (commandLineProperties != null) {
                try {
                    for (String commandLineProperty : commandLineProperties.keySet()) {
                        validator
                                .notNull(commandLineProperty, MessageSupplier.of("property option is null"))
                                .notBlank(commandLineProperty, MessageSupplier.of("property option is blank"))
                                .isValidProperty(
                                        commandLineProperty,
                                        MessageSupplier.of("property option [%s] is invalid", commandLineProperty));
                    }
                } catch (ValidatorException e) {
                    console.error("command line " + e.getMessage());
                    console.closeAndExit(1);
                }

                Map<String, String> temp = new HashMap<>();
                for (String commandLineProperty : commandLineProperties.keySet()) {
                    temp.put(commandLineProperty, commandLineProperties.get(commandLineProperty));
                    temp.put("INPUT_" + commandLineProperty, commandLineProperties.get(commandLineProperty));
                }

                commandLineProperties.clear();
                commandLineProperties.putAll(temp);
            }

            // Validate filename arguments

            if (filenames == null || filenames.isEmpty()) {
                console.error("no filename(s) provided");
                console.closeAndExit(1);
            }

            try {
                for (String filename : filenames) {
                    if (filename.trim().isEmpty()) {
                        console.error("no filename(s) provided");
                        console.closeAndExit(1);
                    }

                    console.log("@info filename=[%s]", filename);
                    File file = new File(filename);

                    validator.isValidFile(
                            file, MessageSupplier.of("file either doesn't exit, not a file, or not accessible"));

                    files.add(file);
                }
            } catch (ValidatorException e) {
                console.error(e.getMessage());
                console.closeAndExit(1);
            }

            try {
                ExecutableFactory executableFactory = new ExecutableFactory();

                for (File file : files) {
                    executableFactory.create(file.getAbsolutePath()).execute(console);
                }

                console.closeAndExit(0);
            } catch (Throwable t) {
                console.error("error [%s] exit-code=[%d]", t.getMessage(), 1);

                if (trace) {
                    t.printStackTrace(System.out);
                }

                console.closeAndExit(1);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            console.closeAndExit(1);
        }
    }

    /*
    private void xexecute(Pipeline pipeline) {
        Stopwatch pipelineStopwatch = new Stopwatch();
        Stopwatch jobStopwatch = new Stopwatch();
        Stopwatch stepStopwatch = new Stopwatch();

        console.log(pipeline);

        int exitCode = 0;

        Iterator<Job> jobIterator = pipeline.getJobs().iterator();
        while (jobIterator.hasNext()) {
            jobStopwatch.reset();

            Job job = jobIterator.next();

            console.log(job);

            Iterator<Step> stepIterator = job.getSteps().iterator();

            while (stepIterator.hasNext()) {
                stepStopwatch.reset();

                Step step = stepIterator.next();

                Map<String, String> mergedWith = new TreeMap<>();

                mergeWithPrefix(pipeline.getWith(), pipeline.getId() + ".", mergedWith);
                mergeWithPrefix(job.getWith(), job.getId() + ".", mergedWith);
                mergeWithPrefix(step.getWith(), step.getId() + ".", mergedWith);

                mergedWith.putAll(pipeline.getWith());
                mergedWith.putAll(job.getWith());
                mergedWith.putAll(step.getWith());

                mergeWithPrefix(pipeline.getWith(), "INPUT_", mergedWith);
                mergeWithPrefix(job.getWith(), "INPUT_", mergedWith);
                mergeWithPrefix(step.getWith(), "INPUT_", mergedWith);

                Map<String, String> mergedEnv = new TreeMap<>();

                mergedEnv.putAll(System.getenv());
                mergedEnv.putAll(pipeline.getEnv());
                mergedEnv.putAll(job.getEnv());
                mergedEnv.putAll(step.getEnv());

                mergedEnv.put("PIPELINER_VERSION", Version.getVersion());
                mergedEnv.put("INPUT_PIPELINER_VERSION", Version.getVersion());

                Map<String, String> mergedOpt = new TreeMap<>();

                mergedOpt.putAll(pipeline.getOpt());
                mergedOpt.putAll(job.getOpt());
                mergedOpt.putAll(step.getOpt());

                String PROPERTY_MATCHING_REGEX = "(?<!\\\\)\\$\\{\\{\\s*([a-zA-Z0-9_\\-.]+)\\s*\\}\\}";

                String workingDirectory = step.getWorkingDirectory();
                String mergedWorkingDirectory =
                        RecursiveReplacer.replace(mergedWith, PROPERTY_MATCHING_REGEX, workingDirectory);

                mergedEnv.forEach((name, value) -> console.trace("%s env [%s] = [%s]", step, name, value));
                mergedWith.forEach((name, value) -> console.trace("%s with [%s] = [%s]", step, name, value));
                mergedOpt.forEach((name, value) -> console.trace("%s opt [%s] = [%s]", step, name, value));

                console.trace("%s working directory [%s]", step, mergedWorkingDirectory);

                String run = step.getRun();

                List<String> commands = mergeLines(Arrays.asList(run.split("\\R")));

                Iterator<String> commandsIterator = commands.iterator();
                while (commandsIterator.hasNext()) {
                    Shell shell = Shell.decode(step.getShell());
                    String command = commandsIterator.next();
                    String mergedCommand = RecursiveReplacer.replace(mergedWith, PROPERTY_MATCHING_REGEX, command);

                    console.trace("%s shell [%s]", step, shell);

                    console.log(step);

                    if ("mask".equals(mergedOpt.get("properties"))) {
                        console.log("$ %s", command);
                    } else {
                        console.log("$ %s", mergedCommand);
                    }

                    ProcessExecutor processExecutor =
                            new ProcessExecutor(mergedEnv, workingDirectory, shell, mergedCommand, false);
                    processExecutor.execute(console);
                    exitCode = processExecutor.getExitCode();

                    if (exitCode != 0) {
                        break;
                    }
                }

                while (stepIterator.hasNext()) {
                    Step step2 = stepIterator.next();
                    console.log("%s", step2);
                }
            }

            console.log(
                    "%s exit-code=[%d] ms=[%d]",
                    job, exitCode, jobStopwatch.elapsedTime().toMillis());

            if (exitCode != 0) {
                break;
            }
        }

        while (jobIterator.hasNext()) {
            Job job = jobIterator.next();
            console.log("%s", job);
        }

        console.log(
                "%s exit-code=[%d] ms=[%d]",
                pipeline, exitCode, pipelineStopwatch.elapsedTime().toMillis());
    }

    private static void mergeWithPrefix(Map<String, String> source, String prefix, Map<String, String> target) {
        if (source != null && prefix != null) {
            for (Map.Entry<String, String> entry : source.entrySet()) {
                target.put(prefix + entry.getKey(), entry.getValue());
            }
        }
    }

    private static List<String> mergeLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String str : lines) {
            if (str.endsWith(" \\")) {
                current.append(str.substring(0, str.length() - 2));
            } else {
                if (current.length() > 0) {
                    current.append(" ");
                    current.append(str.trim());
                    result.add(current.toString().trim());
                    current.setLength(0);
                } else {
                    result.add(str);
                }
            }
        }

        if (current.length() > 0) {
            result.add(current.toString());
        }

        return result;
    }
    */

    /**
     * Main method
     *
     * @param args args
     */
    public static void main(String[] args) {
        new CommandLine(new Pipeliner2()).execute(args);
    }
}
