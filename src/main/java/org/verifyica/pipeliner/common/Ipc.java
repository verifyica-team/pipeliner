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

package org.verifyica.pipeliner.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Class to implement Ipc */
public class Ipc {

    private static final String TEMPORARY_DIRECTORY_PREFIX = "pipeliner-ipc-";

    private static final String TEMPORARY_DIRECTORY_SUFFIX = "";

    private static final Set<PosixFilePermission> PERMISSIONS = PosixFilePermissions.fromString("rw-------");

    /** Constructor */
    private Ipc() {
        // INTENTIONALLY BLANK
    }

    /**
     * Create a new Ipc file
     *
     * @return an Ipc file
     * @throws IpcException If an error occurs
     */
    public static File createIpcFile() throws IpcException {
        try {
            File file = File.createTempFile(TEMPORARY_DIRECTORY_PREFIX, TEMPORARY_DIRECTORY_SUFFIX);
            Files.setPosixFilePermissions(file.toPath(), PERMISSIONS);
            ShutdownHook.deleteOnExit(file.toPath());
            return file;
        } catch (IOException e) {
            throw new IpcException("failed to create IPC file", e);
        }
    }

    /**
     * Read the properties
     *
     * @param ipcFile ipcFile
     * @return map map
     * @throws IpcException If an error occurs
     */
    public static Map<String, String> read(File ipcFile) throws IpcException {
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
                    map.put(key, unescapeCRLF(value));
                }
            }
        } catch (IOException e) {
            throw new IpcException("Failed to read IPC file", e);
        }

        return map;
    }

    /**
     * Write the properties
     *
     * @param ipcFile ipcFile
     * @param map map
     * @throws IpcException If an error occurs
     */
    public static void write(File ipcFile, Map<String, String> map) throws IpcException {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(Files.newOutputStream(ipcFile.toPath()), StandardCharsets.UTF_8))) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String escapedValue = escapeCRLF(entry.getValue());
                writer.println(entry.getKey() + "=" + escapedValue);
            }
        } catch (IOException e) {
            throw new IpcException("Failed to write IPC file", e);
        }
    }

    /**
     * Cleanup the Ipc file
     *
     * @param ipcFile ipcFile
     */
    public static void cleanup(File ipcFile) {
        if (ipcFile != null) {
            ipcFile.delete();
        }
    }

    /**
     * Escapes \, \r, and \n
     *
     * @param value the string to escape
     * @return the escaped string
     */
    private static String escapeCRLF(String value) {
        if (value == null) {
            return null;
        }

        return value.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n");
    }

    /**
     * Unescapes \, \r, and \n
     *
     * @param value the string to unescape
     * @return the unescaped string
     */
    private static String unescapeCRLF(String value) {
        if (value == null) {
            return null;
        }

        return value.replace("\\n", "\n").replace("\\r", "\r").replace("\\\\", "\\");
    }
}
