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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.verifyica.pipeliner.logger.Level;
import org.verifyica.pipeliner.logger.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Class to implement PipelinerCLI */
@CommandLine.Command(name = "pipeliner")
public class PipelinerCLI implements Runnable {

    @CommandLine.ArgGroup(multiplicity = "0..1", exclusive = true)
    ExclusiveOptions exclusiveOptions;

    /**
     * Option to enable timestamps
     */
    @Option(
            names = {"-ts", "--timestamps"},
            description = "enable timestamps")
    private Boolean optionTimestamps;

    /**
     * Option to enable quiet verbosity level
     */
    @Option(
            names = {"-q", "--quiet"},
            description = "enable quiet output")
    private Boolean optionalQuiet;

    /**
     * Option to enable quieter verbosity level
     */
    @Option(
            names = {"-qq", "--quieter"},
            description = "enable quieter output")
    private Boolean optionQuieter;

    /**
     * Option to enable tracing
     */
    @Option(
            names = {"-t", "--trace"},
            description = "enable tracing")
    private Boolean optionTrace;

    /**
     * Option to set an environment variable
     */
    @Option(
            names = {"-E", "--env"},
            description = "specify environment variables in key=value format",
            split = ",")
    private final Map<String, String> environmentVariables = new LinkedHashMap<>();

    /**
     * Option to set a variable
     */
    @Option(
            names = {"-V", "--with"},
            description = "specify variables in key=value format",
            split = ",")
    private final Map<String, String> variables = new LinkedHashMap<>();

    /**
     * Option to set a variable file
     */
    @Option(names = "--with-file", description = "specify variable files", split = ",")
    private final List<String> variablesFilenames = new ArrayList<>();

    /**
     * Option to emit help
     */
    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "emit this help message.")
    private boolean optionHelp;

    @Parameters(description = "filenames")
    private List<String> filenames;

    /** Class to implement ExclusiveOptions */
    static class ExclusiveOptions {

        /**
         * Option to emit information
         */
        @Option(
                names = {"--info", "--information"},
                description = "emit information")
        private boolean optionInformation;

        /**
         * Option to emit version
         */
        @Option(
                names = {"--version"},
                description = "emit version")
        private boolean optionVersion;

        /**
         * Option to validate (a) pipeline file(s)
         */
        @Option(
                names = {"--validate"},
                description = "validate pipeline file")
        private boolean optionValidate;
    }

    /**
     * Constructor
     */
    public PipelinerCLI() {
        // INTENTIONALLY BLANK
    }

    @Override
    public void run() {
        // Enabled trace if required before executing code that uses logging
        if (isTraceEnabled()) {
            LoggerFactory.setLevel(Level.TRACE);
        }

        try {
            // Create and run Pipeliner
            int exitCode = new Pipeliner()
                    .setVerbosity(getVerbosity())
                    .setEnabledTimestamps(getTimestampsEnabled())
                    .setFilenames(filenames)
                    .setEnvironmentVariables(environmentVariables)
                    .setVariables(variables)
                    .setVariablesFilenames(variablesFilenames)
                    .setExecutionMode(getExecutionMode())
                    .run();

            // Check the exit code
            if (exitCode != 0) {
                exit();
            }
        } catch (Throwable t) {
            t.printStackTrace(System.out);

            exit();
        }
    }

    /**
     * Method to get if trace is enabled
     *
     * @return true if enabled, else false
     */
    private boolean isTraceEnabled() {
        return optionTrace != null ? optionTrace : Constants.TRUE.equals(Environment.getenv(Constants.PIPELINER_TRACE));
    }

    /**
     * Method to get the verbosity level
     *
     * @return the verbosity level
     */
    private Console.Verbosity getVerbosity() {
        // Set the default verbosity level
        Console.Verbosity verbosity = Console.Verbosity.NORMAL;

        if (optionQuieter != null) {
            // Set the verbosity level to quieter
            verbosity = Console.Verbosity.QUIETER;
        } else if (optionalQuiet != null) {
            // Set the verbosity level to quiet
            verbosity = Console.Verbosity.QUIET;
        }

        return verbosity;
    }

    /**
     * Method to get the execution mode
     *
     * @return the execution mode
     */
    private Pipeliner.ExecutionMode getExecutionMode() {
        // Set the default execution mode
        Pipeliner.ExecutionMode executionMode = Pipeliner.ExecutionMode.EXECUTE;

        // If an exclusive option is set, then set the execution mode
        if (exclusiveOptions != null) {
            if (exclusiveOptions.optionVersion) {
                // Set the execution mode to emit version
                executionMode = Pipeliner.ExecutionMode.EMIT_VERSION;
            } else if (exclusiveOptions.optionInformation) {
                // Set the execution mode to emit information
                executionMode = Pipeliner.ExecutionMode.EMIT_INFORMATION;
            } else if (exclusiveOptions.optionValidate) {
                // Set the execution mode to validate
                executionMode = Pipeliner.ExecutionMode.VALIDATE;
            }
        }

        return executionMode;
    }

    /**
     * Method to get if timestamps are enabled
     *
     * @return true if timestamps are enabled, else false
     */
    private boolean getTimestampsEnabled() {
        return optionTimestamps != null
                ? optionTimestamps
                : Constants.TRUE.equals(Environment.getenv(Constants.PIPELINER_TIMESTAMPS));
    }

    /**
     * Method to exit
     */
    private void exit() {
        System.exit(CommandLine.ExitCode.SOFTWARE);
    }

    /**
     * Main method
     *
     * @param args the args
     */
    public static void main(String[] args) {
        System.exit(new CommandLine(new PipelinerCLI()).execute(args));
    }
}
