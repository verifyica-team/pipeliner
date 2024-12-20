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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.Properties;
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
     * Send the properties
     *
     * @param ipcFile ipcFile
     * @param map map
     * @throws IpcException If an error occurs
     */
    public static void write(File ipcFile, Map<String, String> map) throws IpcException {
        try {
            try (OutputStream out = Files.newOutputStream(ipcFile.toPath())) {
                Properties properties = new Properties();
                properties.putAll(map);
                properties.store(out, "# IpcMap");
            }
        } catch (IOException e) {
            ipcFile.delete();
            throw new IpcException("failed to write IPC file", e);
        }
    }

    /**
     * Receive the properties
     *
     * @param ipcFile ipcFile
     * @return map map
     * @throws IpcException If an error occurs
     */
    public static Map<String, String> read(File ipcFile) throws IpcException {
        try {
            Map<String, String> map = new TreeMap<>();
            Properties properties = new Properties();
            properties.load(Files.newInputStream(ipcFile.toPath()));
            properties.forEach((object, object2) -> map.put(object.toString(), object2.toString()));
            return map;
        } catch (IOException e) {
            throw new IpcException("failed to read IPC file", e);
        }
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
     * Cleanup the Ipc file
     *
     * @param ipcFile ipcFile
     */
    public static void cleanup(File ipcFile) {
        if (ipcFile != null) {
            ipcFile.delete();
        }
    }
}
