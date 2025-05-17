#!/bin/bash

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

# Check if the input file is specified and exists
if [[ -z "$PIPELINER_IPC_IN" || ! -f "$PIPELINER_IPC_IN" ]]; then
    echo "Error: PIPELINER_IPC_IN is not set or the file does not exist."
    exit 1
fi

echo "PIPELINER_IPC_IN file [$PIPELINER_IPC_IN]"

# Declare an associative array
declare -A ipc_in_properties

# Read the file line by line
while IFS= read -r line; do
    # Skip empty lines and comments
    if [[ -z "$line" || "$line" == \#* ]]; then
        continue
    fi

    # Split the line into name and value on the first space
    encoded_name="${line%% *}"
    encoded_value="${line#* }"

    # Decode the name from Base64
    name=$(echo "$encoded_name" | base64 --decode)

    # Check if encoded_value is empty
    if [[ -z "$encoded_value" ]]; then
        value=""
    else
        # Decode the value from Base64
        value=$(echo "$encoded_value" | base64 --decode)
    fi

    # Store the key and decoded value in the associative array
    ipc_in_properties["$name"]="$value"

done < "$PIPELINER_IPC_IN"

# Output the associative array for debugging or demonstration
for key in "${!ipc_in_properties[@]}"; do
    echo "PIPELINER_IPC_IN variable [$key] = [${ipc_in_properties[$key]}]"
done

echo "This is a sample Bash extension"

# Check if the output file is specified
if [[ -z "$PIPELINER_IPC_OUT" || ! -f "$PIPELINER_IPC_OUT" ]]; then
    echo "Error: PIPELINER_IPC_OUT is not set."
    exit 1
fi

# Example associative array (replace with your array)
declare -A ipc_out_properties=(
    ["bash_extension_variable_1"]="bash extension foo"
    ["bash_extension_variable_2"]="bash extension bar"
)

echo "PIPELINER_IPC_OUT file [$PIPELINER_IPC_OUT]"

# Write the associative array to the output file with Base64-encoded values
for name in "${!ipc_out_properties[@]}"; do
    value="${ipc_out_properties[$name]}"

    # Base64 encode the name
    encoded_name=$(echo -n "$name" | base64)

    # Base64 encode the value
    encoded_value=$(echo -n "$value" | base64)

    echo "PIPELINER_IPC_OUT variable [$key] = [$value]"

    # Write the key and Base64-encoded value to the file
    echo "$encoded_name $encoded_value" >>  "$PIPELINER_IPC_OUT"
done
