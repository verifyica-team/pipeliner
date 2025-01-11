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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.Environment;
import org.verifyica.pipeliner.execution.Context;
import org.verifyica.pipeliner.execution.Pipeline;
import org.verifyica.pipeliner.execution.PipelineFactory;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.model.EnvironmentVariableName;
import org.verifyica.pipeliner.model.PipelineDefinitionException;
import org.verifyica.pipeliner.model.PropertyName;
import org.verifyica.pipeliner.tokenizer.Tokenizer;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Class to implement Pipeliner */
@SuppressWarnings("PMD.EmptyCatchBlock")
@CommandLine.Command(name = "pipeliner")
public class Pipeliner implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pipeliner.class);

    private static final String PIPELINER_PROPERTIES = "/pipeliner.properties";

    private static final String VERSION_KEY = "version";

    private static final String VERSION_UNKNOWN = "unknown";

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

    @Parameters(description = "filenames")
    private List<String> argumentFilenames;

    @Option(
            names = {"--env", "-E"},
            description = "specify environment variables in key=value format",
            split = ",")
    private final Map<String, String> commandLineEnvironmentVariables = new HashMap<>();

    @Option(
            names = {"--with", "-P"},
            description = "specify properties in key=value format",
            split = ",")
    private final Map<String, String> commandLineProperties = new HashMap<>();

    @Option(names = "--with-file", description = "specify property files", split = ",")
    private final List<String> commandLinePropertiesFiles = new ArrayList<>();

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display this help message.")
    private boolean helpRequested;

    private final List<File> files;

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
        String environmentVariable = Environment.getenv(PIPELINER_TIMESTAMPS);
        if (environmentVariable != null) {
            optionTimestamps = Constants.TRUE.equals(environmentVariable.trim())
                    || Constants.ONE.equals(environmentVariable.trim());
            console.enableTimestamps(optionTimestamps);
        }

        // Enable minimal output if the option is set
        console.enableMinimal(optionMinimal);

        // Enable minimal output if the environment variable is set
        environmentVariable = Environment.getenv(PIPELINER_MINIMAL);
        if (environmentVariable != null) {
            optionMinimal = Constants.TRUE.equals(environmentVariable.trim())
                    || Constants.ONE.equals(environmentVariable.trim());
            console.enableMinimal(optionMinimal);
        }

        // Enable trace logging if the option is set
        console.enableTrace(optionTrace);

        // Enable trace logging if the environment variable is set
        environmentVariable = Environment.getenv(Constants.PIPELINER_TRACE);
        if (environmentVariable != null) {
            optionTrace = Constants.TRUE.equals(environmentVariable.trim())
                    || Constants.ONE.equals(environmentVariable.trim());
            console.enableTrace(optionTrace);
        }

        // Enable minimal output if trace logging is enabled
        if (console.isTraceEnabled()) {
            console.enableMinimal(false);
        }

        try {
            // Display information if requested
            if (optionInformation) {
                console.info(
                        "@info Verifyica Pipeliner " + getVersion() + " (https://github.com/verifyica-team/pipeliner)");
                console.closeAndExit(0);
            }

            // Display version if requested
            if (optionVersion) {
                System.out.print(getVersion());
                console.closeAndExit(0);
            }

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Pipeliner version [%s]", getVersion());
                LOGGER.trace("trace [%s]", optionMinimal);
                LOGGER.trace("minimal [%s]", optionMinimal);
            }

            // Display info header
            console.info(
                    "@info Verifyica Pipeliner " + getVersion() + " (https://github.com/verifyica-team/pipeliner)");

            // Validate command line properties files

            for (String commandLinePropertiesFile : commandLinePropertiesFiles) {
                Path filePath = Paths.get(commandLinePropertiesFile);

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("properties file [%s]", commandLinePropertiesFile);
                }

                if (!Files.exists(filePath)) {
                    console.error("properties file=[%s] doesn't exist", commandLinePropertiesFile);
                    console.closeAndExit(1);
                }

                if (!Files.isReadable(filePath)) {
                    console.error("properties file=[%s] isn't accessible", commandLinePropertiesFile);
                    console.closeAndExit(1);
                }

                if (!Files.isRegularFile(filePath)) {
                    console.error("properties file=[%s] isn't a file", commandLinePropertiesFile);
                    console.closeAndExit(1);
                }
            }

            // Validate command line environment variables

            for (String commandLineEnvironmentVariable : commandLineEnvironmentVariables.keySet()) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("environment variable [%s]", commandLineEnvironmentVariable);
                }

                if (EnvironmentVariableName.isInvalid(commandLineEnvironmentVariable)) {
                    console.error("option -E [%s] is an invalid environment variable", commandLineEnvironmentVariable);
                    console.closeAndExit(1);
                }
            }

            // Validate command line properties

            for (String commandLineProperty : commandLineProperties.keySet()) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("property [%s]", commandLineProperty);
                }

                if (PropertyName.isInvalid(commandLineProperty)) {
                    console.error("option -P [%s] is an invalid property", commandLineProperty);
                    console.closeAndExit(1);
                }
            }

            // Validate filename arguments

            if (argumentFilenames == null || argumentFilenames.isEmpty()) {
                console.error("no filename(s) provided");
                console.closeAndExit(1);
            }

            // Validate file arguments

            for (String filename : argumentFilenames) {
                if (filename.trim().isEmpty()) {
                    console.error("filename is blank");
                    console.closeAndExit(1);
                }

                File file = new File(filename);

                if (!file.exists()) {
                    console.error("file [%s] doesn't exist", filename);
                    console.closeAndExit(1);
                }

                if (!file.canRead()) {
                    console.error("file [%s] isn't accessible", filename);
                    console.closeAndExit(1);
                }

                if (!file.isFile()) {
                    console.error("file [%s] isn't a file", filename);
                    console.closeAndExit(1);
                }

                files.add(file);
            }

            // Load properties from properties files and command line property options

            Map<String, String> properties = new TreeMap<>();

            for (String commandLinePropertiesFile : commandLinePropertiesFiles) {
                try {
                    console.info("@info --with-file [%s]", commandLinePropertiesFile);

                    Properties fileProperties = new Properties();

                    fileProperties.load(
                            new File(commandLinePropertiesFile).toURI().toURL().openStream());

                    fileProperties.forEach((key, value) -> {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("file property [%s] value [%s]", key, value);
                        }

                        // Validate the property name
                        if (PropertyName.isInvalid(key.toString())) {
                            console.error("file property=[%s] is an invalid property", key);
                            console.closeAndExit(1);
                        }

                        try {
                            // Tokenize the value to validate the syntax
                            Tokenizer.validate(value.toString());
                        } catch (Throwable t) {
                            console.error("file property=[%s] value=[%s] has syntax error", key, value.toString());
                            console.closeAndExit(1);
                        }

                        properties.put(key.toString(), value.toString());
                    });
                } catch (Throwable t) {
                    if (console.isTraceEnabled()) {
                        t.printStackTrace(System.out);
                    }

                    console.error("failed to load properties from file [%s]", commandLinePropertiesFile);
                    console.closeAndExit(1);
                }
            }

            // Store the command line properties
            properties.putAll(commandLineProperties);

            int exitCode = 0;

            // Create a pipeline factory
            PipelineFactory pipelineFactory = new PipelineFactory();

            for (File file : files) {
                // Validate the pipeline file if the option is set
                if (!optionValidate) {
                    console.info("@info filename [%s]", file.getName());
                }

                // Create a pipeline
                Pipeline pipeline =
                        pipelineFactory.create(file.getAbsolutePath(), commandLineEnvironmentVariables, properties);

                // Show the basic validation result if the option is set
                if (optionValidate) {
                    console.info("@info filename [%s] passes basic pipeline validation", file.getName());
                } else {
                    pipeline.execute(new Context(console));
                }

                // Get the exit code
                exitCode = pipeline.getExitCode();

                // Exit if the exit code is not 0
                if (exitCode != 0) {
                    break;
                }
            }

            console.closeAndExit(exitCode);
        } catch (PipelineDefinitionException e) {
            if (console.isTraceEnabled()) {
                e.printStackTrace(System.out);
            }

            console.error("%s", e.getMessage());
            console.closeAndExit(1);
        } catch (Throwable t) {
            t.printStackTrace(System.out);

            console.closeAndExit(1);
        }
    }

    /**
     * Method to get the version
     *
     * @return the version
     */
    public static String getVersion() {
        String value = VERSION_UNKNOWN;

        // Load the version from the properties file
        try (InputStream inputStream = Pipeliner.class.getResourceAsStream(PIPELINER_PROPERTIES)) {
            if (inputStream != null) {
                try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                    Properties properties = new Properties();
                    properties.load(inputStreamReader);
                    value = properties.getProperty(VERSION_KEY).trim();
                }
            }
        } catch (IOException e) {
            // INTENTIONALLY BLANK
        }

        return value;
    }

    /**
     * Main method
     *
     * @param args the args
     */
    public static void main(String[] args) {
        System.exit(new CommandLine(new Pipeliner()).execute(args));
    }

    /** Debug class */
    private static class Debug {

        private static final String PWD = "PWD";

        private static final String PIPELINER = "/pipeliner";

        /**
         * Main method
         *
         * @param args ignored
         */
        public static void main(String[] args) {
            // SystemEnvironment.getenv().forEach((s, s2) -> System.out.printf("[%s] = [%s]%n", s, s2));

            if (Environment.getenv(Constants.PIPELINER_HOME) == null) {
                Environment.setenv(Constants.PIPELINER_HOME, Environment.getenv(PWD));
            }

            if (Environment.getenv(Constants.PIPELINER) == null) {
                Environment.setenv(Constants.PIPELINER, Environment.getenv(Constants.PIPELINER_HOME) + PIPELINER);
            }

            // Lock the environment
            Environment.lock();

            // Set the arguments to run
            String[] arguments = new String[] {"tests/all.yaml"};

            Pipeliner.main(arguments);
        }
    }
}
