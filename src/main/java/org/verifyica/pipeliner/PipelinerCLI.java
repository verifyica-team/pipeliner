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
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Class to implement Pipeliner */
@CommandLine.Command(name = "pipeliner")
public class PipelinerCLI implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PipelinerCLI.class);

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

    @Option(
            names = {"--minimal", "--min"},
            description = "enable minimal output")
    private boolean optionMinimal;

    @Option(
            names = {"--extra-minimal", "--extra-min"},
            description = "enable extra minimal output")
    private boolean optionExtraMinimal;

    @Option(names = "--trace", description = "enable tracing")
    private boolean optionTrace;

    @Option(
            names = {"--validate", "--val"},
            description = "validate pipeline file")
    private boolean optionValidate;

    @Option(
            names = {"--env", "-E"},
            description = "specify environment variables in key=value format",
            split = ",")
    private final Map<String, String> environmentVariables = new LinkedHashMap<>();

    @Option(
            names = {"--with", "-P"},
            description = "specify properties in key=value format",
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
    private boolean helpRequested;

    private int exitCode;

    /**
     * Constructor
     */
    public PipelinerCLI() {
        // INTENTIONALLY BLANK
    }

    @Override
    public void run() {
        if (optionVersion) {
            System.out.print(Pipeliner.VERSION);
            return;
        }

        if (optionInformation) {
            System.out.println(Pipeliner.BANNER);
            return;
        }

        if (optionTrace) {
            LoggerFactory.setLevel(Level.TRACE);
        }

        try {
            int exitCode = new Pipeliner()
                    .enableMinimal(optionMinimal)
                    .enableExtraMinimal(optionExtraMinimal)
                    .enableTimestamps(optionTimestamps)
                    .setEnvironmentVariables(environmentVariables)
                    .setVariables(variables)
                    .setVariablesFilenames(variablesFilenames)
                    .setFilenames(filenames)
                    .execute(optionValidate ? Pipeliner.Mode.VALIDATE : Pipeliner.Mode.EXECUTE);

            if (exitCode != 0) {
                exit();
            }
        } catch (Throwable t) {
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
        System.exit(new CommandLine(new PipelinerCLI()).execute(args));
    }
}
