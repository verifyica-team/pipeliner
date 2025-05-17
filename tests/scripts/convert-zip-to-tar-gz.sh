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

# Ensure the script exits on errors
set -euo pipefail

# Check if the correct number of arguments is provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <ZIP_FILE> <TAR_GZ_FILE>"
    exit 1
fi

# Get the input and output file paths
input_zip="$1"
output_tar_gz="$2"

# Validate the input zip file
if [ ! -f "$input_zip" ]; then
    echo "Error: Input file '$input_zip' does not exist or is not a regular file."
    exit 1
fi

# Create a temporary directory for extraction
temp_dir=$(mktemp -d)

# Cleanup function to remove the temporary directory on exit
cleanup() {
    rm -rf "$temp_dir"
}
trap cleanup EXIT

# Extract the zip file to the temporary directory
unzip -q "$input_zip" -d "$temp_dir"

# Create a tar.gz file from the extracted contents
tar -czf "$output_tar_gz" -C "$temp_dir" .

echo "Successfully converted '$input_zip' to '$output_tar_gz'."

