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
import tempfile
import atexit

class IpcException(Exception):
    """
    Custom Exception Class to simulate IpcException
    """
    def __init__(self, message, cause=None):
        super().__init__(message)
        self.cause = cause

class Ipc:
    """
    Class to implement IPC (Inter-process communication)
    """
    BUFFER_SIZE_BYTES = 16384
    TEMPORARY_DIRECTORY_PREFIX = "pipeliner-ipc-"
    TEMPORARY_DIRECTORY_SUFFIX = ""

    @staticmethod
    def write(ipc_file_path, data):
        """
        Write properties to the IPC file.

        :param ipc_file_path: Path to the IPC file
        :param data: A dictionary of properties to write
        :raises IpcException: If an error occurs
        """
        try:
            with open(ipc_file_path, 'w', encoding='utf-8') as file:
                file.write("# IpcMap\n")
                for key, value in data.items():
                    file.write(f"{key}={value}\n")
        except Exception as e:
            if os.path.exists(ipc_file_path):
                os.remove(ipc_file_path)
            raise IpcException("Failed to write IPC file", e)

    @staticmethod
    def read(ipc_file_path):
        """
        Read properties from the IPC file.

        :param ipc_file_path: Path to the IPC file
        :return: A dictionary of properties
        :raises IpcException: If an error occurs
        """
        try:
            with open(ipc_file_path, 'r', encoding='utf-8') as file:
                data = file.readlines()

            properties = {}
            for line in data:
                line = line.strip()
                if line and not line.startswith('#'):
                    key, value = line.split('=', 1)
                    properties[key.strip()] = value.strip()

            return properties
        except Exception as e:
            raise IpcException("Failed to read IPC file", e)