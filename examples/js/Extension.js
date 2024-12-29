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

const Ipc = require('./Ipc');
const fs = require('fs');
const path = require('path');
const os = require('os');

/**
 * Class to implement Extension
 */
class Extension {

    static PIPELINER_TRACE = 'PIPELINER_TRACE';
    static PIPELINER_IPC_IN = 'PIPELINER_IPC_IN';
    static PIPELINER_IPC_OUT = 'PIPELINER_IPC_OUT';

    /**
     * Constructor
     */
    constructor() {
        // INTENTIONALLY BLANK
    }

    /**
     * Run the extension
     *
     * @param {Array} args Command line arguments
     * @throws {Error} If an error occurs
     */
    async run(args) {
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

        console.log("This is a sample JavaScript extension");
        for (const [key, value] of ipcInProperties) {
            console.log(`extension with property [${key}] = [${value}]`);
        }

        const ipcOutProperties = new Map();

        // Pipeliner will automatically scope the properties if ids (pipeliner, job, step) are available
        ipcOutProperties.set("extension.property.1", "extension.foo");
        ipcOutProperties.set("extension.property.2", "extension.bar");

        // Write the properties to the output IPC file
        await this.writeIpcOutProperties(ipcOutProperties);
    }

    /**
     * Read the IPC properties from the input file
     *
     * @returns {Promise<Map>} A Map of properties
     * @throws {Error} If an error occurs
     */
    async readIpcInProperties() {
        const ipcFilenameInput = process.env[Extension.PIPELINER_IPC_IN];
        console.log(`${Extension.PIPELINER_IPC_IN} [${ipcFilenameInput}]`);
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
        console.log(`${Extension.PIPELINER_IPC_OUT} [${ipcFilenameOutput}]`);
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

// Running the extension
Extension.main(process.argv);

