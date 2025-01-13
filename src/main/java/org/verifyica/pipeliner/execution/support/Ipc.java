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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.verifyica.pipeliner.common.ShutdownHook;
import org.verifyica.pipeliner.model.PropertyName;

/** Class to implement Ipc */
public class Ipc {

    private static final String TEMPORARY_FILE_PREFIX = "pipeliner-ipc-";

    private static final String TEMPORARY_FILE_SUFFIX = "";

    private static final Set<PosixFilePermission> PERMISSIONS = PosixFilePermissions.fromString("rw-------");

    private static final int BUFFER_SIZE_BYTES = 16384;

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
            File file = File.createTempFile(TEMPORARY_FILE_PREFIX, TEMPORARY_FILE_SUFFIX);
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
     * @param ipcFile the IPC file
     * @return map the properties map
     * @throws IpcException If an error occurs
     */
    public static Map<String, String> read(File ipcFile) throws IpcException {
        Map<String, String> map = new TreeMap<>();
        String line;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(ipcFile.toPath()), StandardCharsets.UTF_8),
                BUFFER_SIZE_BYTES)) {
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                int equalIndex = line.indexOf('=');
                if (equalIndex == -1) {
                    String key = line.trim();
                    if (PropertyName.isInvalid(key)) {
                        throw new IpcException(format("invalid capture property [%s]", key));
                    }
                    map.put(key, "");
                } else {
                    String key = line.substring(0, equalIndex).trim();
                    if (PropertyName.isInvalid(key)) {
                        throw new IpcException(format("invalid capture property [%s]", key));
                    }
                    String value = line.substring(equalIndex + 1);
                    String decodedValue = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
                    map.put(key, decodedValue); // unescapeCRLF(value));
                }
            }
        } catch (IOException e) {
            throw new IpcException("failed to read IPC file", e);
        }

        return map;
    }

    /**
     * Write the properties
     *
     * @param ipcFile the IPC file
     * @param map the property map
     * @throws IpcException If an error occurs
     */
    public static void write(File ipcFile, Map<String, String> map) throws IpcException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(ipcFile.toPath()), StandardCharsets.UTF_8),
                BUFFER_SIZE_BYTES)) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String value = entry.getValue();
                String encodedValue;
                if (value == null) {
                    encodedValue = "";
                } else {
                    encodedValue = Base64.getEncoder()
                            .encodeToString(value.getBytes(StandardCharsets.UTF_8)); // escapeCRLF(entry.getValue());
                }
                writer.write(entry.getKey() + "=" + encodedValue);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IpcException("failed to write IPC file", e);
        }
    }

    /**
     * Cleanup the Ipc file
     *
     * @param ipcFile the IPC file
     */
    public static void cleanup(File ipcFile) {
        if (ipcFile != null) {
            ipcFile.delete();
        }
    }
}
