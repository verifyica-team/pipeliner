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
            return value?.replace("\\n", "\n")?.replace("\\r", "\r")?.replace("\\\\", "\\")
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

        println("This is a sample Kotlin extension")
        ipcInProperties.forEach { (key, value) ->
            println("extension with property [$key] = [$value]")
        }

        val ipcOutProperties = sortedMapOf(
            "extension.property.1" to "extension.foo",
            "extension.property.2" to "extension.bar"
        )

        // Write the properties to the output IPC file
        writeIpcOutProperties(ipcOutProperties)
    }

    private fun isTraceEnabled(): Boolean {
        return System.getenv(PIPELINER_TRACE) == "true"
    }

    private fun readIpcInProperties(): Map<String, String> {
        val ipcFilenameInput = getEnvironmentVariables()[PIPELINER_IPC_IN]
        println("$PIPELINER_IPC_IN [$ipcFilenameInput]")
        val ipcInputFile = File(ipcFilenameInput)
        return read(ipcInputFile)
    }

    private fun writeIpcOutProperties(properties: Map<String, String>) {
        val ipcFilenameOutput = getEnvironmentVariables()[PIPELINER_IPC_OUT]
        println("$PIPELINER_IPC_OUT [$ipcFilenameOutput]")
        val ipcOutputFile = File(ipcFilenameOutput)
        write(ipcOutputFile, properties)
    }
}

fun main(args: Array<String>) {
    Extension().run(args)
}
