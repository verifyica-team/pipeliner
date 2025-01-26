/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.Enabled;
import org.verifyica.pipeliner.core.EnvironmentVariable;
import org.verifyica.pipeliner.core.Pipeline;
import org.verifyica.pipeliner.core.PipelineFactory;
import org.verifyica.pipeliner.core.support.Ipc;

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
    public enum Mode {
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

    private boolean enableMinimal;
    private boolean enableExtraMinimal;
    private boolean enableTimestamps;
    private final Map<String, String> environmentVariables;
    private final Map<String, String> variables;
    private final List<String> variablesFilenames;
    private final List<String> filenames;

    /**
     * Constructor
     */
    public Pipeliner() {
        environmentVariables = new TreeMap<>();
        variables = new TreeMap<>();
        variablesFilenames = new ArrayList<>();
        filenames = new ArrayList<>();
    }

    /**
     * Method to enable minimal
     *
     * @param enableMinimal enable minimal
     * @return this
     */
    public Pipeliner enableMinimal(boolean enableMinimal) {
        this.enableMinimal = enableMinimal;
        return this;
    }

    /**
     * Method to enable extra minimal
     *
     * @param enableExtraMinimal enable extra minimal
     * @return this
     */
    public Pipeliner enableExtraMinimal(boolean enableExtraMinimal) {
        this.enableExtraMinimal = enableExtraMinimal;
        return this;
    }

    /**
     * Method to enable timestamps
     *
     * @param enableTimestamps enable timestamps
     * @return this
     */
    public Pipeliner enableTimestamps(boolean enableTimestamps) {
        this.enableTimestamps = enableTimestamps;
        return this;
    }

    /**
     * Method to set environment variables
     *
     * @param environmentVariables the environment variables
     * @return this
     */
    public Pipeliner setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables.clear();
        this.environmentVariables.putAll(environmentVariables);
        return this;
    }

    /**
     * Method to set variables
     *
     * @param variables the variables
     * @return this
     */
    public Pipeliner setVariables(Map<String, String> variables) {
        this.variables.clear();
        this.variables.putAll(variables);
        return this;
    }

    /**
     * Method to set variables filenames
     *
     * @param variablesFilenames the variables filenames
     * @return this
     */
    public Pipeliner setVariablesFilenames(List<String> variablesFilenames) {
        this.variablesFilenames.clear();
        this.variablesFilenames.addAll(variablesFilenames);
        return this;
    }

    /**
     * Method to set filenames
     *
     * @param filenames the filenames
     * @return this
     */
    public Pipeliner setFilenames(List<String> filenames) {
        this.filenames.clear();
        this.filenames.addAll(filenames);
        return this;
    }

    /**
     * Method to execute the pipelines
     *
     * @param mode the mode
     * @return the exit code
     * @throws Throwable if an error occurs
     */
    public int execute(Mode mode) throws Throwable {
        int exitCode = 0;

        // Validate environment variables
        validateEnvironmentVariables();

        // Validate variables
        validateVariables();

        // Add environment variables
        Environment.setenvs(environmentVariables);

        // Create a console
        Console console = new Console();

        // Enable minimal
        console.enableMinimal(enableMinimal);

        // Enable extra minimal
        console.enableExtraMinimal(enableExtraMinimal);

        // Enable timestamps
        console.enableTimestamps(enableTimestamps);

        // Create a context
        Context context = new Context(console);

        // Add environment variables
        context.getEnv().putAll(environmentVariables);

        // Check if we have an IPC in file
        File ipcInFile = Environment.getenv(Constants.PIPELINER_IPC_IN) != null
                ? new File(Environment.getenv(Constants.PIPELINER_IPC_IN))
                : null;

        // If we have an IPC in file
        if (ipcInFile != null) {
            // Read the IPC in file
            Map<String, String> ipcInVariables = Ipc.read(ipcInFile);

            // Add the IPC variables
            context.getWith().putAll(ipcInVariables);
        }

        // Add variables
        context.getWith().putAll(variables);

        // Create a pipeline factory
        PipelineFactory pipelineFactory = new PipelineFactory();

        // Create a list to hold the pipelines
        List<Pipeline> pipelines = new ArrayList<>();

        // Check if we are a nested execution
        if (Environment.getenv(Constants.PIPELINER_DISABLE_BANNER) == null) {
            // Emit the banner
            console.emit(BANNER);

            // Disable the banner for nested execution
            Environment.setenv(Constants.PIPELINER_DISABLE_BANNER, "true");
        }

        // Create the pipelines
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

        try {
            // Create the pipelines
            for (String filename : filenames) {
                pipelines.add(pipelineFactory.createPipeline(filename));

                if (mode == Mode.VALIDATE) {
                    console.emit("@info validated [%s]", filename);
                }
            }

            if (mode == Mode.VALIDATE) {
                return 0;
            }
        } catch (Throwable t) {
            console.emit("@error %s", t.getMessage());
            t.printStackTrace(System.out);

            return 1;
        }

        // Count the number of enabled pipelines
        int enabledPipelineCount = 0;

        // Execute the pipelines
        for (Pipeline pipeline : pipelines) {
            if (Boolean.TRUE.equals(Enabled.decode(pipeline.getEnabled()))) {
                // Increment the enabled pipeline count
                enabledPipelineCount++;

                // Execute the pipeline
                exitCode = pipeline.execute(context);

                // If the exit code is not 0, break
                if (exitCode != 0) {
                    break;
                }
            }
        }

        if (enabledPipelineCount == 0) {
            console.emit("@info no enabled pipelines");
            return 0;
        }

        // Check if we have an IPC out file
        File ipcOutFile = Environment.getenv(Constants.PIPELINER_IPC_OUT) != null
                ? new File(Environment.getenv(Constants.PIPELINER_IPC_OUT))
                : null;

        // If we have an IPC out file
        if (ipcOutFile != null) {
            // Write the IPC out variables
            Ipc.write(ipcOutFile, context.getWith());
        }

        return exitCode;
    }

    /**
     * Method to validate environment variables
     */
    private void validateEnvironmentVariables() {
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            if (EnvironmentVariable.isInvalid(entry.getKey())) {
                throw new IllegalArgumentException("invalid environment variable [" + entry.getKey() + "]");
            }
        }
    }

    /**
     * Method to validate variables
     */
    private void validateVariables() {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            if (EnvironmentVariable.isInvalid(entry.getKey())) {
                throw new IllegalArgumentException("invalid variable [" + entry.getKey() + "]");
            }
        }
    }
}
