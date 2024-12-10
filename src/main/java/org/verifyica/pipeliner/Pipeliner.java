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
import org.verifyica.pipeliner.execution.ExecutableContext;
import org.verifyica.pipeliner.execution.ExecutableFactory;
import org.verifyica.pipeliner.execution.ExecutablePipeline;
import org.verifyica.pipeliner.model.parser.YamlDefinitionException;
import org.verifyica.pipeliner.model.support.EnvironmentVariable;
import org.verifyica.pipeliner.model.support.Property;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Class to implement CLI */
public class Pipeliner implements Runnable {

    private static final String PIPELINER_TIMESTAMPS = "PIPELINER_TIMESTAMPS";

    private static final String PIPELINER_TRACE = "PIPELINER_TRACE";

    private static final String PIPELINER_MINIMAL = "PIPELINER_MINIMAL";

    @Option(names = "--version", description = "show version")
    private boolean version;

    @Option(names = "--timestamps", description = "enable timestamps")
    private boolean timestamps;

    @Option(names = "--trace", description = "enable trace logging")
    private boolean trace;

    @Option(names = "--minimal", description = "enable minimal output")
    private boolean minimal;

    @Parameters(description = "filenames")
    private List<String> filenames;

    @Option(names = "-E", description = "specify environment variables in key=value format", split = ",")
    private final Map<String, String> commandLineEnvironmentVariables = new HashMap<>();

    @Option(names = "-P", description = "specify property variables in key=value format", split = ",")
    private final Map<String, String> commandLineProperties = new HashMap<>();

    private final Console console;
    private final List<File> files;

    /** Constructor */
    public Pipeliner() {
        console = Console.getInstance();
        files = new ArrayList<>();
    }

    /**
     * Method to get the Console
     *
     * @return the Console
     */
    private Console getConsole() {
        return console;
    }

    @Override
    public void run() {
        getConsole().enableTimestamps(timestamps);

        String environmentVariable = System.getenv(PIPELINER_TIMESTAMPS);
        if (environmentVariable != null) {
            timestamps = "true".equals(environmentVariable.trim()) || "1".equals(environmentVariable.trim());
            getConsole().enableTimestamps(timestamps);
        }

        getConsole().enableMinimal(minimal);

        environmentVariable = System.getenv(PIPELINER_MINIMAL);
        if (environmentVariable != null) {
            minimal = "true".equals(environmentVariable.trim()) || "1".equals(environmentVariable.trim());
            getConsole().enableMinimal(minimal);
        }

        getConsole().enableTrace(trace);

        environmentVariable = System.getenv(PIPELINER_TRACE);
        if (environmentVariable != null) {
            trace = "true".equals(environmentVariable.trim()) || "1".equals(environmentVariable.trim());
            getConsole().enableTrace(trace);
        }

        if (getConsole().isTraceEnabled()) {
            getConsole().enableMinimal(false);
        }

        try {
            if (version) {
                if (minimal) {
                    System.out.print(Version.getVersion());
                } else {
                    getConsole()
                            .log("@info Verifyica Pipeliner " + Version.getVersion()
                                    + " (https://github.com/verifyica-team/pipeliner)");
                }
                getConsole().closeAndExit(0);
            }

            getConsole()
                    .log("@info Verifyica Pipeliner " + Version.getVersion()
                            + " (https://github.com/verifyica-team/pipeliner)");

            // Validate command line environment variables

            for (String commandLineEnvironmentVariable : commandLineEnvironmentVariables.keySet()) {
                if (!EnvironmentVariable.isValid(commandLineEnvironmentVariable)) {
                    getConsole().error("option [-E=%s] is invalid", commandLineEnvironmentVariable);
                    getConsole().closeAndExit(1);
                }
            }

            // Validate command line properties

            for (String commandLineProperty : commandLineProperties.keySet()) {
                if (!Property.isValid(commandLineProperty)) {
                    getConsole().error("option [-P=%s] is invalid", commandLineProperty);
                    getConsole().closeAndExit(1);
                }
            }

            // Validate filename arguments

            if (filenames == null || filenames.isEmpty()) {
                getConsole().error("no filename(s) provided");
                getConsole().closeAndExit(1);
            }

            for (String filename : filenames) {
                if (filename.trim().isEmpty()) {
                    getConsole().error("no filename(s)");
                    getConsole().closeAndExit(1);
                }

                File file = new File(filename);

                if (!file.exists()) {
                    getConsole().error("filename=[%s] either doesn't exist", filename);
                    getConsole().closeAndExit(1);
                }

                if (!file.canRead()) {
                    getConsole().error("filename=[%s] isn't accessible", filename);
                    getConsole().closeAndExit(1);
                }

                if (!file.isFile()) {
                    getConsole().error("filename=[%s] isn't a file", filename);
                    getConsole().closeAndExit(1);
                }

                files.add(file);
            }

            int exitCode = 0;
            ExecutableFactory executableFactory = new ExecutableFactory();

            for (File file : files) {
                getConsole().log("@info filename=[%s]", file.getName());

                ExecutablePipeline executablePipeline = executableFactory.create(
                        file.getAbsolutePath(), commandLineEnvironmentVariables, commandLineProperties);
                executablePipeline.execute(new ExecutableContext(console));

                exitCode = executablePipeline.getExitCode();
                if (exitCode != 0) {
                    break;
                }
            }

            getConsole().closeAndExit(exitCode);
        } catch (YamlDefinitionException e) {
            getConsole().error("%s", e.getMessage());
            getConsole().closeAndExit(1);
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            getConsole().closeAndExit(1);
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
