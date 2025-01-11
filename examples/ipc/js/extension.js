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

const fs = require('fs');
const path = require('path');

/**
 * Custom Exception Class for Ipc
 */
class IpcException extends Error {
    constructor(message, cause) {
        super(message);
        this.name = "IpcException";
        this.cause = cause;
    }
}

/**
 * Class to implement Ipc (Inter-process communication)
 */
class Ipc {

    /*
     * Function to escape \, \r, and \n
     *
     * @param {value} string The value to escape
     */
    static escapeCRLF(value) {
        value = value.split('\\').join('\\\\');
        value = value.split('\r').join('\\r');
        value = value.split('\n').join('\\n');
        return value;
    }

    /**
     * Function to unescape \\, \\r, and \\n
     *
     * @param {value} value The value to unescape
     */
    static unescapeCRLF(value) {
        value = value.split('\\n').join('\n');
        value = value.split('\\r').join('\r');
        value = value.split('\\\\').join('\\');
        return value;
    }

    /**
     * Read the properties
     *
     * @param {string} ipcFilePath Path to the IPC file
     * @returns {Map} A Map of properties
     * @throws {IpcException} If an error occurs
     */
    static read(ipcFilePath) {
        try {
            const data = fs.readFileSync(ipcFilePath, { encoding: 'utf8' });
            const map = new Map();

            data.split('\n').forEach(line => {
                if (line.trim() && !line.startsWith('#')) {
                    const [key, value] = line.split('=');
                    if (key && value) {
                        map.set(key.trim(), Ipc.unescapeCRLF(value));
                    }
                }
            });

            return map;
        } catch (e) {
            throw new IpcException("failed to read IPC file", e);
        }
    }

    /**
     * Write the properties
     *
     * @param {string} ipcFilePath Path to the IPC file
     * @param {Map} map A Map of properties to be written
     * @throws {IpcException} If an error occurs
     */
    static write(ipcFilePath, map) {
        try {
            const writeStream = fs.createWriteStream(ipcFilePath, { flags: 'w', encoding: 'utf8' });
            const properties = new Map(map);

            properties.forEach((value, key) => {
                const escapedValue = Ipc.escapeCRLF(value);
                writeStream.write(`${key}=${escapedValue}\n`);
            });

            writeStream.end();
        } catch (e) {
            console.error(e);
            throw new IpcException("failed to write IPC file", e);
        }
    }
}

/**
 * Class to implement Extension
 */
class Extension {

    static PIPELINER_TRACE = 'PIPELINER_TRACE';
    static PIPELINER_IPC_IN = 'PIPELINER_IPC_IN';
    static PIPELINER_IPC_OUT = 'PIPELINER_IPC_OUT';

    /**
     * Read the IPC properties from the input file
     *
     * @returns {Promise<Map>} A Map of properties
     * @throws {Error} If an error occurs
     */
    async readIpcInProperties() {
        const ipcFilenameInput = process.env[Extension.PIPELINER_IPC_IN];
        console.log(`${Extension.PIPELINER_IPC_IN} file [${ipcFilenameInput}]`);
        const ipcInputFile = path.resolve(ipcFilenameInput);

        try {
            return await Ipc.read(ipcInputFile);
        } catch (error) {
            throw new Error(`Failed to read IPC input file: ${error.message}`);
        }
    }

    /**
     * Write the IPC properties to the output file
     *
     * @param {Map} properties A Map of properties to write
     * @throws {Error} If an error occurs
     */
    async writeIpcOutProperties(properties) {
        const ipcFilenameOutput = process.env[Extension.PIPELINER_IPC_OUT];
        console.log(`${Extension.PIPELINER_IPC_OUT} file [${ipcFilenameOutput}]`);
        const ipcOutputFile = path.resolve(ipcFilenameOutput);

        try {
            await Ipc.write(ipcOutputFile, properties);
        } catch (error) {
            throw new Error(`Failed to write IPC output file: ${error.message}`);
        }
    }

    /**
     * Get environment variables as a Map
     *
     * @returns {Object} An object of environment variables
     */
    getEnvironmentVariables() {
        return process.env;
    }

    /**
     * Check if trace is enabled
     *
     * @returns {boolean} True if trace is enabled, else false
     */
    isTraceEnabled() {
        return process.env[Extension.PIPELINER_TRACE] === 'true';
    }

    /**
     * Run the extension
     *
     * @throws {Error} If an error occurs
     */
    async run() {
        const environmentVariables = this.getEnvironmentVariables();

        // Read the properties from the input IPC file
        const ipcInProperties = await this.readIpcInProperties();

        if (this.isTraceEnabled()) {
            for (const [key, value] of Object.entries(environmentVariables)) {
                console.log(`@trace environment variable [${key}] = [${value}]`);
            }

            for (const [key, value] of Object.entries(ipcInProperties)) {
                console.log(`@trace extension property [${key}] = [${value}]`);
            }
        }

        for (const [key, value] of ipcInProperties) {
            console.log(`PIPELINER_IPC_IN property [${key}] = [${value}]`);
        }

        console.log("This is a sample JavaScript extension");

        const ipcOutProperties = new Map();

        // Pipeliner will automatically scope the properties if ids (pipeliner, job, step) are available
        ipcOutProperties.set("extension.property.1", "js.extension.foo");
        ipcOutProperties.set("extension.property.2", "js.extension.bar");

        for (const [key, value] of ipcOutProperties) {
            console.log(`PIPELINER_IPC_OUT property [${key}] = [${value}]`);
        }

        // Write the properties to the output IPC file
        await this.writeIpcOutProperties(ipcOutProperties);
    }

    /**
     * Main method
     *
     * @param {Array} args Command line arguments
     * @throws {Error} If an error occurs
     */
    static async main(args) {
        try {
            const extension = new Extension();
            await extension.run(args);
        } catch (error) {
            console.error('Error occurred during execution:', error);
        }
    }
}

// Run the extension
Extension.main(process.argv);
