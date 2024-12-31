"""
Copyright (C) 2024-present Pipeliner project authors and contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

import os
import sys
from pathlib import Path
from Ipc import Ipc

class Extension:

    PIPELINER_TRACE = "PIPELINER_TRACE"
    PIPELINER_IPC_IN = "PIPELINER_IPC_IN"
    PIPELINER_IPC_OUT = "PIPELINER_IPC_OUT"

    def __init__(self):
        pass

    async def run(self, args):
        """
        Run the extension.

        :param args: Command line arguments
        :raises Exception: If an error occurs
        """
        environment_variables = self.get_environment_variables()

        # Read the properties from the input IPC file
        ipc_in_properties = await self.read_ipc_in_properties()

        if self.is_trace_enabled():
            for key, value in environment_variables.items():
                print(f"@trace environment variable [{key}] = [{value}]")

            for key, value in ipc_in_properties.items():
                print(f"@trace extension property [{key}] = [{value}]")

        print("This is a sample Python extension")
        for key, value in ipc_in_properties.items():
            print(f"extension with property [{key}] = [{value}]")

        ipc_out_properties = {
            "extension.property.1": "extension.foo",
            "extension.property.2": "extension.bar"
        }

        # Write the properties to the output IPC file
        await self.write_ipc_out_properties(ipc_out_properties)

    async def read_ipc_in_properties(self):
        """
        Read the IPC properties from the input file.

        :return: A dictionary of properties
        :raises Exception: If an error occurs
        """
        ipc_filename_input = os.getenv(self.PIPELINER_IPC_IN)
        print(f"{self.PIPELINER_IPC_IN} [{ipc_filename_input}]")
        ipc_input_file = Path(ipc_filename_input).resolve()

        try:
            return Ipc.read(ipc_input_file)
        except Exception as e:
            raise Exception(f"Failed to read IPC input file: {str(e)}")

    async def write_ipc_out_properties(self, properties):
        """
        Write the IPC properties to the output file.

        :param properties: A dictionary of properties to write
        :raises Exception: If an error occurs
        """
        ipc_filename_output = os.getenv(self.PIPELINER_IPC_OUT)
        print(f"{self.PIPELINER_IPC_OUT} [{ipc_filename_output}]")
        ipc_output_file = Path(ipc_filename_output).resolve()

        try:
            Ipc.write(ipc_output_file, properties)
        except Exception as e:
            raise Exception(f"Failed to write IPC output file: {str(e)}")

    def get_environment_variables(self):
        """
        Get environment variables.

        :return: A dictionary of environment variables
        """
        return dict(os.environ)

    def is_trace_enabled(self):
        """
        Check if trace is enabled.

        :return: True if trace is enabled, else False
        """
        return os.getenv(self.PIPELINER_TRACE) == 'true'

    @staticmethod
    async def main(args):
        """
        Main method to run the extension.

        :param args: Command line arguments
        :raises Exception: If an error occurs
        """
        try:
            extension = Extension()
            await extension.run(args)
        except Exception as e:
            print(f"Error occurred during execution: {e}", file=sys.stderr)


# Run the extension
if __name__ == "__main__":
    import asyncio
    asyncio.run(Extension.main(sys.argv))
