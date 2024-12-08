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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.MessageSupplier;
import org.verifyica.pipeliner.common.Validator;
import org.verifyica.pipeliner.common.ValidatorException;
import org.verifyica.pipeliner.common.Version;
import org.verifyica.pipeliner.execution.ExecutableFactory;
import org.verifyica.pipeliner.execution.ExecutablePipeline;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Class to implement CLI */
public class Pipeliner implements Runnable {

    private static final String PIPELINER_TIMESTAMPS = "PIPELINER_TIMESTAMPS";

    private static final String PIPELINER_TRACE = "PIPELINER_TRACE";

    private static final String PIPELINER_LOG = "PIPELINER_LOG";

    private static final String PIPELINER_MINIMAL = "PIPELINER_MINIMAL";

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

    // Deprecated options

    @Option(names = "--suppress-timestamps", description = "DEPRECATED")
    private Boolean suppressTimestamps;

    /** Constructor */
    public Pipeliner() {
        validator = new Validator();
        files = new ArrayList<>();
    }

    @Override
    public void run() {
        Console console = Console.getInstance();

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
                console.error(
                        "message=[option [--suppress-timestamps] has been deprecated. Timestamps are disabled by default] exit-code=[1]");
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
                    console.error("message=[command line " + e.getMessage() + "] exit-code=[1]");
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
                    console.error("message=[command line " + e.getMessage() + "] exit-code=[1]");
                    console.closeAndExit(1);
                }

                for (Map.Entry<String, String> entry : new LinkedHashSet<>(commandLineProperties.entrySet())) {
                    commandLineProperties.put("INPUT_" + entry.getKey(), entry.getValue());
                }
            }

            // Validate filename arguments

            if (filenames == null || filenames.isEmpty()) {
                console.error("message=[no filename(s) provided] exit-code=[1]");
                console.closeAndExit(1);
            }

            try {
                for (String filename : filenames) {
                    if (filename.trim().isEmpty()) {
                        console.error("message=[no filename(s) provided] exit-code=[1]");
                        console.closeAndExit(1);
                    }

                    console.log("@info filename=[%s]", filename);
                    File file = new File(filename);

                    validator.isValidFile(
                            file, MessageSupplier.of("file either doesn't exit, not a file, or not accessible"));

                    files.add(file);
                }
            } catch (ValidatorException e) {
                console.error("message=[" + e.getMessage() + "] exit-code=[1]");
                console.closeAndExit(1);
            }

            try {
                int exitCode = 0;
                ExecutableFactory executableFactory = new ExecutableFactory();

                for (File file : files) {
                    ExecutablePipeline executablePipeline = executableFactory.create(
                            file.getAbsolutePath(), commandLineEnvironmentVariables, commandLineProperties);
                    executablePipeline.execute();
                    exitCode = executablePipeline.getExitCode();
                    if (exitCode != 0) {
                        break;
                    }
                }

                console.closeAndExit(exitCode);
            } catch (Throwable t) {
                console.error("message=[%s] exit-code=[%d]", t.getMessage(), 1);

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

    /**
     * Main method
     *
     * @param args args
     */
    public static void main(String[] args) {
        new CommandLine(new Pipeliner()).execute(args);
    }
}
