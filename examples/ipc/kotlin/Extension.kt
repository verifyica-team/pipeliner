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

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * Class to implement Extension
 */
class Extension {

    companion object {
        private const val PIPELINER_TRACE = "PIPELINER_TRACE"
        private const val PIPELINER_IPC_IN = "PIPELINER_IPC_IN"
        private const val PIPELINER_IPC_OUT = "PIPELINER_IPC_OUT"

        /**
         * Get environment variables
         *
         * @return Map of environment variables
         */
        private fun getEnvironmentVariables(): Map<String, String> = System.getenv().toSortedMap()

        /**
         * Escapes \, \r, and \n
         *
         * @param value the string to escape
         * @return the escaped string
         */
        private fun escapeCRLF(value: String?): String? {
            return value?.replace("\\", "\\\\")?.replace("\r", "\\r")?.replace("\n", "\\n")
        }

        /**
         * Unescapes \, \r, and \n
         *
         * @param value the string to unescape
         * @return the unescaped string
         */
        private fun unescapeCRLF(value: String?): String? {
            return value?.replace("\\\\", "\\")?.replace("\\n", "\n")?.replace("\\r", "\r")
        }

        /**
         * Read the properties
         *
         * @param ipcFile ipcFile
         * @return map map
         * @throws IOException If an error occurs
         */
        @Throws(IOException::class)
        private fun read(ipcFile: File): Map<String, String> {
            val map = mutableMapOf<String, String>()

            Files.newBufferedReader(ipcFile.toPath(), StandardCharsets.UTF_8).use { reader ->
                reader.lineSequence().forEach { line ->
                    if (line.isBlank() || line.startsWith("#")) return@forEach

                    val equalIndex = line.indexOf('=')
                    if (equalIndex == -1) {
                        map[line.trim()] = ""
                    } else {
                        val key = line.substring(0, equalIndex).trim()
                        val value = line.substring(equalIndex + 1)
                        map[key] = unescapeCRLF(value) ?: ""
                    }
                }
            }

            return map
        }

        /**
         * Write the properties
         *
         * @param ipcFile ipcFile
         * @param map map
         * @throws IOException If an error occurs
         */
        @Throws(IOException::class)
        private fun write(ipcFile: File, map: Map<String, String>) {
            Files.newOutputStream(ipcFile.toPath()).use { outputStream ->
                PrintWriter(OutputStreamWriter(outputStream, StandardCharsets.UTF_8)).use { writer ->
                    map.forEach { (key, value) ->
                        writer.println("$key=${escapeCRLF(value)}")
                    }
                }
            }
        }
    }

    fun run(args: Array<String>) {
        val environmentVariables = getEnvironmentVariables()

        // Read the properties from the input IPC file
        val ipcInProperties = readIpcInProperties()

        if (isTraceEnabled()) {
            environmentVariables.forEach { (key, value) ->
                println("@trace environment variable [$key] = [$value]")
            }

            ipcInProperties.forEach { (key, value) ->
                println("@trace extension property [$key] = [$value]")
            }
        }

        ipcInProperties.forEach { (key, value) ->
            println("PIPELINER_IPC_IN property [$key] = [$value]")
        }

        println("This is a sample Kotlin extension")

        val ipcOutProperties = sortedMapOf(
            "extension.property.1" to "kotlin.extension.foo",
            "extension.property.2" to "kotlin.extension.bar"
        )

        ipcOutProperties.forEach { (key, value) ->
            println("PIPELINER_IPC_OUT property [$key] = [$value]")
        }

        // Write the properties to the output IPC file
        writeIpcOutProperties(ipcOutProperties)
    }

    private fun isTraceEnabled(): Boolean {
        return System.getenv(PIPELINER_TRACE) == "true"
    }

    private fun readIpcInProperties(): Map<String, String> {
        val ipcFilenameInput = getEnvironmentVariables()[PIPELINER_IPC_IN]
        println("$PIPELINER_IPC_IN file [$ipcFilenameInput]")
        val ipcInputFile = File(ipcFilenameInput)
        return read(ipcInputFile)
    }

    private fun writeIpcOutProperties(properties: Map<String, String>) {
        val ipcFilenameOutput = getEnvironmentVariables()[PIPELINER_IPC_OUT]
        println("$PIPELINER_IPC_OUT file [$ipcFilenameOutput]")
        val ipcOutputFile = File(ipcFilenameOutput)
        write(ipcOutputFile, properties)
    }
}

fun main(args: Array<String>) {
    Extension().run(args)
}
