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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.verifyica.pipeliner.common.Environment;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.EnvironmentVariable;
import org.verifyica.pipeliner.core.Pipeline;
import org.verifyica.pipeliner.core.PipelineDefinitionException;
import org.verifyica.pipeliner.core.PipelineFactory;
import org.verifyica.pipeliner.core.Variable;
import org.verifyica.pipeliner.core.support.Ipc;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.parser.Parser;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Class to implement Pipeliner */
@CommandLine.Command(name = "pipeliner")
public class Pipeliner implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pipeliner.class);

    private static final String PIPELINER_TIMESTAMPS = "PIPELINER_TIMESTAMPS";

    private static final String PIPELINER_MINIMAL = "PIPELINER_MINIMAL";

    @Option(
            names = {"--information", "--info"},
            description = "show information")
    private boolean optionInformation;

    @Option(
            names = {"--version", "--ver"},
            description = "show version")
    private boolean optionVersion;

    @Option(names = "--timestamps", description = "enable timestamps")
    private boolean optionTimestamps;

    @Option(names = "--trace", description = "enable trace logging")
    private boolean optionTrace;

    @Option(
            names = {"--minimal", "--min"},
            description = "enable minimal output")
    private boolean optionMinimal;

    @Option(
            names = {"--validate", "--val"},
            description = "validate pipeline file")
    private boolean optionValidate;

    @Option(
            names = {"--env", "-E"},
            description = "specify environment variables in key=value format",
            split = ",")
    private final Map<String, String> commandLineEnvironmentVariables = new LinkedHashMap<>();

    @Option(
            names = {"--with", "-P"},
            description = "specify properties in key=value format",
            split = ",")
    private final Map<String, String> commandLineProperties = new LinkedHashMap<>();

    @Option(names = "--with-file", description = "specify property files", split = ",")
    private final List<String> commandLinePropertiesFiles = new ArrayList<>();

    @Parameters(description = "filenames")
    private List<String> argumentFilenames;

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display this help message.")
    private boolean helpRequested;

    private final List<File> files;

    private int exitCode;

    /** Constructor */
    public Pipeliner() {
        files = new ArrayList<>();
    }

    @Override
    public void run() {
        // Lock the environment
        Environment.lock();

        // Create a console
        Console console = new Console();

        // Enable timestamps if the option is set
        console.enableTimestamps(optionTimestamps);

        // Enable timestamps if the environment variable is set
        String enableTimestamps = Environment.getenv(PIPELINER_TIMESTAMPS);
        if (enableTimestamps != null) {
            optionTimestamps =
                    Constants.TRUE.equals(enableTimestamps.trim()) || Constants.ONE.equals(enableTimestamps.trim());
            console.enableTimestamps(optionTimestamps);
        }

        // Enable minimal output if the option is set
        console.enableMinimal(optionMinimal);

        // Enable minimal output if the environment variable is set
        String enableMinimal = Environment.getenv(PIPELINER_MINIMAL);
        if (enableMinimal != null) {
            optionMinimal = Constants.TRUE.equals(enableMinimal.trim()) || Constants.ONE.equals(enableMinimal.trim());
            console.enableMinimal(optionMinimal);
        }

        // Enable trace logging if the option is set
        console.enableTrace(optionTrace);

        // Enable trace logging if the environment variable is set
        String enableTrace = Environment.getenv(Constants.PIPELINER_TRACE);
        if (enableTrace != null) {
            optionTrace = Constants.TRUE.equals(enableTrace.trim()) || Constants.ONE.equals(enableTrace.trim());
            console.enableTrace(optionTrace);
        }

        // Enable minimal output if trace logging is enabled
        if (console.isTraceEnabled()) {
            console.enableMinimal(false);
        }

        try {
            // Display information if requested
            if (optionInformation) {
                console.info("@info Verifyica Pipeliner " + Version.getVersion()
                        + " (https://github.com/verifyica-team/pipeliner)");

                return;
            }

            // Display version if requested
            if (optionVersion) {
                System.out.print(Version.getVersion());

                return;
            }

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Pipeliner version [%s]", Version.getVersion());
                LOGGER.trace("trace [%s]", optionMinimal);
                LOGGER.trace("minimal [%s]", optionMinimal);
            }

            if (Environment.getenv(Constants.PIPELINER_IPC_IN) == null) {
                // Display info header
                console.info("@info Verifyica Pipeliner " + Version.getVersion()
                        + " (https://github.com/verifyica-team/pipeliner)");
            }

            // *****************************
            // Environment variable handling
            // *****************************

            // Create a sorted map for environment variables and load the environment variables
            Map<String, String> environmentVariables = new TreeMap<>(Environment.getenv());

            // Load command line environment variables
            for (Map.Entry<String, String> commandLineEnvironmentVariableEntry :
                    commandLineEnvironmentVariables.entrySet()) {
                String name = commandLineEnvironmentVariableEntry.getKey();
                String value = commandLineEnvironmentVariableEntry.getValue();

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("command line environment variable [%s] = [%s]", name, value);
                }

                if (EnvironmentVariable.isInvalid(name)) {
                    console.error("option -E [%s] is an invalid environment variable", name);

                    exit();
                }

                environmentVariables.put(name, value);
            }

            // *******************
            // Properties handling
            // *******************

            Map<String, String> properties = new TreeMap<>();

            // Load PIPELINER_IPC_IN properties if available
            if (Environment.getenv(Constants.PIPELINER_IPC_IN) != null) {
                File pipelinerIpcInFile = new File(Environment.getenv(Constants.PIPELINER_IPC_IN));
                Map<String, String> pipelinerIpcProperties = Ipc.read(pipelinerIpcInFile);
                properties.putAll(pipelinerIpcProperties);
            }

            // Load properties from files first
            for (String commandLinePropertiesFile : commandLinePropertiesFiles) {
                Path filePath = Paths.get(commandLinePropertiesFile);

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("properties file [%s]", commandLinePropertiesFile);
                }

                if (!Files.exists(filePath)) {
                    console.error("properties file=[%s] doesn't exist", commandLinePropertiesFile);

                    exit();
                }

                if (!Files.isReadable(filePath)) {
                    console.error("properties file=[%s] isn't accessible", commandLinePropertiesFile);

                    exit();
                }

                if (!Files.isRegularFile(filePath)) {
                    console.error("properties file=[%s] isn't a file", commandLinePropertiesFile);

                    exit();
                }

                Properties fileProperties = new Properties();

                fileProperties.load(
                        new File(commandLinePropertiesFile).toURI().toURL().openStream());

                fileProperties.forEach((name, value) -> {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("file variable [%s] value [%s]", name, value);
                    }

                    // Validate the property name
                    if (Variable.isInvalid(name.toString())) {
                        console.error("file variable=[%s] is an invalid variable", name);

                        exit();
                    }

                    try {
                        // Tokenize the value to validate the syntax
                        Parser.validate(value.toString());
                    } catch (Throwable t) {
                        console.error("file variable=[%s] value=[%s] has syntax error", name, value.toString());

                        exit();
                    }

                    properties.put(name.toString(), value.toString());
                });
            }

            // Load command line properties second
            for (Map.Entry<String, String> commandLinePropertyEntry : commandLineProperties.entrySet()) {
                String property = commandLinePropertyEntry.getKey();
                String value = commandLinePropertyEntry.getValue();

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("command line variable [%s] = [%s]", property, value);
                }

                if (Variable.isInvalid(property)) {
                    console.error("option -P [%s] is an invalid variable", property);

                    exit();
                }

                properties.put(property, value);
            }

            // **********************
            // File argument handling
            // **********************

            // Validate a least one file was provided
            if (argumentFilenames == null || argumentFilenames.isEmpty()) {
                console.error("no filename(s) provided");

                exit();
            }

            // Process file arguments
            for (String filename : argumentFilenames) {
                if (filename.trim().isEmpty()) {
                    console.error("filename is blank");

                    exit();
                }

                File file = new File(filename);

                if (!file.exists()) {
                    console.error("file [%s] doesn't exist", filename);

                    exit();
                }

                if (!file.canRead()) {
                    console.error("file [%s] isn't accessible", filename);

                    exit();
                }

                if (!file.isFile()) {
                    console.error("file [%s] isn't a file", filename);

                    exit();
                }

                files.add(file);
            }

            // Set the default exit code
            int exitCode = 0;

            // Create a pipeline factory
            PipelineFactory pipelineFactory = new PipelineFactory();

            for (File file : files) {
                // Validate the pipeline file if the option is set
                if (!optionValidate && Environment.getenv(Constants.PIPELINER_IPC_IN) == null) {
                    console.info("@info filename [%s]", file.getName());
                }

                // Create a context
                Context context = new Context(console);

                // Create a pipeline
                Pipeline pipeline = pipelineFactory.create(file.getAbsolutePath());

                // environmentVariables, properties);

                // Show the basic validation result if the option is set
                if (optionValidate) {
                    console.info("@info filename [%s] passes basic pipeline validation", file.getName());
                } else {
                    pipeline.execute(context);
                }

                // Get the exit code
                exitCode = pipeline.execute(context);

                // Exit if the exit code is not 0
                if (exitCode != 0) {
                    break;
                }

                // Write PIPELINER_IPC_OUT properties if available
                if (Environment.getenv(Constants.PIPELINER_IPC_OUT) != null) {
                    File pipelinerIpcInFile = new File(Environment.getenv(Constants.PIPELINER_IPC_OUT));
                    Ipc.write(pipelinerIpcInFile, context.getWith());
                }
            }

            if (exitCode != 0) {
                exit();
            }
        } catch (PipelineDefinitionException e) {
            if (console.isTraceEnabled()) {
                e.printStackTrace(System.out);
            }

            console.error("%s", e.getMessage());

            exit();
        } catch (Throwable t) {
            t.printStackTrace(System.out);

            exit();
        }
    }

    private void exit() {
        System.exit(CommandLine.ExitCode.SOFTWARE);
    }

    /**
     * Main method
     *
     * @param args the args
     */
    public static void main(String[] args) {
        System.exit(new CommandLine(new Pipeliner()).execute(args));
    }
}
