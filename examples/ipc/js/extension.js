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

// Get the input and output file paths from environment variables
const ipcInFile = process.env.PIPELINER_IPC_IN || '';
const ipcOutFile = process.env.PIPELINER_IPC_OUT || '';

// Validate input file
if (!ipcInFile || !fs.existsSync(ipcInFile)) {
  console.error("Error: PIPELINER_IPC_IN is not set or the file does not exist.");
  process.exit(1);
}

// Validate output file
if (!ipcOutFile || !fs.existsSync(ipcOutFile)) {
  console.error("Error: PIPELINER_IPC_OUT is not set or the file does not exist.");
  process.exit(1);
}

console.log(`PIPELINER_IPC_IN file [${ipcInFile}]`);

// Read input file into an object
const ipcInProperties = {};
const lines = fs.readFileSync(ipcInFile, 'utf8').split('\n');

for (const line of lines) {
  // Skip empty lines and lines that start with '#'
  if (!line.trim() || line.trim().startsWith('#'))
    continue;

  // Split the line into name and value
  const [encodedName, encodedValue = ''] = line.split(' ', 2);

  // Decode the Base64 name
  const name = Buffer.from(encodedName, 'base64').toString('utf8')

  // Decode the Base64 value
  const value = encodedValue
    ? Buffer.from(encodedValue, 'base64').toString('utf8')
    : '';

    // Use Object.defineProperty for safer property assignment
    Object.defineProperty(ipcInProperties, name, {
      value: value,
      enumerable: true,
      writable: true,
      configurable: true
    });
}

// Debug output for the object
for (const [key, value] of Object.entries(ipcInProperties)) {
  console.log(`PIPELINER_IPC_IN variable [${key}] = [${value}]`);
}

console.log("This is a sample JavaScript extension");

// Example output properties (replace with actual values)
const ipcOutProperties = {
  "js_extension_variable_1": "js.extension.foo",
  "js_extension_variable_2": "js.extension.bar"
};

console.log(`PIPELINER_IPC_OUT file [${ipcOutFile}]`);

// Write the properties to the output file
const outputLines = [];

for (const [key, value] of Object.entries(ipcOutProperties)) {
  if (!key) continue; // Skip entries with null or empty keys
    console.log(`PIPELINER_IPC_OUT variable [${key}] = [${value}]`);

    const encodedName = Buffer.from(key, 'utf8').toString('base64');

    const encodedValue = value
      ? Buffer.from(value, 'utf8').toString('base64')
      : '';

    // Write the key-value pair to the array
    outputLines.push(`${encodedName} ${encodedValue}`);
}

// Write the output lines to the file
fs.writeFileSync(ipcOutFile, outputLines.join('\n'), 'utf8');
