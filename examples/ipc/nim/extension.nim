#
# Copyright (C) Pipeliner project authors and contributors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# This is AI generated code
#

import base64, strutils, os, tables

# Check if the input file is specified and exists
if not existsEnv("PIPELINER_IPC_IN") or not fileExists(getEnv("PIPELINER_IPC_IN")):
  echo "Error: PIPELINER_IPC_IN is not set or the file does not exist."
  quit(1)

let pipelinerIpcIn = getEnv("PIPELINER_IPC_IN")
echo "PIPELINER_IPC_IN file [", pipelinerIpcIn, "]"

# Declare a table to store key-value pairs
var ipcInProperties = initTable[string, string]()

# Read the file line by line
for line in lines(pipelinerIpcIn):
  # Skip empty lines and comments
  if line.len == 0 or line.startsWith("#"):
    continue

  # Split the line into name and value on the first space
  let parts = line.split(' ', maxsplit=1)
  let encodedName = parts[0]
  let encodedValue = if parts.len > 1: parts[1] else: ""

  # Decode the name from Base64
  let name = decode(encodedName)

  # Decode the value from Base64 (if not empty)
  let value = if encodedValue.len > 0: decode(encodedValue) else: ""

  # Store the key and decoded value in the table
  ipcInProperties[name] = value

# Output the table for debugging or demonstration
for key, value in ipcInProperties.pairs:
  echo "PIPELINER_IPC_IN variable [", key, "] = [", value, "]"

echo "This is a sample Nim extension"

# Check if the output file is specified
if not existsEnv("PIPELINER_IPC_OUT"):
  echo "Error: PIPELINER_IPC_OUT is not set."
  quit(1)

let pipelinerIpcOut = getEnv("PIPELINER_IPC_OUT")

# Example table (replace with your data)
var ipcOutProperties = {
  "nim_extension_variable_1": "nim extension foo",
  "nim_extension_variable_2": "nim extension bar"
}.toTable

echo "PIPELINER_IPC_OUT file [", pipelinerIpcOut, "]"

# Write the table to the output file with Base64-encoded values
let outFile = open(pipelinerIpcOut, fmAppend)  # Open the file in append mode
for name, value in ipcOutProperties.pairs:
  # Base64 encode the name and value
  let encodedName = encode(name)
  let encodedValue = encode(value)

  echo "PIPELINER_IPC_OUT variable [", name, "] = [", value, "]"

  # Write the key and Base64-encoded value to the file
  outFile.writeLine(encodedName & " " & encodedValue)

outFile.close()  # Close the file after writing