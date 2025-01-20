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

/*
 * This is AI generated code
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.String.format;

/**
 * Class to implement Extension
 */
public class Extension {

    private static final String PIPELINER_TRACE = "PIPELINER_TRACE";
    private static final String PIPELINER_IPC_IN = "PIPELINER_IPC_IN";
    private static final String PIPELINER_IPC_OUT = "PIPELINER_IPC_OUT";

    public void run(String[] args) throws IOException {
        Map<String, String> environmentVariables = getEnvironmentVariables();

        // Read the properties from the input IPC file
        Map<String, String> ipcInVariables = readIpcInVariables();

        if (isTraceEnabled()) {
            for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
                System.out.printf("@trace environment variable [%s] = [%s]%n", entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, String> entry : ipcInVariables.entrySet()) {
                System.out.printf("@trace extension variable [%s] = [%s]%n", entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<String, String> entry : ipcInVariables.entrySet()) {
            System.out.printf("PIPELINER_IPC_IN variable [%s] = [%s]%n", entry.getKey(), entry.getValue());
        }

        System.out.println("This is a sample Java extension");

        Map<String, String> ipcOutVariables = new TreeMap<>();

        // A variable name must match the regular expression `^[a-zA-Z0-9_][a-zA-Z0-9_-]*[a-zA-Z0-9_]$`

        // Example output properties (replace with actual values)
        ipcOutVariables.put("extension_variable_1", "java extension foo");
        ipcOutVariables.put("extension_variable_2", "java extension bar");

        String ipcFilenameOutput = getEnvironmentVariables().get(PIPELINER_IPC_OUT);
        System.out.printf("%s file [%s]%n", PIPELINER_IPC_OUT, ipcFilenameOutput);

        for (Map.Entry<String, String> entry : ipcOutVariables.entrySet()) {
            System.out.printf("PIPELINER_IPC_OUT variable [%s] = [%s]%n", entry.getKey(), entry.getValue());
        }

        // Write the properties to the output IPC file
        writeIpcOutVariables(ipcOutVariables);
    }

    /**
     * Get environment variables
     *
     * @return Map of environment variables
     */
    private static Map<String, String> getEnvironmentVariables() {
        return new TreeMap<>(System.getenv());
    }

    /**
     * Check if trace is enabled
     *
     * @return trude if trace is enabled, else false
     */
    private boolean isTraceEnabled() {
        return "true".equals(System.getenv(PIPELINER_TRACE));
    }

    /**
     * Read the IPC properties
     *
     * @return a Map of properties
     * @throws IOException If an error occurs
     */
    private Map<String, String> readIpcInVariables() throws IOException {
        String ipcFilenameInput = getEnvironmentVariables().get(PIPELINER_IPC_IN);
        System.out.printf("%s file [%s]%n", PIPELINER_IPC_IN, ipcFilenameInput);
        File ipcInputFile = new File(ipcFilenameInput);
        return read(ipcInputFile);
    }

    /**
     * Write the IPC properties
     *
     * @param properties properties
     * @throws IOException If an error occurs
     */
    private void writeIpcOutVariables(Map<String, String> properties) throws IOException {
        String ipcFilenameOutput = getEnvironmentVariables().get(PIPELINER_IPC_OUT);
        File ipcOutputFile = new File(ipcFilenameOutput);
        write(ipcOutputFile, properties);
    }

    /**
     * Read the properties
     *
     * @param ipcFile ipcFile
     * @return map map
     * @throws IOException If an error occurs
     */
    private static Map<String, String> read(File ipcFile) throws IOException {
        Map<String, String> map = new TreeMap<>();
        String line;

        try (BufferedReader reader = Files.newBufferedReader(ipcFile.toPath(), StandardCharsets.UTF_8)) {
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int equalIndex = line.indexOf('=');
                if (equalIndex == -1) {
                    String key = line.trim();
                    map.put(key, "");
                } else {
                    String key = line.substring(0, equalIndex).trim();
                    String value = line.substring(equalIndex + 1);
                    String encodedValue = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
                    map.put(key, encodedValue);
                }
            }
        }

        return map;
    }

    /**
     * Write the properties
     *
     * @param ipcFile ipcFile
     * @param map map
     * @throws IOException If an error occurs
     */
    private static void write(File ipcFile, Map<String, String> map) throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(Files.newOutputStream(ipcFile.toPath()), StandardCharsets.UTF_8))) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                if (key != null && !key.trim().isEmpty()) {
                    String value = entry.getValue();
                    String encodedValue;
                    if (value == null) {
                        encodedValue = "";
                    } else {
                        encodedValue = Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
                    }
                    writer.println(entry.getKey() + "=" + encodedValue);
                }
            }
        }
    }

    public static void main(String[] args) throws Throwable{
        new Extension().run(args);
    }
}
