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

import java.io.File
import java.util.Base64

/**
 * Main function
 */
fun main() {
    // Get the input and output file paths from environment variables
    val ipcInFile = System.getenv("PIPELINER_IPC_IN") ?: ""
    val ipcOutFile = System.getenv("PIPELINER_IPC_OUT") ?: ""

    // Validate input file
    if (ipcInFile.isEmpty() || !File(ipcInFile).exists()) {
        System.err.println("Error: PIPELINER_IPC_IN is not set or the file does not exist.")
        return
    }

    // Validate output file
    if (ipcOutFile.isEmpty() || !File(ipcOutFile).exists()) {
        System.err.println("Error: PIPELINER_IPC_OUT is not set or the file does not exist.")
        return
    }

    println("PIPELINER_IPC_IN file [$ipcInFile]")

    // Read input file into a map
    val ipcInProperties = mutableMapOf<String, String>()

    File(ipcInFile).forEachLine { line ->
        // Trim the line
        val trimmedLine = line.trim()

        // Skip empty lines and lines that start with '#'
        if (!trimmedLine.isBlank() && !trimmedLine.startsWith("#")) {

            // Split the line into name and value
            val (encodedName, encodedValue) = trimmedLine.split(" ", limit = 2).let {
                it[0] to if (it.size > 1) it[1] else ""
            }

            var name = String(Base64.getDecoder().decode(encodedName))

            // Decode the Base64 value
            val value = if (encodedValue.isNotBlank()) {
                String(Base64.getDecoder().decode(encodedValue))
            } else {
                ""
            }

            // Add to the map
            ipcInProperties[name] = value
        }
    }

    // Debug output for the map
    ipcInProperties.forEach { (key, value) ->
        println("PIPELINER_IPC_IN variable [$key] = [$value]")
    }

    println("This is a sample Kotlin extension")

    // Example output properties (replace with actual values)
    val ipcOutProperties = mapOf(
        "kotlin_extension_variable_1" to "kotlin extension foo",
        "kotlin_extension_variable_2" to "kotlin extension bar"
    )

    println("PIPELINER_IPC_OUT file [$ipcOutFile]")

    // Write the map to the output file
    File(ipcOutFile).bufferedWriter().use { writer ->
        for ((key, value) in ipcOutProperties) {
            // Skip entries with empy keys
            if (key.isBlank()) {
                continue
            }

            println("PIPELINER_IPC_OUT variable [$key] = [$value]")

            // Encode the key and value
            val encodedKey = Base64.getEncoder().encodeToString(key.toByteArray())
            val encodedValue = if (value.isNotBlank()) {
                Base64.getEncoder().encodeToString(value.toByteArray())
            } else {
                ""
            }

            // Write the name-value pair to the output file
            writer.write("$encodedKey $encodedValue")
            writer.newLine()
        }
    }
}
