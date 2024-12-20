#!/bin/bash

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

