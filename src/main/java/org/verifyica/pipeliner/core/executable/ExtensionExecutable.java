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

package org.verifyica.pipeliner.core.executable;

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
import org.verifyica.pipeliner.core.Shell;
import org.verifyica.pipeliner.core.Step;
import org.verifyica.pipeliner.core.support.ExtensionManager;
import org.verifyica.pipeliner.core.support.Ipc;
import org.verifyica.pipeliner.core.support.Resolver;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.parser.SyntaxException;

/** Class to implement ExtensionExecutable */
public class ExtensionExecutable extends AbstractExecutable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionExecutable.class);

    /**
     * Constructor
     *
     * @param step the step
     * @param commandLine the command line
     */
    public ExtensionExecutable(Step step, String commandLine) {
        super(step, commandLine);
    }

    @Override
    public int execute(Context context) throws Throwable {
        LOGGER.trace("executing %s %s %s command line [%s]", getPipeline(), getJob(), getStep(), getCommandLine());

        int exitCode;
        File ipcOutFile = null;
        File ipcInFile = null;

        // Get the console
        Console console = context.getConsole();

        try {
            // Emit the command
            console.print("$ %s", getCommandLine());

            // Resolve variables
            Map<String, String> variables = Resolver.resolveVariables(context.getVariables());

            // Create the environment variables
            Map<String, String> environmentVariables = new TreeMap<>(Environment.getenv());

            // Resolve environment variables
            environmentVariables.putAll(
                    Resolver.resolveEnvironmentVariables(context.getEnvironmentVariables(), variables));

            // Resolve the working directory
            File workingDirectory = resolveWorkingDirectory(environmentVariables, variables);

            // Resolve variables in the command line
            String resolvedCommandLine = Resolver.resolveAllVariables(
                    context.getEnvironmentVariables(), context.getVariables(), getCommandLine());

            // Parse the command line
            List<String> commandLineTokens = QuotedStringTokenizer.tokenize(resolvedCommandLine);

            // Validate the number of command line tokens
            if (commandLineTokens.size() < 2 || commandLineTokens.size() > 3) {
                throw new SyntaxException(format("invalid extension command [%s]", getCommandLine()));
            }

            // Get the URL
            String url = commandLineTokens.get(1);

            // Get the checksum
            String checksum = commandLineTokens.size() == 3 ? commandLineTokens.get(2) : null;

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
                LOGGER.trace("command [%s]", getCommandLine());
                LOGGER.trace("resolved command [%s]", resolvedCommandLine);
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
                    console.print("> " + line);
                }
            }

            // Get the exit code
            exitCode = process.waitFor();

            LOGGER.trace("exit code [%d]", exitCode);

            if (exitCode == 0) {
                // Read the variables from the IPC out file
                Ipc.read(ipcOutFile).forEach((name, value) -> {
                    LOGGER.trace("IPC in variable [%s] -> [%s]", name, value);

                    // Add the variable with optional scopes
                    context.setVariable(name, value, getPipelineId(), getJobId(), getStepId());
                });
            }

        } catch (Throwable t) {
            // Emit the error
            console.print("@error %s -> %s", getStep(), t.getMessage());

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
}
