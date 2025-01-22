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

package org.verifyica.pipeliner.execution.support;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.io.NoOpPrintStream;
import org.verifyica.pipeliner.common.io.StringPrintStream;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement ProcessExecutableCommand */
public class ProcessExecutableCommand implements ExecutableCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExecutableCommand.class);

    private final Console console;
    private final Map<String, String> environmentVariables;
    private final String workingDirectory;
    private final Shell shell;
    private final String commandLine;
    private final CaptureType captureType;
    private String output;
    private int exitCode;

    private Process process;

    /**
     * Constructor
     *
     * @param console the console
     * @param environmentVariables the environment variables
     * @param workingDirectory the working directory
     * @param shell the shell
     * @param commandLine the command line
     * @param captureType the capture type
     */
    public ProcessExecutableCommand(
            Console console,
            Map<String, String> environmentVariables,
            String workingDirectory,
            Shell shell,
            String commandLine,
            CaptureType captureType) {
        this.console = console;
        this.environmentVariables = environmentVariables;
        this.workingDirectory = workingDirectory;
        this.shell = shell;
        this.commandLine = commandLine;
        this.captureType = captureType;
    }

    @Override
    public void execute(int timeoutMinutes) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("executing command ...");
            LOGGER.trace("command line [%s]", commandLine);
            LOGGER.trace("working directory [%s]", workingDirectory);
            LOGGER.trace("timeout minutes [%d]", timeoutMinutes);
        }

        final ProcessExecutableCommand commandExecutor = this;
        final AtomicReference<Throwable> throwableReference = new AtomicReference<>();

        try {
            Awaitility.await().atMost(timeoutMinutes, TimeUnit.MINUTES).until(() -> {
                try {
                    commandExecutor.run();
                } catch (Throwable t) {
                    setExitCode(1);
                    throwableReference.set(t);
                }

                return true;
            });

            Throwable throwable = throwableReference.get();
            if (throwable != null) {
                throw throwable;
            }

            setExitCode(0);
        } catch (ConditionTimeoutException e) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("command [%s] execution timed out", commandLine);
                LOGGER.trace("timeout minutes [%d]", timeoutMinutes);
                LOGGER.trace("kill the process ...");
            }

            setExitCode(1);

            process.destroyForcibly();

            try {
                process.waitFor(10, TimeUnit.SECONDS);
            } catch (InterruptedException e2) {
                Thread.currentThread().interrupt();
                throw new InterruptedException(
                        format("thread interrupted while waiting for command [%s] to terminate", commandLine));
            }

            throw new CommandExecutionException(
                    format("timeout-minutes=[%d] exceeded, terminating command [%s]", timeoutMinutes, commandLine));
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String getProcessOutput() {
        return output;
    }

    /**
     * Method to set the exit code
     *
     * @param exitCode the exit code
     */
    private void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Method to run
     *
     * @throws CommandExecutionException ProcessExecutorException
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException InterruptedException
     */
    private void run() throws CommandExecutionException, IOException, InterruptedException {
        String[] processingBuilderCommandArguments = Shell.getProcessBuilderCommandArguments(shell, commandLine);

        if (LOGGER.isTraceEnabled()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String commandArgument : processingBuilderCommandArguments) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(" ");
                }
                stringBuilder.append("[").append(commandArgument).append("]");
            }

            LOGGER.trace("process command arguments %s", stringBuilder.toString());
        }

        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.environment().putAll(environmentVariables);
        processBuilder.directory(new File(workingDirectory));
        processBuilder.command(processingBuilderCommandArguments);
        processBuilder.redirectErrorStream(true);

        try {
            process = processBuilder.start();
        } catch (IOException e) {
            if (e.getMessage().contains("error=2")) {
                throw new CommandExecutionException(format("command [%s] not found", commandLine));
            } else {
                throw e;
            }
        }

        StringBuilder outputStringBuilder = new StringBuilder();
        PrintStream capturingPrintStream;

        if (captureType != CaptureType.NONE) {
            capturingPrintStream = new StringPrintStream(outputStringBuilder);
        } else {
            capturingPrintStream = new NoOpPrintStream();
        }

        String line;
        String[] tokens;

        try (BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            boolean appendCRLF = false;
            while ((line = bufferedReader.readLine()) != null) {
                tokens = line.split("\\R");
                for (String token : tokens) {
                    if (appendCRLF) {
                        capturingPrintStream.println();
                        capturingPrintStream.flush();
                    }
                    capturingPrintStream.print(token);
                    capturingPrintStream.flush();

                    if (captureType == CaptureType.NONE) {
                        console.info("> %s", token);
                    }

                    appendCRLF = true;
                }
            }
        }

        capturingPrintStream.close();

        if (captureType != CaptureType.NONE) {
            output = outputStringBuilder.toString();
        }

        setExitCode(process.waitFor());
    }
}
