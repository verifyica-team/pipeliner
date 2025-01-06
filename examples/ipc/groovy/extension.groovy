

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
        def environmentVariables = getEnvironmentVariables()

        // Read the properties from the input IPC file
        def ipcInProperties = readIpcInProperties()

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

        def ipcOutProperties = new TreeMap<>()
        ipcOutProperties["extension.property.1"] = "extension.foo"
        ipcOutProperties["extension.property.2"] = "extension.bar"

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
        System.getenv().sort()
    }

    /**
     * Check if trace is enabled
     */
    private boolean isTraceEnabled() {
        System.getenv(PIPELINER_TRACE) == "true"
    }

    /**
     * Read the IPC properties
     */
    private Map<String, String> readIpcInProperties() {
        def ipcFilenameInput = getEnvironmentVariables()[PIPELINER_IPC_IN]
        println "$PIPELINER_IPC_IN file [$ipcFilenameInput]"
        def ipcInputFile = new File(ipcFilenameInput)
        read(ipcInputFile)
    }

    /**
     * Write the IPC properties
     */
    private void writeIpcOutProperties(Map<String, String> properties) {
        def ipcFilenameOutput = getEnvironmentVariables()[PIPELINER_IPC_OUT]
        println "$PIPELINER_IPC_OUT file [$ipcFilenameOutput]"
        def ipcOutputFile = new File(ipcFilenameOutput)
        write(ipcOutputFile, properties)
    }

    /**
     * Escape special characters
     */
    private String escapeCRLF(String value) {
        value?.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n")
    }

    /**
     * Unescape special characters
     */
    private String unescapeCRLF(String value) {
        value?.replace("\\n", "\n").replace("\\r", "\r").replace("\\\\", "\\")
    }

    /**
     * Read properties from a file
     */
    private Map<String, String> read(File ipcFile) {
        def map = new TreeMap<>()
        ipcFile.eachLine(StandardCharsets.UTF_8.name()) { line ->
            if (!line.trim() || line.startsWith("#")) return
            def equalIndex = line.indexOf('=')
            if (equalIndex == -1) {
                map[line.trim()] = ""
            } else {
                def key = line[0..equalIndex - 1].trim()
                def value = unescapeCRLF(line[equalIndex + 1..-1])
                map[key] = value
            }
        }
        map
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
