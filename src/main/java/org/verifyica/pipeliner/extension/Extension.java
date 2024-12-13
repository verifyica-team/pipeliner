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
import org.verifyica.pipeliner.common.Ipc;

/** Class to implement Extension */
public class Extension {

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
        String ipcFilename = System.getenv().get("PIPELINER_IPC");
        File ipcFile = new File(ipcFilename);
        Map<String, String> properties = Ipc.receive(ipcFile);

        if (isTraceEnabled()) {
            for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
                System.out.printf("@trace environment variable [%s] = [%s]%n", entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, String> entry : properties.entrySet()) {
                System.out.printf("@trace extension property [%s] = [%s]%n", entry.getKey(), entry.getValue());
            }
        }

        System.out.println("This is a sample Java extension");
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            System.out.printf("extension with property [%s] = [%s]%n", entry.getKey(), entry.getValue());
        }

        properties.clear();
        properties.put("extension.out.property.1", "extension.foo");
        properties.put("extension.out.property.2", "extension.bar");

        Ipc.send(properties, ipcFile);
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

    /**
     * Check if trace is enabled
     *
     * @return trude if trace is enabled, else false
     */
    private boolean isTraceEnabled() {
        return "true".equals(System.getenv("PIPELINER_TRACE"));
    }

    /**
     * Get environment variables
     *
     * @return Map of environment variables
     */
    private static Map<String, String> getEnvironmentVariables() {
        return new TreeMap(System.getenv());
    }
}
