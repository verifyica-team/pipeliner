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

    @Option(names = "--timestamps", description = "enable timestamps")
    private Boolean optionTimestamps;

    @Option(
            names = {"--minimal"},
            description = "enable minimal output")
    private Boolean optionMinimal;

    @Option(
            names = {"--extra-minimal"},
            description = "enable extra minimal output")
    private Boolean optionExtraMinimal;

    @Option(names = "--trace", description = "enable tracing")
    private Boolean optionTrace;

    @Option(
            names = {"--env", "-E"},
            description = "specify environment variables in key=value format",
            split = ",")
    private final Map<String, String> environmentVariables = new LinkedHashMap<>();

    @Option(
            names = {"--with", "-V"},
            description = "specify variables in key=value format",
            split = ",")
    private final Map<String, String> variables = new LinkedHashMap<>();

    @Option(names = "--with-file", description = "specify variable files", split = ",")
    private final List<String> variablesFilenames = new ArrayList<>();

    @Parameters(description = "filenames")
    private List<String> filenames;

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display this help message.")
    private boolean optionHelp;

    /** Class to implement ExclusiveOptions */
    static class ExclusiveOptions {

        @Option(
                names = {"--info", "--information"},
                description = "show information")
        private boolean optionInformation;

        @Option(
                names = {"--version"},
                description = "show version")
        private boolean optionVersion;

        @Option(
                names = {
                    "--validate",
                },
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
        Pipeliner.Mode mode = Pipeliner.Mode.EXECUTE;

        if (exclusiveOptions != null) {
            if (exclusiveOptions.optionVersion) {
                mode = Pipeliner.Mode.VERSION;
            } else if (exclusiveOptions.optionInformation) {
                mode = Pipeliner.Mode.INFORMATION;
            } else if (exclusiveOptions.optionValidate) {
                mode = Pipeliner.Mode.VALIDATE;
            }
        }

        if (isTraceEnabled()) {
            LoggerFactory.setLevel(Level.TRACE);
        }

        try {
            int exitCode = new Pipeliner()
                    .enableMinimal(isMinimalEnabled())
                    .enableExtraMinimal(isExtraMinimalEnabled())
                    .enableTimestamps(isTimestampsEnabled())
                    .setEnvironmentVariables(environmentVariables)
                    .setVariables(variables)
                    .setVariablesFilenames(variablesFilenames)
                    .setFilenames(filenames)
                    .setMode(mode)
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
     * Method to get if minimal is enabled
     *
     * @return true if enabled, else false
     */
    private boolean isMinimalEnabled() {
        return optionMinimal != null
                ? optionMinimal
                : Constants.TRUE.equals(Environment.getenv(Constants.PIPELINER_MINIMAL));
    }

    /**
     * Method to get if extra minimal is enabled
     *
     * @return true if enabled, else false
     */
    private boolean isExtraMinimalEnabled() {
        return optionExtraMinimal != null
                ? optionExtraMinimal
                : Constants.TRUE.equals(Environment.getenv(Constants.PIPELINER_EXTRA_MINIMAL));
    }

    /**
     * Method to get if timestamps are enabled
     *
     * @return true if enabled, else false
     */
    private boolean isTimestampsEnabled() {
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
