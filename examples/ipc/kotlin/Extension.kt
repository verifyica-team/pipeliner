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
        // Skip empty lines and lines without '='
        if (line.isBlank() || !line.contains("=")) return@forEachLine

        // Split the line into key and value
        val (key, encodedValue) = line.split("=", limit = 2).let {
            it[0] to if (it.size > 1) it[1] else ""
        }

        // Decode the Base64 value
        val decodedValue = if (encodedValue.isNotBlank()) {
            String(Base64.getDecoder().decode(encodedValue))
        } else {
            ""
        }

        // Add to the map
        ipcInProperties[key] = decodedValue
    }

    // Debug output for the map
    ipcInProperties.forEach { (key, value) ->
        println("PIPELINER_IPC_IN property [$key] = [$value]")
    }

    println("This is a sample Kotlin extension")

    // Example output properties (replace with actual values)
    val ipcOutProperties = mapOf(
        "extension_property_1" to "kotlin.extension.foo",
        "extension_property_2" to "kotlin.extension.bar"
    )

    println("PIPELINER_IPC_OUT file [$ipcOutFile]")

    // Write the map to the output file with Base64-encoded values
    File(ipcOutFile).bufferedWriter().use { writer ->
        ipcOutProperties.forEach { (key, value) ->
            if (key.isBlank()) return@forEach // Skip entries with empty keys

            try {
                println("PIPELINER_IPC_OUT property [$key] = [$value]")

                val encodedValue = if (value.isNotBlank()) {
                    Base64.getEncoder().encodeToString(value.toByteArray())
                } else {
                    ""
                }

                // Write the key-value pair to the output file
                writer.write("$key=$encodedValue")
                writer.newLine()
            } catch (e: Exception) {
                System.err.println("Error processing property [$key]: ${e.message}")
            }
        }
    }
}
