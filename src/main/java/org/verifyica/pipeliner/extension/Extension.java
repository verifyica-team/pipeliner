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

package org.verifyica.pipeliner.extension;

import java.io.*;
import java.util.*;
import org.verifyica.pipeliner.common.Environment;
import org.verifyica.pipeliner.common.Ipc;
import org.verifyica.pipeliner.common.IpcException;

/** Class to implement Extension */
public class Extension {

    private static final String PIPELINER_TRACE = "PIPELINER_TRACE";

    private static final String PIPELINER_IPC_IN = "PIPELINER_IPC_IN";

    private static final String PIPELINER_IPC_OUT = "PIPELINER_IPC_OUT";

    private static final String TRUE = "true";

    /** Constructor */
    private Extension() {
        // INTENTIONALLY BLANK
    }

    /**
     * Run the extension
     *
     * @param args Command line arguments
     * @throws Throwable If an error occurs
     */
    public void run(String[] args) throws Throwable {
        Map<String, String> environmentVariables = getEnvironmentVariables();

        // Read the properties from the input IPC file
        Map<String, String> ipcInProperties = readIpcInProperties();

        if (isTraceEnabled()) {
            for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
                System.out.printf("@trace environment variable [%s] = [%s]%n", entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, String> entry : ipcInProperties.entrySet()) {
                System.out.printf("@trace extension property [%s] = [%s]%n", entry.getKey(), entry.getValue());
            }
        }

        System.out.println("This is a sample Java extension");
        for (Map.Entry<String, String> entry : ipcInProperties.entrySet()) {
            System.out.printf("extension with property [%s] = [%s]%n", entry.getKey(), entry.getValue());
        }

        Map<String, String> ipcOutProperties = new TreeMap<>();

        // Pipeliner will automatically scope the properties if ids (pipeliner, job, step) are available
        ipcOutProperties.put("extension.property.1", "extension.foo");
        ipcOutProperties.put("extension.property.2", "extension.bar");

        // Write the properties to the output IPC file
        writeIpcOutProperties(ipcOutProperties);
    }

    /**
     * Read the IPC properties
     *
     * @return a Map of properties
     * @throws IpcException If an error occurs
     */
    private Map<String, String> readIpcInProperties() throws IpcException {
        String ipcFilenameInput = Environment.getenv().get(PIPELINER_IPC_IN);
        System.out.printf("%s [%s]%n", PIPELINER_IPC_IN, ipcFilenameInput);
        File ipcInputFile = new File(ipcFilenameInput);
        return Ipc.read(ipcInputFile);
    }

    /**
     * Write the IPC properties
     *
     * @param properties properties
     * @throws IpcException If an error occurs
     */
    private void writeIpcOutProperties(Map<String, String> properties) throws IpcException {
        String ipcFilenameOutput = Environment.getenv().get(PIPELINER_IPC_OUT);
        System.out.printf("%s [%s]%n", PIPELINER_IPC_OUT, ipcFilenameOutput);
        File ipcOutputFile = new File(ipcFilenameOutput);
        Ipc.write(ipcOutputFile, properties);
    }

    /**
     * Get environment variables
     *
     * @return Map of environment variables
     */
    private static Map<String, String> getEnvironmentVariables() {
        return new TreeMap<>(Environment.getenv());
    }

    /**
     * Check if trace is enabled
     *
     * @return trude if trace is enabled, else false
     */
    private boolean isTraceEnabled() {
        return TRUE.equals(Environment.getenv(PIPELINER_TRACE));
    }

    /**
     * Main method
     *
     * @param args Command line arguments
     * @throws Throwable If an error occurs
     */
    public static void main(String[] args) throws Throwable {
        new Extension().run(args);
    }
}
