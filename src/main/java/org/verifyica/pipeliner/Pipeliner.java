/*
 * Copyright (C) Pipeliner project authors and contributors
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.Enabled;
import org.verifyica.pipeliner.core.EnvironmentVariable;
import org.verifyica.pipeliner.core.Pipeline;
import org.verifyica.pipeliner.core.PipelineDefinitionException;
import org.verifyica.pipeliner.core.PipelineFactory;
import org.verifyica.pipeliner.core.Variable;
import org.verifyica.pipeliner.core.support.Ipc;
import org.verifyica.pipeliner.parser.SyntaxException;

/** Class to implement Pipeliner */
public class Pipeliner {

    /**
     * The banner
     */
    public static final String BANNER =
            "@info Verifyica Pipeliner " + Version.getVersion() + " (https://github.com/verifyica-team/pipeliner)";

    /**
     * Enum to implement Mode
     */
    public enum ExecutionMode {

        /**
         * Information mode
         */
        EMIT_INFORMATION,

        /**
         * Version mode
         */
        EMIT_VERSION,

        /**
         * Validate mode
         */
        VALIDATE,

        /**
         * Execute mode
         */
        EXECUTE;
    }

    /**
     * The version
     */
    public static final String VERSION = Version.getVersion();

    private ExecutionMode executionMode;
    private final Console console;
    private final Map<String, String> environmentVariables;
    private final Map<String, String> variables;
    private final List<String> variablesFilenames;
    private final List<String> filenames;

    /**
     * Constructor
     */
    public Pipeliner() {
        executionMode = ExecutionMode.EXECUTE;
        console = new Console();
        environmentVariables = new TreeMap<>();
        variables = new TreeMap<>();
        variablesFilenames = new ArrayList<>();
        filenames = new ArrayList<>();
    }

    /**
     * Method to set verbosity
     *
     * @param verbosity verbosity
     * @return this
     */
    public Pipeliner setVerbosity(Console.Verbosity verbosity) {
        if (verbosity != null) {
            console.setVerbosity(verbosity);
        }
        return this;
    }

    /**
     * Method to enable timestamps
     *
     * @param enableTimestamps enable timestamps
     * @return this
     */
    public Pipeliner setEnabledTimestamps(boolean enableTimestamps) {
        console.setEnabledTimestamps(enableTimestamps);
        return this;
    }

    /**
     * Method to set environment variables
     *
     * @param environmentVariables the environment variables
     * @return this
     */
    public Pipeliner setEnvironmentVariables(Map<String, String> environmentVariables) {
        if (environmentVariables != null) {
            this.environmentVariables.clear();
            this.environmentVariables.putAll(environmentVariables);
        }
        return this;
    }

    /**
     * Method to set variables
     *
     * @param variables the variables
     * @return this
     */
    public Pipeliner setVariables(Map<String, String> variables) {
        if (variables != null) {
            this.variables.clear();
            this.variables.putAll(variables);
        }
        return this;
    }

    /**
     * Method to set variables filenames
     *
     * @param variablesFilenames the variables filenames
     * @return this
     */
    public Pipeliner setVariablesFilenames(List<String> variablesFilenames) {
        if (variablesFilenames != null) {
            this.variablesFilenames.clear();
            this.variablesFilenames.addAll(variablesFilenames);
        }
        return this;
    }

    /**
     * Method to set filenames
     *
     * @param filenames the filenames
     * @return this
     */
    public Pipeliner setFilenames(List<String> filenames) {
        if (filenames != null) {
            this.filenames.clear();
            this.filenames.addAll(filenames);
        }
        return this;
    }

    /**
     * Method to set the mode
     *
     * @param executionMode the mode
     * @return this
     */
    public Pipeliner setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
        return this;
    }

    /**
     * Method to execute
     *
     * @return the exit code
     * @throws Throwable if an error occurs
     */
    public int run() throws Throwable {
        switch (executionMode) {
            case EMIT_INFORMATION: {
                System.out.println(BANNER);
                return 0;
            }
            case EMIT_VERSION: {
                System.out.print(VERSION);
                return 0;
            }
            case VALIDATE: {
                return validate();
            }
            case EXECUTE: {
                return execute();
            }
            default: {
                throw new IllegalStateException("unsupported mode [" + executionMode + "]");
            }
        }
    }

    /**
     * Method to validate pipelines
     *
     * @return the exit code
     */
    private int validate() {
        // Emit the banner
        console.emit(BANNER);

        if (filenames.isEmpty()) {
            console.emit("@error no pipeline filenames provided");
            return -1;
        }

        // Validate the filenames
        for (String filename : filenames) {
            File file = new File(filename);

            if (!file.exists()) {
                console.emit("@error file not found [%s]", filename);
                return 1;
            }

            if (!file.canRead()) {
                console.emit("@error file not accessible [%s]", filename);
                return 1;
            }

            if (!file.isFile()) {
                console.emit("@error not a file [%s]", filename);
                return 1;
            }
        }

        // Create a pipeline factory
        PipelineFactory pipelineFactory = new PipelineFactory();

        try {
            // Create the pipelines
            for (String filename : filenames) {
                pipelineFactory.createPipeline(filename);
                console.emit("@info pipeline [%s] is valid", filename);
            }

            return 0;
        } catch (Throwable t) {
            console.emit("@error %s", t.getMessage());
            return 1;
        }
    }

    /**
     * Method to execute pipelines
     *
     * @return the exit code
     * @throws Throwable if an error occurs
     */
    private int execute() throws Throwable {
        // Check if we are a nested execution
        if (Environment.getenv(Constants.PIPELINER_NESTED_EXECUTION) == null) {
            // Emit the banner
            console.emit(BANNER);

            // Disable the banner for nested execution
            Environment.setenv(Constants.PIPELINER_NESTED_EXECUTION, Constants.TRUE);
        } else {
            // Disabled timestamps for nested execution
            console.setEnabledTimestamps(false);
        }

        if (filenames.isEmpty()) {
            console.emit("@warning no enabled pipelines");
            return 0;
        }

        try {
            // Validate environment variables
            validateEnvironmentVariables();
        } catch (SyntaxException e) {
            console.emit("@error %s", e.getMessage());
            return 1;
        }

        for (String filename : variablesFilenames) {
            File file = new File(filename);

            if (!file.exists()) {
                console.emit("@error file not found [%s]", filename);
                return 1;
            }

            if (!file.canRead()) {
                console.emit("@error file not accessible [%s]", filename);
                return 1;
            }

            if (!file.isFile()) {
                console.emit("@error not a file [%s]", filename);
                return 1;
            }

            try {
                Properties properties = new Properties();

                properties.load(new BufferedReader(
                        new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)));

                properties.forEach((key, value) -> {
                    if (key instanceof String && value instanceof String) {
                        if (Variable.isInvalid((String) key)) {
                            console.emit("@error invalid variable [%s]", key);
                        }

                        // Only add the variable if it does not already exist (command line variables take precedence)
                        if (!variables.containsKey((String) key)) {
                            variables.put((String) key, (String) value);
                        }
                    }
                });
            } catch (Throwable t) {
                console.emit("@error %s", t.getMessage());
                return 1;
            }
        }

        try {
            // Validate variables
            validateVariables();
        } catch (SyntaxException e) {
            console.emit("@error %s", e.getMessage());
            return 1;
        }

        // Add environment variables
        Environment.setenvs(environmentVariables);

        // Create a context
        Context context = new Context(console);

        // Add environment variables
        context.getEnvironmentVariables().putAll(environmentVariables);

        // Check if we have an IPC in file
        File ipcInFile = Environment.getenv(Constants.PIPELINER_IPC_IN) != null
                ? new File(Environment.getenv(Constants.PIPELINER_IPC_IN))
                : null;

        // If we have an IPC in file
        if (ipcInFile != null) {
            // Read the IPC in file
            Map<String, String> ipcInVariables = Ipc.read(ipcInFile);

            // Add the IPC variables
            context.getVariables().putAll(ipcInVariables);
        }

        // Add variables
        context.getVariables().putAll(variables);

        // Validate the filenames
        for (String filename : filenames) {
            File file = new File(filename);

            if (!file.exists()) {
                console.emit("@error file not found [%s]", filename);
                return 1;
            }

            if (!file.canRead()) {
                console.emit("@error file not accessible [%s]", filename);
                return 1;
            }

            if (!file.isFile()) {
                console.emit("@error not a file [%s]", filename);
                return 1;
            }
        }

        // Exit code
        int exitCode = 0;

        // Counter of enabled pipelines
        int enabledPipelineCount = 0;

        // Create a pipeline factory
        PipelineFactory pipelineFactory = new PipelineFactory();

        String filename = null;

        try {
            // Iterate over the filenames
            Iterator<String> filenamesIterator = filenames.iterator();

            while (filenamesIterator.hasNext()) {
                // Get the filename
                filename = filenamesIterator.next();

                console.emit("@info filename [%s]", filename);

                // Create the pipeline
                Pipeline pipeline = pipelineFactory.createPipeline(filename);

                if (Boolean.TRUE.equals(Enabled.decode(pipeline.getEnabled()))) {
                    // Increment the enabled pipeline count
                    enabledPipelineCount++;

                    // Execute the pipeline
                    exitCode = pipeline.execute(context);

                    // Check the exit code
                    if (exitCode != 0) {
                        break;
                    }
                }
            }
        } catch (SyntaxException | PipelineDefinitionException e) {
            console.emit("@error filename [%s] -> %s", filename, e.getMessage());
            return 1;
        }

        if (enabledPipelineCount == 0) {
            console.emit("@warning no enabled pipelines");
            return 0;
        }

        if (exitCode == 0) {
            // Check if we have an IPC out file
            File ipcOutFile = Environment.getenv(Constants.PIPELINER_IPC_OUT) != null
                    ? new File(Environment.getenv(Constants.PIPELINER_IPC_OUT))
                    : null;

            // If we have an IPC out file
            if (ipcOutFile != null) {
                // Write the IPC out variables
                Ipc.write(ipcOutFile, context.getVariables());
            }
        }

        return exitCode;
    }

    /**
     * Method to validate environment variables
     */
    private void validateEnvironmentVariables() throws SyntaxException {
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            if (EnvironmentVariable.isInvalid(entry.getKey())) {
                throw new SyntaxException(format("invalid environment variable syntax [%s]", entry.getKey()));
            }
        }
    }

    /**
     * Method to validate variables
     */
    private void validateVariables() throws SyntaxException {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            if (Variable.isInvalid(entry.getKey())) {
                throw new SyntaxException(format("invalid variable syntax [%s]", entry.getKey()));
            }
        }
    }
}
