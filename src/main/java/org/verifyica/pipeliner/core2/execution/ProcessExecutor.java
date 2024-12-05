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

package org.verifyica.pipeliner.core2.execution;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.io.NoOpPrintStream;
import org.verifyica.pipeliner.common.io.StringPrintStream;

public class ProcessExecutor {

    private final Map<String, String> environmentVariables;
    private final String workingDirectory;
    private final Shell shell;
    private final String command;
    private final boolean captureOutput;
    private String output;
    private int exitCode;

    public ProcessExecutor(
            Map<String, String> environmentVariables,
            String workingDirectory,
            Shell shell,
            String command,
            boolean captureOutput) {
        this.environmentVariables = environmentVariables;
        this.workingDirectory = workingDirectory;
        this.shell = shell;
        this.command = command;
        this.captureOutput = captureOutput;
    }

    public void execute(Console console) {
        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.environment().putAll(environmentVariables);
        processBuilder.directory(new File(workingDirectory));
        processBuilder.command(Shell.toCommandTokens(shell, command));
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            StringBuilder outputStringBuilder = new StringBuilder();
            PrintStream capturingPrintStream;

            if (captureOutput) {
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
                        }
                        capturingPrintStream.print(token);

                        if (!captureOutput) {
                            console.log("> %s", token);
                        }

                        appendCRLF = true;
                    }
                }
            }

            capturingPrintStream.close();

            if (captureOutput) {
                output = outputStringBuilder.toString();
            }

            exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.out);
            exitCode = 1;
        }
    }

    public boolean getCaptureOutput() {
        return captureOutput;
    }

    public String getOutput() {
        return output;
    }

    public int getExitCode() {
        return exitCode;
    }
}
