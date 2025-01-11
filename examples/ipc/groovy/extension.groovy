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

import java.nio.charset.StandardCharsets

/**
 * Groovy class to implement the Extension functionality
 */
class Extension {

    static final String PIPELINER_TRACE = "PIPELINER_TRACE"
    static final String PIPELINER_IPC_IN = "PIPELINER_IPC_IN"
    static final String PIPELINER_IPC_OUT = "PIPELINER_IPC_OUT"

    /**
     * Main method to execute the extension
     */
    void run(String[] args) {
        String environmentVariables = getEnvironmentVariables()

        // Read the properties from the input IPC file
        Map<String, String> ipcInProperties = readIpcInProperties()

        if (isTraceEnabled()) {
            environmentVariables.each { key, value ->
                println "@trace environment variable [$key] = [$value]"
            }

            ipcInProperties.each { key, value ->
                println "@trace extension property [$key] = [$value]"
            }
        }

        ipcInProperties.each { key, value ->
            println "PIPELINER_IPC_IN property [$key] = [$value]"
        }

        println "This is a sample Groovy extension"

        Map<String, String> ipcOutProperties = new TreeMap<>()
        ipcOutProperties["extension.property.1"] = "groovy.extension.foo"
        ipcOutProperties["extension.property.2"] = "groovy.extension.bar"

        ipcOutProperties.each { key, value ->
            println "PIPELINER_IPC_OUT property [$key] = [$value]"
        }

        // Write the properties to the output IPC file
        writeIpcOutProperties(ipcOutProperties)
    }

    /**
     * Get environment variables
     */
    private Map<String, String> getEnvironmentVariables() {
        return System.getenv().sort()
    }

    /**
     * Check if trace is enabled
     */
    private boolean isTraceEnabled() {
        return System.getenv(PIPELINER_TRACE) == "true"
    }

    /**
     * Read the IPC properties
     */
    private Map<String, String> readIpcInProperties() {
        String ipcFilenameInput = getEnvironmentVariables()[PIPELINER_IPC_IN]
        println "$PIPELINER_IPC_IN file [$ipcFilenameInput]"
        File ipcInputFile = new File(ipcFilenameInput)
        return read(ipcInputFile)
    }

    /**
     * Write the IPC properties
     */
    private void writeIpcOutProperties(Map<String, String> properties) {
        String ipcFilenameOutput = getEnvironmentVariables()[PIPELINER_IPC_OUT]
        println "$PIPELINER_IPC_OUT file [$ipcFilenameOutput]"
        File ipcOutputFile = new File(ipcFilenameOutput)
        write(ipcOutputFile, properties)
    }

    /**
     * Escape special characters
     */
    private String escapeCRLF(String value) {
        return value?.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n")
    }

    /**
     * Unescape special characters
     */
    private String unescapeCRLF(String value) {
        return value?.replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r")
    }

    /**
     * Read properties from a file
     */
    private Map<String, String> read(File ipcFile) {
        Map<String, String> map = new TreeMap<>()
        ipcFile.eachLine(StandardCharsets.UTF_8.name()) { line ->
            if (!line.trim() || line.trim().startsWith("#")) {
                String equalIndex = line.indexOf('=')
                if (equalIndex == -1) {
                    map[line.trim()] = ""
                } else {
                    String key = line[0..equalIndex - 1].trim()
                    String value = unescapeCRLF(line[equalIndex + 1..-1])
                    map[key] = value
                }
            }
        }

        return map
    }

    /**
     * Write properties to a file
     */
    private void write(File ipcFile, Map<String, String> map) {
        ipcFile.withWriter(StandardCharsets.UTF_8.name()) { writer ->
            map.each { key, value ->
                writer.println("$key=${escapeCRLF(value)}")
            }
        }
    }

    /**
     * Entry point for the script
     */
    static void main(String[] args) {
        new Extension().run(args)
    }

}
