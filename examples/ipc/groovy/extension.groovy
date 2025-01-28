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
String ipcInFile = System.getenv('PIPELINER_IPC_IN')
String ipcOutFile = System.getenv('PIPELINER_IPC_OUT')

// Validate input file
if (!ipcInFile || !Files.exists(Paths.get(ipcInFile))) {
    System.err.println('Error: PIPELINER_IPC_IN is not set or the file does not exist.')
    System.exit(1)
}

// Validate output file
if (!ipcOutFile || !Files.exists(Paths.get(ipcOutFile))) {
    System.err.println('Error: PIPELINER_IPC_OUT is not set or the file does not exist.')
    System.exit(1)
}

println "PIPELINER_IPC_IN file [${ipcInFile}]"

// Read input file into a map
Map<String, String> ipcInProperties = [:]

Files.lines(Paths.get(ipcInFile)).each { line ->
    // Trim the line first
    line = line?.trim()

    // Skip empty lines and lines that start with '#'
    if (!line || line.startsWith('#')) {
        return
    }

    // Split the line into key and value
    def (encodedKey, encodedValue) = line.split(' ').toList() + ''
    String name = encodedKey ? new String(Base64.decoder.decode(encodedKey), "UTF-8") : ''
    String value = encodedValue ? new String(Base64.decoder.decode(encodedValue), "UTF-8") : ''

    // Add to the map
    ipcInProperties[name] = value
}

// Debug output for the map
ipcInProperties.each { key, value ->
    println "PIPELINER_IPC_IN variable [${key}] = [${value}]"
}

println 'This is a sample Groovy extension'

// Example output properties (replace with actual values)
Map<String, String> ipcOutProperties = [
        'groovy_extension_variable_1': 'groovy.extension.foo',
        'groovy_extension_variable_2': 'groovy.extension.bar'
]

println "PIPELINER_IPC_OUT file [${ipcOutFile}]"

// Write the values to the output file
def outputLines = ipcOutProperties.collect { name, value ->
        println "PIPELINER_IPC_OUT variable [${name}] = [${value}]"

        def encodedName = name ? Base64.encoder.encodeToString(name.bytes) : ''
        def encodedValue = value ? Base64.encoder.encodeToString(value.bytes) : ''

        "${encodedName} ${encodedValue}"
}.findAll { it != null }

// Write the encoded lines to the output file
Files.write(Paths.get(ipcOutFile), outputLines)
