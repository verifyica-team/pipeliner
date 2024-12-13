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
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/** Class to implement Ipc */
public class Ipc {

    /** Constructor */
    private Ipc() {
        // INTENTIONALLY BLANK
    }

    /**
     * Send the properties
     *
     * @param ipcFile ipcFile
     * @param map map
     * @throws IOException If an error occurs
     */
    public static void write(File ipcFile, Map<String, String> map) throws IOException {
        try (OutputStream out = Files.newOutputStream(ipcFile.toPath())) {
            Properties properties = new Properties();
            properties.putAll(map);
            properties.store(out, "# IpcMap");
        }
    }

    /**
     * Receive the properties
     *
     * @param ipcFile ipcFile
     * @return map map
     */
    public static Map<String, String> read(File ipcFile) throws IOException {
        Map<String, String> map = new TreeMap<>();

        Properties properties = new Properties();
        properties.load(Files.newInputStream(ipcFile.toPath()));
        properties.forEach((object, object2) -> map.put(object.toString(), object2.toString()));

        return map;
    }

    /**
     * Create a new Ipc file
     *
     * @return an Ipc file
     * @throws IOException If an error occurs
     */
    public static File createIpcFile() throws IOException {
        return File.createTempFile("pipeliner-ipc-", "");
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
