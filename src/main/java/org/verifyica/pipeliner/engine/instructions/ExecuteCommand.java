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

package org.verifyica.pipeliner.engine.instructions;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.Verbosity;
import org.verifyica.pipeliner.engine.Context;
import org.verifyica.pipeliner.engine.Instruction;
import org.verifyica.pipeliner.engine.instructions.support.Shell;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.support.Ipc;
import org.verifyica.pipeliner.support.PeekIterator;

/**
 * Instruction to execute a command using the context's current frame.
 */
public class ExecuteCommand implements Instruction {

    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteCommand.class);

    /**
     * Pattern to capture commands with the --capture operator and a variable.
     */
    private static final Pattern CAPTURE_PATTERN = Pattern.compile("^--capture\\s*\\$\\{\\{\\s*(\\w+)\\s*}}\\s+(.+)$");

    /**
     * Pattern to capture commands with the --capture:append operator and a variable.
     */
    private static final Pattern CAPTURE_APPEND_PATTERN =
            Pattern.compile("^--capture:append\\s*\\$\\{\\{\\s*(\\w+)\\s*}}\\s+(.+)$");

    /**
     * The instruction line.
     */
    private final String line;

    /**
     * Constructor
     *
     * @param line the instruction line
     */
    private ExecuteCommand(String line) {
        this.line = line.trim();
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("line [%s]", line);

        String workingCommand = line;
        String captureOperator = null;
        String captureVariable = null;

        // Check if the command starts with "--capture" and a variable
        Matcher captureMatcher = CAPTURE_PATTERN.matcher(line);
        if (captureMatcher.matches()) {
            captureOperator = "--capture";
            captureVariable = captureMatcher.group(1);
            workingCommand = captureMatcher.group(2);
        } else {
            // Check if the command starts with the "--capture:append" operator and a variable
            captureMatcher = CAPTURE_APPEND_PATTERN.matcher(line);
            if (captureMatcher.matches()) {
                captureOperator = "--capture:append";
                captureVariable = captureMatcher.group(1);
                workingCommand = captureMatcher.group(2);
            }
        }

        LOGGER.trace("captureOperator [%s]", captureOperator);
        LOGGER.trace("captureVariable [%s]", captureVariable);
        LOGGER.trace("workingCommand [%s]", workingCommand);

        // Resolve variables in the command
        String resolvedCommand = context.resolveVariables(workingCommand);

        // Execute the command
        execute(context, peekIterator, line, resolvedCommand, captureOperator, captureVariable);
    }

    /**
     * Executes the command in the context's current frame.
     *
     * @param context the context
     * @param peekIterator the iterator for instructions
     * @param command the command
     * @param resolveCommand the resolved command
     * @param captureOperator the capture operator ("--capture" or "--capture:append"), or null if not capturing
     * @param captureVariable the capture variable name, or null if not capturing
     * @throws Throwable if an error occurs during execution
     */
    private void execute(
            Context context,
            PeekIterator<Instruction> peekIterator,
            String command,
            String resolveCommand,
            String captureOperator,
            String captureVariable)
            throws Throwable {
        // Get the verbosity level
        Verbosity verbosity = context.getConsole().getVerbosity();

        // IPC file for input (pipeliner to command)
        File ipcInFile = null;

        // IPC file for output (command to pipeliner)
        File ipcOutFile = null;

        try {
            // If the verbosity is normal
            if (verbosity.isNormal()) {
                // Print the command
                context.getConsole().println("@command %s", command);
            }

            // Get the shell
            String shell = context.getShell();

            // Get the working directory
            String workingDirectory = context.getWorkingDirectory();

            // Get the timeout minutes
            int timeoutMinutes = Integer.parseInt(context.getTimeoutMinutes());

            // Create a copy of the environment variables
            Map<String, String> environmentVariables = new HashMap<>(context.getEnvironmentVariables());

            // Create the IPC in file (pipeliner to command)
            ipcInFile = Ipc.createFile(Constants.PIPELINER_IPC_IN_FILE_PREFIX);

            // Create the IPC out file (command to pipeliner)
            ipcOutFile = Ipc.createFile(Constants.PIPELINER_IPC_OUT_FILE_PREFIX);

            // Set the IPC environment variable
            environmentVariables.put(Constants.PIPELINER_IPC_IN, ipcInFile.getAbsolutePath());

            // Set the IPC environment variable
            environmentVariables.put(Constants.PIPELINER_IPC_OUT, ipcOutFile.getAbsolutePath());

            // Write the variables to the IPC in file (pipeliner to command)
            Ipc.write(ipcInFile, context.getVariables());

            // Build the command parts for the ProcessBuilder
            String[] commandParts = Shell.getProcessBuilderCommandArguments(Shell.decode(shell), resolveCommand);

            // Create a ProcessBuilder with the command parts
            ProcessBuilder processBuilder = new ProcessBuilder(commandParts);

            // Set the working directory
            processBuilder.directory(new File(workingDirectory));

            // Redirect error stream to the output stream
            processBuilder.redirectErrorStream(true);

            // Set the environment variables for the process
            processBuilder.environment().putAll(environmentVariables);

            // Start the process
            Process process = processBuilder.start();

            // Create a StringBuilder to capture the output if capturing
            StringBuilder captureBuilder = new StringBuilder();

            // While reading the process output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;

                // Read each line of the process output
                while ((line = reader.readLine()) != null) {
                    // If capturing
                    if (captureOperator != null) {
                        // If the capture builder is not empty
                        if (captureBuilder.length() > 0) {
                            // Append a new line before the next line
                            captureBuilder.append(System.lineSeparator());
                        }

                        // Append the line to the capture builder
                        captureBuilder.append(line);
                    } else {
                        // Not capturing, so just print the line

                        // Print the line to the context's print stream
                        context.getConsole().println("@output %s", line);
                    }
                }
            }

            /*
            if (captureOperator == null && verbosity.isNormal()) {
                // If there was no output and verbosity is normal, print a message
                context.getConsole().println("@output ((no output))");
            }
            */

            // Wait for the process to finish, with a timeout
            boolean finished = process.waitFor(timeoutMinutes, TimeUnit.MINUTES);

            // If the process did finish within the timeout
            if (!finished) {
                // If the process did not finish within the timeout, destroy it forcibly
                process.destroyForcibly();

                // Throw a runtime exception indicating the timeout
                throw new RuntimeException(format("process timed out after [%d] minute(s)", timeoutMinutes));
            }

            // If capturing output
            if (captureOperator != null) {
                // Get the captured output as a string and remove any trailing newlines
                String capturedOutput = captureBuilder.toString().replaceAll("[\r\n]+$", "");

                // Escape any carriage returns and newlines in the captured output
                capturedOutput = capturedOutput.replace("\r", "\\r").replace("\n", "\\n");

                // If the capture operator is "--capture", set the variable to the captured output
                if ("--capture".equals(captureOperator)) {
                    // Set the variable
                    SetVariable.of(captureVariable, capturedOutput).execute(context, peekIterator);
                } else if ("--capture:append".equals(captureOperator)) {
                    // The capture operator is "--capture:append"

                    // Get the existing value of the variable, or an empty string if not set
                    String value = context.getVariables().getOrDefault(captureVariable, "");

                    // Append the captured output to the existing value
                    value += capturedOutput;

                    // Set the variable with the appended value
                    SetVariable.of(captureVariable, value).execute(context, peekIterator);
                }
            }

            // Get the exit code of the process
            int exitCode = process.exitValue();

            // If the exit code is zero
            if (exitCode == 0) {
                // Read the variables from the IPC out file
                Map<String, String> ipcVariables = Ipc.read(ipcOutFile);

                // Set the variables in the context
                context.getVariables().putAll(ipcVariables);
            }

            // If the exit code is not zero
            if (exitCode != 0) {
                // If the process exited with a non-zero exit value, throw an exception
                throw new RuntimeException(format("exit code [%d]", process.exitValue()));
            }

        } finally {
            // Delete the IPC out file (pipeliner to command)
            Ipc.delete(ipcOutFile);

            // Delete the IPC in file (command to pipeliner)
            Ipc.delete(ipcInFile);
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + " { line [" + line + "] }";
    }

    /**
     * Factory method to create a new Run instruction.
     *
     * @param command the command
     * @return a new Run instance
     */
    public static ExecuteCommand of(String command) {
        return new ExecuteCommand(command);
    }
}
