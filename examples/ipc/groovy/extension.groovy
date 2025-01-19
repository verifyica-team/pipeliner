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

import java.nio.file.Files
import java.nio.file.Paths

// Get the input and output file paths from environment variables
def ipcInFile = System.getenv("PIPELINER_IPC_IN")
def ipcOutFile = System.getenv("PIPELINER_IPC_OUT")

// Validate input file
if (!ipcInFile || !Files.exists(Paths.get(ipcInFile))) {
    System.err.println("Error: PIPELINER_IPC_IN is not set or the file does not exist.")
    System.exit(1)
}

// Validate output file
if (!ipcOutFile || !Files.exists(Paths.get(ipcOutFile))) {
    System.err.println("Error: PIPELINER_IPC_OUT is not set or the file does not exist.")
    System.exit(1)
}

println "PIPELINER_IPC_IN file [${ipcInFile}]"

// Read input file into a map
def ipcInProperties = [:]

Files.lines(Paths.get(ipcInFile)).each { line ->
    // Skip empty lines and lines without '='
    if (!line?.trim() || !line.contains('=')) {
        return
    }

    // Split the line into key and value
    def (key, encodedValue) = line.split('=', 2).toList() + ''
    def decodedValue = encodedValue ? new String(Base64.decoder.decode(encodedValue), "UTF-8") : ''

    // Add to the map
    ipcInProperties[key] = decodedValue
}

// Debug output for the map
ipcInProperties.each { key, value ->
    println "PIPELINER_IPC_IN property [${key}] = [${value}]"
}

println "This is a sample Groovy extension"

// A property name must match the regular expression `^[a-zA-Z0-9_][a-zA-Z0-9_.-]*[a-zA-Z0-9_]$`

// Example output properties (replace with actual values)
def ipcOutProperties = [
        "extension.property.1": "groovy.extension.foo",
        "extension.property.2": "groovy.extension.bar"
]

println "PIPELINER_IPC_OUT file [${ipcOutFile}]"

// Write the map to the output file with Base64-encoded values
def outputLines = ipcOutProperties.collect { key, value ->
    try {
        println "PIPELINER_IPC_OUT property [${key}] = [${value}]"
        def encodedValue = value ? Base64.encoder.encodeToString(value.bytes) : ''
        "${key}=${encodedValue}"
    } catch (Exception ex) {
        System.err.println("Error processing property [${key}]: ${ex.message}")
        null
    }
}.findAll { it != null }

Files.write(Paths.get(ipcOutFile), outputLines.join('\n').getBytes("UTF-8"))
