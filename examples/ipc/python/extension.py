#
# Copyright (C) 2024-present Pipeliner project authors and contributors
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

import base64
import os
import sys
from pathlib import Path

# Custom Exception Class to simulate IpcException
class IpcException(Exception):

    def __init__(self, message, cause=None):
        super().__init__(message)
        self.cause = cause

# Class to implement Extension
class Extension:

    PIPELINER_TRACE = "PIPELINER_TRACE"
    PIPELINER_IPC_IN = "PIPELINER_IPC_IN"
    PIPELINER_IPC_OUT = "PIPELINER_IPC_OUT"

    def __init__(self):
        pass

    #
    # Run the extension.
    #
    # :param args: Command line arguments
    # :raises Exception: If an error occurs
    #
    async def run(self, args):
        environment_variables = self.get_environment_variables()

        # Read the properties from the input IPC file
        ipc_in_properties = await self.read_ipc_in_properties()

        if self.is_trace_enabled():
            for key, value in environment_variables.items():
                print(f"@trace environment variable [{key}] = [{value}]")

            for key, value in ipc_in_properties.items():
                print(f"@trace extension property [{key}] = [{value}]")

        for key, value in ipc_in_properties.items():
            print(f"PIPELINER_IPC_IN property [{key}] = [{value}]")

        print("This is a sample Python extension")

        ipc_out_properties = {
            "extension.property.1": "python.extension.foo",
            "extension.property.2": "python.extension.bar"
        }

        print(f"PIPELINER_IPC_OUT file [{environment_variables.get(self.PIPELINER_IPC_OUT)}]")

        for key, value in ipc_out_properties.items():
            print(f"PIPELINER_IPC_OUT property [{key}] = [{value}]")

        # Write the properties to the output IPC file
        await self.write_ipc_out_properties(ipc_out_properties)

    #
    # Read the IPC properties from the input file.
    #
    # :return: A dictionary of properties
    # :raises Exception: If an error occurs
    #
    async def read_ipc_in_properties(self):
        ipc_filename_input = os.getenv(self.PIPELINER_IPC_IN)
        print(f"{self.PIPELINER_IPC_IN} file [{ipc_filename_input}]")
        ipc_input_file = Path(ipc_filename_input).resolve()

        try:
            # Read properties from the input file
            properties = {}
            with ipc_input_file.open("r", encoding="utf-8") as file:
                for line in file:
                    line = line.strip()
                    if line and not line.startswith("#"):  # Skip empty and commented lines
                        # Split on the first "=" character
                        key, sep, value = line.partition("=")
                        key = key.strip()
                        value = value.strip() if sep else ""  # Use empty string if no "=" or value

                        if key:  # Ensure the key is not empty
                            properties[key] = base64.b64decode(value).decode('utf-8')

            return properties
        except Exception as e:
            raise Exception(f"Failed to read IPC input file: {str(e)}")

    #
    # Write the IPC properties to the output file.
    #
    # :param properties: A dictionary of properties to write
    # :raises Exception: If an error occurs
    #
    async def write_ipc_out_properties(self, properties):
        ipc_filename_output = os.getenv(self.PIPELINER_IPC_OUT)
        ipc_output_file = Path(ipc_filename_output).resolve()

        try:
            with ipc_output_file.open("w", encoding="utf-8") as file:
                for key, value in properties.items():
                    if value is None:
                        encoded_value = ""
                    else:
                        encoded_value = base64.b64encode(value.encode("utf-8")).decode("utf-8")
                    file.write(f"{key}={encoded_value}\n")
                    file.flush()
        except Exception as e:
            raise Exception(f"Failed to write IPC output file: {str(e)}")

    #
    # Get environment variables.
    #
    # :return: A dictionary of environment variables
    #
    def get_environment_variables(self):
        return dict(os.environ)

    #
    # Check if trace is enabled.
    #
    # return: True if trace is enabled, else False
    #
    def is_trace_enabled(self):
        return os.getenv(self.PIPELINER_TRACE) == 'true'

    #
    # Main method to run the extension.
    #
    # :param args: Command line arguments
    # :raises Exception: If an error occurs
    #
    @staticmethod
    async def main(args):
        try:
            extension = Extension()
            await extension.run(args)
        except Exception as e:
            print(f"Error occurred during execution: {e}", file=sys.stderr)


#
# Main method to run the extension.
#
if __name__ == "__main__":
    import asyncio
    asyncio.run(Extension.main(sys.argv))
