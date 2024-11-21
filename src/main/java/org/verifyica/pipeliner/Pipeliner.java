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
import org.verifyica.pipeliner.common.Validator;
import org.verifyica.pipeliner.common.ValidatorException;
import org.verifyica.pipeliner.model.Pipeline;
import org.verifyica.pipeliner.model.PipelineFactory;
import org.verifyica.pipeliner.yaml.YamlFormatException;
import org.verifyica.pipeliner.yaml.YamlValueException;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Class to implement CLI */
public class Pipeliner implements Runnable {

    private static final String PIPELINER_TIMESTAMPS = "PIPELINER_TIMESTAMPS";

    private static final String PIPELINER_TRACE = "PIPELINER_TRACE";

    private static final String PIPELINER_LOG = "PIPELINER_LOG";

    private static final String PIPELINER_MINIMAL = "PIPELINER_MINIMAL";

    private final Console console;

    @Option(names = "--version", description = "show version")
    private boolean showVersion;

    @Option(names = "--timestamps", description = "enable timestamps")
    private Boolean timestamps;

    @Option(names = "--trace", description = "enable trace logging")
    private Boolean trace;

    @Option(names = "--log", description = "enable file logging")
    private Boolean log;

    @Option(names = "--minimal", description = "enable minimal output")
    private Boolean minimal;

    @Parameters(description = "filenames")
    private List<String> filenames;

    @CommandLine.Option(names = "-E", description = "specify environment variables in key=value format", split = ",")
    private Map<String, String> commandLineEnvironmentVariables = new HashMap<>();

    @CommandLine.Option(names = "-P", description = "specify property variables in key=value format", split = ",")
    private Map<String, String> commandLineProperties = new HashMap<>();

    private List<File> files;
    private List<Pipeline> pipelines;

    // Deprecated options

    @Option(names = "--suppress-timestamps", description = "DEPRECATED")
    private Boolean suppressTimestamps;

    /** Constructor */
    public Pipeliner() {
        this.console = new Console();
        this.files = new ArrayList<>();
        this.pipelines = new ArrayList<>();
    }

    @Override
    public void run() {
        if (timestamps != null) {
            console.enableTimestamps(timestamps);
        } else {
            String environmentVariable = System.getenv(PIPELINER_TIMESTAMPS);
            if (environmentVariable != null) {
                timestamps = "true".equals(environmentVariable.trim()) || "1".equals(environmentVariable.trim());
                console.enableTimestamps(timestamps);
            }
        }

        if (trace != null) {
            console.enableTrace(trace);
        } else {
            String environmentVariable = System.getenv(PIPELINER_TRACE);
            if (environmentVariable != null) {
                trace = "true".equals(environmentVariable.trim()) || "1".equals(environmentVariable.trim());
                console.enableTrace(trace);
            }
        }

        if (log != null) {
            console.enableLogging(log);
        } else {
            String environmentVariable = System.getenv(PIPELINER_LOG);
            if (environmentVariable != null) {
                log = "true".equals(environmentVariable.trim()) || "1".equals(environmentVariable.trim());
                console.enableLogging(log);
            }
        }

        if (minimal != null) {
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
                console.log("@info Verifyica Pipeliner " + Version.getVersion()
                        + " (https://github.com/verifyica-team/pipeliner)");
                console.closeAndExit(0);
            }

            console.log("@info Verifyica Pipeliner " + Version.getVersion());
            console.log("@info https://github.com/verifyica-team/pipeliner");

            // Validate command line environment variables

            if (commandLineEnvironmentVariables != null) {
                try {
                    for (String commandLineEnvironmentVariable : commandLineEnvironmentVariables.keySet()) {
                        Validator.validateEnvironmentVariable(commandLineEnvironmentVariable);
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
                        Validator.validateProperty(commandLineProperty);
                    }
                } catch (ValidatorException e) {
                    console.error("command line " + e.getMessage());
                    console.closeAndExit(1);
                }

                Map<String, String> temp = new HashMap<>();
                for (String commandLineProperty : commandLineProperties.keySet()) {
                    temp.put("INPUT_" + commandLineProperty, commandLineProperties.get(commandLineProperty));
                }

                commandLineProperties.clear();
                commandLineProperties.putAll(temp);
            }

            // Validate filename arguments

            if (filenames == null) {
                console.error("no filename(s) provided");
                console.closeAndExit(1);
            }

            try {
                for (String filename : filenames) {
                    File file = new File(filename);
                    Validator.validateFile(file);
                    files.add(file);
                }
            } catch (ValidatorException e) {
                console.error(e.getMessage());
                console.closeAndExit(1);
            }

            try {
                PipelineFactory pipelineFactory = new PipelineFactory(console);

                for (File file : files) {
                    Pipeline pipeline = pipelineFactory.createPipeline(file.getAbsolutePath());
                    pipeline.addProperties(commandLineProperties);
                    pipeline.addEnvironmentVariables(commandLineEnvironmentVariables);
                    pipelines.add(pipeline);
                }

                for (Pipeline pipeline : pipelines) {
                    pipeline.execute(console);
                }
            } catch (ValidatorException e) {
                console.error(e.getMessage());
                console.closeAndExit(1);
            } catch (YamlValueException | YamlFormatException | IllegalArgumentException e) {
                console.error("message=[%s] exit-code=[%d]", e.getMessage(), 1);

                if (trace != null && trace) {
                    e.printStackTrace(System.out);
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
