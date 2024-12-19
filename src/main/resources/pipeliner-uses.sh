#!/bin/bash

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

# Exit script on any error
set -e

# Function to create a temporary directory
create_temp_dir() {
    TEMP_DIR=$(mktemp -d)
}

# Function to download or copy a file
download_or_copy_file() {
    local file_url="$1"
    local target_dir="$2"
    local file_name=$(basename "$file_url")

    if [[ $file_url == file://* ]]; then
        # Local file
        local_file="${file_url#file://}"
        cp "$local_file" "$target_dir/$file_name"
    else
        # Remote file
        curl -sL "$file_url" -o "$target_dir/$file_name"
    fi

    echo "$target_dir/$file_name"
}

# Function to extract .tar.gz files
extract_tar_gz() {
    local tar_file="$1"
    local target_dir="$2"
    tar -xzf "$tar_file" -C "$target_dir"
}

# Function to extract .zip files
extract_zip() {
    local zip_file="$1"
    local target_dir="$2"
    unzip -q "$zip_file" -d "$target_dir"
}

# Function to execute the run.sh script
execute_script() {
    local script_dir="$1"
    local file_url="$2"
    local script_file="$script_dir/execute.sh"

    if [[ ! -f "$script_file" ]]; then
        echo "execute.sh not found in [$file_url]"
        exit 1
    fi

    chmod +x "$script_file"
    (cd "$script_dir" && bash "$script_file")
    return $?
}

# Main script logic
main() {
    if [[ $# -ne 1 ]]; then
        echo "Usage: $0 <URL>"
        exit 1
    fi

    local file_url="$1"

    # Create a temporary directory
    create_temp_dir

    # Download or copy the file
    file_path=$(download_or_copy_file "$file_url" "$TEMP_DIR")

    # Extract the file based on its extension
    if [[ $file_path == *.tar.gz ]]; then
        extract_tar_gz "$file_path" "$TEMP_DIR"
    elif [[ $file_path == *.zip ]]; then
        extract_zip "$file_path" "$TEMP_DIR"
    else
        echo "unsupported file type [$file_path]"
        exit 1
    fi

    # Run the script and capture the exit code
    execute_script "$TEMP_DIR" "$file_url"
    local exit_code=$?

    # Clean up
    rm -rf "$TEMP_DIR"

    return $exit_code
}

# Run the main function
main "$@"
