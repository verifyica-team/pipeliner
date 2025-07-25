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

package org.verifyica.pipeliner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Utility class to execute external processes and handle their output.
 */
public class ProcessExecutor {

    /**
     * Constructor
     */
    private ProcessExecutor() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Execute the given arguments in a separate process and prints its output.
     *
     * @param environmentVariables the environment variables to set for the process
     * @param workingDirectory the working directory for the process
     * @param arguments the command arguments to execute
     * @param consumer a consumer to process each line of output
     * @return the exit code of the process
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public static int execute(
            Map<String, String> environmentVariables,
            Path workingDirectory,
            List<String> arguments,
            Consumer<String> consumer)
            throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(arguments);

        builder.environment().putAll(environmentVariables);
        builder.directory(workingDirectory.toFile());
        builder.redirectErrorStream(true);

        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                consumer.accept(line);
            }
        }

        return process.waitFor();
    }
}
