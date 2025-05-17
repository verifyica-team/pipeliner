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

import os
import base64

def main():
    # Get the input and output file paths from environment variables
    ipc_in_file = os.getenv("PIPELINER_IPC_IN", "")
    ipc_out_file = os.getenv("PIPELINER_IPC_OUT", "")

    # Validate input file
    if not ipc_in_file or not os.path.exists(ipc_in_file):
        print("Error: PIPELINER_IPC_IN is not set or the file does not exist.")
        exit(1)

    # Validate output file
    if not ipc_out_file or not os.path.exists(ipc_out_file):
        print("Error: PIPELINER_IPC_OUT is not set or the file does not exist.")
        exit(1)

    print(f"PIPELINER_IPC_IN file [{ipc_in_file}]")

    # Read input file into a dictionary
    ipc_in_properties = {}
    with open(ipc_in_file, "r", encoding="utf-8") as f:
        for line in f:
            # Trim the line
            line = line.strip()

            # Skip empty lines and lines that start with "#"
            if not line or line.startswith("#"):
                continue

            # Split the line based on space
            parts = line.split(" ", 1)  # Split only at the first space
            if len(parts) < 2:
                continue  # Skip invalid lines without a space (should not happen if the format is correct)

            encoded_name, encoded_value = parts

            # Decode the Base64 name and value
            name = base64.b64decode(encoded_name).decode("utf-8") if encoded_name else ""
            value = base64.b64decode(encoded_value).decode("utf-8") if encoded_value else ""

            # Add to the properties table (assuming ipc_in_properties is already defined)
            ipc_in_properties[name] = value

    # Debug output for the dictionary
    for key, value in ipc_in_properties.items():
        print(f"PIPELINER_IPC_IN variable [{key}] = [{value}]")

    print("This is a sample Python extension")

    # Example output properties (replace with actual values)
    ipc_out_properties = {
        "python_extension_variable_1": "python extension foo",
        "python_extension_variable_2": "python extension bar"
    }

    print(f"PIPELINER_IPC_OUT file [{ipc_out_file}]")

    # Write the dictionary to the output file with Base64-encoded values
    with open(ipc_out_file, "w", encoding="utf-8") as f:
        for name, value in ipc_out_properties.items():
            if not key:
                continue  # Skip entries with empty keys

            print(f"PIPELINER_IPC_OUT variable [{name}] = [{value}]")

            # Base64 encode the name
            encoded_name = base64.b64encode(name.encode("utf-8")).decode("utf-8")

            # Base64 encode the value
            encoded_value = base64.b64encode(value.encode("utf-8")).decode("utf-8") if value else ""

            # Write the key-value pair to the output file
            f.write(f"{encoded_name} {encoded_value}\n")


if __name__ == "__main__":
    main()
