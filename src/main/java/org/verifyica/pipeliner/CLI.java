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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.verifyica.pipeliner.model.Pipeline;
import org.verifyica.pipeliner.model.PipelineFactory;
import org.verifyica.pipeliner.yaml.YamlFormatException;
import org.verifyica.pipeliner.yaml.YamlValueException;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Class to implement CLI */
public class CLI implements Runnable {

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

    @Parameters(description = "arguments")
    private List<String> args;

    private List<Pipeline> pipelines;

    private int exitCode;

    // Deprecated options

    @Option(names = "--suppress-timestamps", description = "DEPRECATED")
    private Boolean suppressTimestamps;

    /** Constructor */
    public CLI() {
        this.console = new Console();
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
                console.log("@info Verifyica Pipeliner " + Version.getVersion());
                console.log("@info https://github.com/verifyica-team/pipeliner");
                console.log(
                        "@error option [--suppress-timestamps] has been deprecated. Timestamps are disabled by default");
                console.close();

                System.exit(1);
            }

            if (showVersion) {
                console.log("@info Verifyica Pipeliner " + Version.getVersion());
                console.log("@info https://github.com/verifyica-team/pipeliner");
                console.close();

                System.exit(0);
            }

            try {
                console.log("@info Verifyica Pipeliner " + Version.getVersion());
                console.log("@info https://github.com/verifyica-team/pipeliner");

                if (args == null) {
                    exitCode = 1;
                    console.log("@error message=[%s] exit-code[%d]", "No pipeline file argument(s) provided", exitCode);
                } else {
                    loadPipelines();
                    runPipelines();
                }

                console.close();

                System.exit(exitCode);
            } catch (YamlValueException | YamlFormatException | IllegalArgumentException e) {
                console.log("@error message=[%s] exit-code[%d]", e.getMessage(), 1);

                if (trace != null && trace) {
                    e.printStackTrace(System.out);
                }

                console.close();

                System.exit(1);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.exit(1);
        }
    }

    /**
     * Method to load pipelines
     */
    private void loadPipelines() {
        PipelineFactory pipelineFactory = new PipelineFactory(console);

        for (String filename : args) {
            String absoluteFilename = new File(filename).getAbsolutePath();

            console.log("@info filename[%s]", absoluteFilename);

            File file = new File(filename.trim());

            if (!file.exists()) {
                throw new IllegalArgumentException(format("filename[%s] doesn't exist", absoluteFilename));
            }

            if (!file.isFile()) {
                throw new IllegalArgumentException(format("filename[%s] is a directory", absoluteFilename));
            }

            if (!file.canRead()) {
                throw new IllegalArgumentException(format("filename[%s] is not readable", absoluteFilename));
            }

            pipelines.add(pipelineFactory.createPipeline(absoluteFilename));
        }
    }

    /**
     * Method to run pipelines
     */
    private void runPipelines() {
        for (Pipeline pipeline : pipelines) {
            new Runner(console, pipeline).run();

            if (pipeline.getExitCode() != 0) {
                exitCode = pipeline.getExitCode();
                break;
            }
        }
    }

    /**
     * Main method
     *
     * @param args args
     */
    public static void main(String[] args) {
        new CommandLine(new CLI()).execute(args);
    }
}
