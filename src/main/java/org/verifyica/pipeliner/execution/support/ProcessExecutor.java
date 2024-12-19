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
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.io.NoOpPrintStream;
import org.verifyica.pipeliner.common.io.StringPrintStream;

/** Class to implement ProcessExecutor */
public class ProcessExecutor {

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
     * @param console console
     * @param environmentVariables environmentVariables
     * @param workingDirectory workingDirectory
     * @param shell shell
     * @param commandLine commandLine
     * @param captureType captureType
     */
    public ProcessExecutor(
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

    /**
     * Method to execute
     *
     * @param timeoutMinutes timeoutMinutes
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    public void execute(int timeoutMinutes) throws IOException, InterruptedException {
        if (timeoutMinutes < 1 || timeoutMinutes == Integer.MAX_VALUE) {
            run();
            return;
        }

        final ProcessExecutor processExecutor = this;
        final AtomicReference<Throwable> throwableReference = new AtomicReference<>();

        try {
            Awaitility.await().atMost(timeoutMinutes, TimeUnit.MINUTES).until(() -> {
                try {
                    processExecutor.run();
                    return true;
                } catch (Throwable t) {
                    setExitCode(1);
                    throwableReference.set(t);
                    return false;
                }
            });

            Throwable throwable = throwableReference.get();

            if (throwable != null) {
                if (throwable instanceof InterruptedException) {
                    throw (InterruptedException) throwable;
                } else if (throwable instanceof IOException) {
                    throw (IOException) throwable;
                } else {
                    throw new RuntimeException(throwable);
                }
            }
        } catch (ConditionTimeoutException e) {
            process.destroyForcibly();
            setExitCode(1);

            String suffix = timeoutMinutes > 1 ? "s" : "";
            throw new InterruptedException(
                    format("step terminated due to timeout of [%s] minute%s", timeoutMinutes, suffix));
        }
    }

    /**
     * Method to set the exit code
     *
     * @param exitCode exitCode
     */
    private void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Method to get the exit code
     *
     * @return the exit code
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Method to get the output
     *
     * @return the output
     */
    public String getProcessOutput() {
        return output;
    }

    /**
     * Method to run
     *
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    private void run() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.environment().putAll(environmentVariables);
        processBuilder.directory(new File(workingDirectory));
        processBuilder.command(Shell.toCommandTokens(shell, commandLine));
        processBuilder.redirectErrorStream(true);

        process = processBuilder.start();

        StringBuilder outputStringBuilder = new StringBuilder();
        PrintStream capturingPrintStream;

        if (captureType != CaptureType.NONE) {
            capturingPrintStream = new StringPrintStream(outputStringBuilder);
        } else {
            capturingPrintStream = new NoOpPrintStream();
        }

        String line;
        String[] tokens;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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
