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

package org.verifyica.pipeliner.core.command;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.Environment;
import org.verifyica.pipeliner.common.QuotedStringTokenizer;
import org.verifyica.pipeliner.common.ShutdownHooks;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.Job;
import org.verifyica.pipeliner.core.Pipeline;
import org.verifyica.pipeliner.core.Shell;
import org.verifyica.pipeliner.core.Step;
import org.verifyica.pipeliner.core.support.ExtensionManager;
import org.verifyica.pipeliner.core.support.Ipc;
import org.verifyica.pipeliner.core.support.Resolver;
import org.verifyica.pipeliner.core.support.UnresolvedException;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.parser.SyntaxException;

/** Class to implement ExtensionCommand */
public class ExtensionCommand implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionCommand.class);

    private final Pipeline pipeline;
    private final String pipelineId;
    private final Job job;
    private final String jobId;
    private final Step step;
    private final String stepId;
    private final String command;

    /**
     * Constructor
     *
     * @param step the step
     * @param command the command
     */
    public ExtensionCommand(Step step, String command) {
        this.pipeline = step.getParent(Job.class).getParent(Pipeline.class);
        this.pipelineId = pipeline.getId();
        this.job = step.getParent(Job.class);
        this.jobId = job.getId();
        this.step = step;
        this.stepId = step.getId();
        this.command = command;
    }

    /**
     * Method to execute the command
     *
     * @param context the context
     * @return the exit code
     */
    public int execute(Context context) {
        int exitCode;
        File ipcOutFile = null;
        File ipcInFile = null;

        // Get the console
        Console console = context.getConsole();

        try {
            // Emit the command
            console.emit("$ %s", command);

            // Resolve variables
            Map<String, String> variables = Resolver.resolveVariables(context.getVariables());

            // Create the environment variables
            Map<String, String> environmentVariables = new TreeMap<>(Environment.getenv());

            // Resolve environment variables
            environmentVariables.putAll(
                    Resolver.resolveEnvironmentVariables(context.getEnvironmentVariables(), variables));

            // Resolve the working directory
            File workingDirectory = resolveWorkingDirectory(environmentVariables, variables);

            // Validate the working directory exists
            if (!workingDirectory.exists()) {
                throw new IllegalStateException(format("working-directory=[%s] doesn't exit", workingDirectory));
            }

            // Validate the working directory is accessible
            if (!workingDirectory.canRead()) {
                throw new IllegalStateException(format("working-directory=[%s] can't be read", workingDirectory));
            }

            // Validate the working directory is a directory
            if (!workingDirectory.isDirectory()) {
                throw new IllegalStateException(format("working-directory=[%s] isn't a directory", workingDirectory));
            }

            // Resolve the command
            String resolvedCommand =
                    Resolver.resolveAllVariables(context.getEnvironmentVariables(), context.getVariables(), command);

            // Parse the command
            List<String> tokens = QuotedStringTokenizer.tokenize(resolvedCommand);

            // Validate the number of tokens
            if (tokens.size() < 2 || tokens.size() > 3) {
                throw new SyntaxException(format("invalid extension command [%s]", command));
            }

            // Get the URL
            String url = tokens.get(1);

            // Get the checksum
            String checksum = tokens.size() == 3 ? tokens.get(2) : null;

            // Create the IPC in file
            ipcInFile = Ipc.createFile(Constants.PIPELINER_IPC_IN_FILE_PREFIX);

            // Create the IPC out file
            ipcOutFile = Ipc.createFile(Constants.PIPELINER_IPC_OUT_FILE_PREFIX);

            // Set the IPC environment variable
            environmentVariables.put(Constants.PIPELINER_IPC_IN, ipcInFile.getAbsolutePath());

            // Set the IPC environment variable
            environmentVariables.put(Constants.PIPELINER_IPC_OUT, ipcOutFile.getAbsolutePath());

            // Write the variables to the IPC in file
            Ipc.write(ipcInFile, variables);

            // Get the extension manager
            ExtensionManager extensionManager = context.getExtensionManager();

            // Get the shell script from the extension manager
            Path shellScript =
                    extensionManager.getShellScript(environmentVariables, variables, workingDirectory, url, checksum);

            // Reset the working directory to the parent directory of the shell script
            workingDirectory = shellScript.getParent().toFile();

            // Get the list of process builder command arguments
            String[] processCommands = Shell.getProcessBuilderCommandArguments(Shell.BASH, shellScript.toString());

            if (LOGGER.isTraceEnabled()) {
                environmentVariables.forEach(
                        (name, value) -> LOGGER.trace("environment variable [%s] = [%s]", name, value));

                variables.forEach((name, value) -> LOGGER.trace("variable [%s] = [%s]", name, value));

                LOGGER.trace("working directory [%s]", workingDirectory);
                LOGGER.trace("resolved command [%s]", resolvedCommand);
                LOGGER.trace("url [%s]", url);
                LOGGER.trace("checksum [%s]", checksum);
                LOGGER.trace("shell script [%s]", shellScript);
                LOGGER.trace(
                        "process command arguments %s",
                        String.join(
                                        " ",
                                        Arrays.stream(processCommands)
                                                .map(part -> "[" + part + "]")
                                                .toArray(String[]::new))
                                .trim());
            }

            // Create a process builder
            ProcessBuilder processBuilder = new ProcessBuilder(processCommands);

            // Set working directory
            processBuilder.directory(workingDirectory);

            // Set environment variables
            processBuilder.environment().putAll(environmentVariables);

            // Redirect stderr to stdout
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Read and print each line of combined output
            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Emit the output
                    console.emit("> " + line);
                }
            }

            // Get the exit code
            exitCode = process.waitFor();

            LOGGER.trace("exit code [%d]", exitCode);

            if (exitCode == 0) {
                // Read the variables from the IPC out file
                Ipc.read(ipcOutFile).forEach((name, value) -> {
                    LOGGER.trace("ipc in variable [%s] -> [%s]", name, value);

                    // Add the unscoped variable
                    context.getVariables().put(name, value);

                    if (stepId != null) {
                        // Add the scoped variable
                        context.getVariables().put(stepId + Constants.SCOPE_SEPARATOR + name, value);

                        if (jobId != null) {
                            // Add the scoped variable
                            context.getVariables()
                                    .put(
                                            jobId
                                                    + Constants.SCOPE_SEPARATOR
                                                    + stepId
                                                    + Constants.SCOPE_SEPARATOR
                                                    + name,
                                            value);

                            if (pipelineId != null) {
                                // Add the scoped variable
                                context.getVariables()
                                        .put(
                                                pipelineId
                                                        + Constants.SCOPE_SEPARATOR
                                                        + jobId
                                                        + Constants.SCOPE_SEPARATOR
                                                        + stepId
                                                        + Constants.SCOPE_SEPARATOR
                                                        + name,
                                                value);
                            }
                        }
                    }
                });
            }

        } catch (Throwable t) {
            // Emit the error
            console.emit("@error %s -> %s", step, t.getMessage());

            if (LOGGER.isTraceEnabled()) {
                t.printStackTrace(System.err);
            }

            // Set the exit code to 1 to indicate an error
            exitCode = 1;
        } finally {
            if (ShutdownHooks.areEnabled()) {
                // Proactively cleanup the IPC in file
                Ipc.cleanup(ipcInFile);

                // Proactively cleanup the IPC out file
                Ipc.cleanup(ipcOutFile);
            }
        }

        return exitCode;
    }

    /**
     * Method to resolve the working directory
     *
     * @param environmentVariables the environment variables
     * @param variables the variables
     * @return the working directory
     * @throws SyntaxException If a syntax error occurs
     */
    private File resolveWorkingDirectory(Map<String, String> environmentVariables, Map<String, String> variables)
            throws SyntaxException, UnresolvedException {
        String workingDirectory = step.getWorkingDirectory();
        if (workingDirectory == null) {
            workingDirectory = job.getWorkingDirectory();
            if (workingDirectory == null) {
                workingDirectory = pipeline.getWorkingDirectory();
            }
        }

        if (workingDirectory == null) {
            workingDirectory = Constants.DEFAULT_WORKING_DIRECTORY;

            return new File(workingDirectory).getAbsoluteFile();
        }

        String resolvedWorkingDirectory =
                Resolver.resolveAllVariables(environmentVariables, variables, workingDirectory);

        return new File(resolvedWorkingDirectory).getAbsoluteFile();
    }
}
