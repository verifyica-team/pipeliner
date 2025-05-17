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

package org.verifyica.pipeliner.core.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.verifyica.pipeliner.common.Accumulator;
import org.verifyica.pipeliner.common.Precondition;
import org.verifyica.pipeliner.common.ShutdownHooks;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement Ipc */
public class Ipc {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ipc.class);

    private static final int BUFFER_SIZE_BYTES = 16384;

    private static final String EMPTY_STRING = "";

    private static final String TEMPORARY_FILE_SUFFIX = "";

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    private static final List<String> EMPTY_LIST = new ArrayList<>();

    private static final Set<PosixFilePermission> PERMISSIONS = PosixFilePermissions.fromString("rw-------");

    /**
     * Constructor
     */
    private Ipc() {
        // INTENTIONALLY BLANK
    }

    /**
     * Create a new IPC file
     *
     * @param prefix the prefix
     * @return a new IPC file
     * @throws IpcException If an error occurs
     */
    public static File createFile(String prefix) throws IpcException {
        Precondition.notBlank(prefix, "prefix is null", "prefix is blank");

        try {
            // Create a temporary file
            File file = File.createTempFile(prefix, TEMPORARY_FILE_SUFFIX);

            // Set the file permissions
            Files.setPosixFilePermissions(file.toPath(), PERMISSIONS);

            // Add the file to the shutdown hook for cleanup
            ShutdownHooks.deleteOnExit(file.toPath());

            return file;
        } catch (IOException e) {
            throw new IpcException("failed to create IPC file", e);
        }
    }

    /**
     * Write the properties
     *
     * @param ipcFile the IPC file
     * @param variables the variables
     * @throws IpcException If an error occurs
     */
    public static void write(File ipcFile, Map<String, String> variables) throws IpcException {
        LOGGER.trace("write IPC file [%s]", ipcFile);

        // Create the IPC file writer
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(ipcFile.toPath()), StandardCharsets.UTF_8),
                BUFFER_SIZE_BYTES)) {

            // Write the variables
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                // Base64 encode the name
                String name = BASE64_ENCODER.encodeToString(entry.getKey().getBytes(StandardCharsets.UTF_8));

                // Base64 encode the value
                String value = entry.getValue() != null
                        ? BASE64_ENCODER.encodeToString(entry.getValue().getBytes(StandardCharsets.UTF_8))
                        : EMPTY_STRING;

                // Write the name and value
                writer.write(name + " " + value);

                // Write a new line
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IpcException("failed to write IPC file", e);
        }
    }

    /**
     * Read the properties
     *
     * @param ipcFile the IPC file
     * @return the variables
     * @throws IpcException If an error occurs
     */
    public static Map<String, String> read(File ipcFile) throws IpcException {
        LOGGER.trace("reading IPC file [%s]", ipcFile);

        Map<String, String> map = new TreeMap<>();
        String line;
        String trimmedLine;

        // Create the IPC file reader
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(ipcFile.toPath()), StandardCharsets.UTF_8),
                BUFFER_SIZE_BYTES)) {
            // Read the lines
            while ((line = reader.readLine()) != null) {
                // Trim the line
                trimmedLine = line.trim();

                // Skip empty lines and comments
                if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                    // Split the line into parts
                    List<String> parts = tokenize(trimmedLine);

                    // Validate the number of parts
                    if (parts.isEmpty() || parts.size() > 2) {
                        throw new IpcException("invalid IPC file");
                    }

                    // Base64 decode the name
                    String name = new String(BASE64_DECODER.decode(parts.get(0)), StandardCharsets.UTF_8);

                    // Base64 decode the value
                    String value = parts.size() > 1
                            ? new String(BASE64_DECODER.decode(parts.get(1)), StandardCharsets.UTF_8)
                            : EMPTY_STRING;

                    // Add the variable
                    map.put(name, value);
                }
            }
        } catch (IOException e) {
            throw new IpcException("failed to read IPC file", e);
        }

        return map;
    }

    /**
     * Cleanup the IPC file
     *
     * @param ipcFile the IPC file
     */
    public static void cleanup(File ipcFile) {
        if (ipcFile != null) {
            ipcFile.delete();
        }
    }

    /**
     * Method to tokenize a line
     *
     * @param line the line
     * @return a list of tokens
     */
    private static List<String> tokenize(String line) {
        if (line == null || line.isEmpty()) {
            return EMPTY_LIST;
        }

        List<String> list = new ArrayList<>();
        char[] characters = line.toCharArray();
        Accumulator accumulator = new Accumulator();
        int i = 0;
        int length = characters.length;

        // Loop through the characters
        while (i < length) {
            // Skip leading whitespace
            while (i < length && Character.isWhitespace(characters[i])) {
                i++;
            }

            // Capture word characters
            while (i < length && !Character.isWhitespace(characters[i])) {
                // Accumulate the character
                accumulator.accumulate(characters[i]);

                i++;
            }

            // If we captured characters
            if (accumulator.isNotEmpty()) {
                // Drain the accumulator and add to the list
                list.add(accumulator.drain());
            }
        }

        return list;
    }
}
