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

# Function to get release version from GitHub
get_release_version() {
  local version=$1
  local repo=$2
  if [[ "$version" == "latest" ]]; then
    # Get the latest release version
    curl -s "https://api.github.com/repos/$repo/releases/latest" | jq -r .tag_name
  else
    # Check if the specified version exists
    local release
    release=$(curl -s "https://api.github.com/repos/$repo/releases/tags/$version" | jq -r .tag_name)
    if [[ "$release" == "null" ]]; then
      echo "Error: Version $version does not exist."
      exit 1
    fi
    # Return the valid release version
    echo "$release"
  fi
}

# Function to clean up and return to the current directory
cleanup() {
  cd "$CURRENT_DIR" || exit
  rm -Rf "$TMP_DIR"
}

# Save the current directory
CURRENT_DIR=$(pwd)

if [[ $# -gt 1 ]]; then
  echo "Usage:"
  echo "  $0              Download and install the latest version of Pipeliner"
  echo "  $0 [VERSION]    Download and install a specific version of Pipeliner"
  echo "  $0 --remove     Remove Pipeliner"
  echo "  $0 --help       Display this help information"
  exit 0
fi

# Handle the "--remove" argument
if [[ $1 == "--remove" ]]; then
  echo "Removing Pipeliner from $CURRENT_DIR"
  rm -f "$CURRENT_DIR/pipeliner"
  rm -rf "$CURRENT_DIR/.pipeliner"
  exit 0
fi

# Handle the "--usage" argument
if [[ $# -eq 1 && $1 == --* ]]; then
  echo "Usage:"
  echo "  $0              Download and install the latest version of Pipeliner"
  echo "  $0 [VERSION]    Download and install a specific version of Pipeliner"
  echo "  $0 --remove     Remove Pipeliner"
  echo "  $0 --help       Display this help information"
  exit 0
fi

# Ensure required commands are installed
for cmd in curl tar jq; do
  if ! command -v $cmd &> /dev/null; then
    echo "Error: $cmd is not installed. Please install it and try again."
    exit 1
  fi
done

# Validate arguments (0 or 1 arguments allowed)
if [[ $# -gt 1 ]]; then
  echo "Usage: $0 [version | --remove]"
  echo "If no version is specified, the latest version will be used."
  exit 1
fi

# Set repository and determine the version to use
REPO="verifyica-team/pipeliner"
VERSION=${1:-latest}

# get the release version
RELEASE_VERSION=$(get_release_version "$VERSION" "$REPO")
if [[ -z "$RELEASE_VERSION" || "$RELEASE_VERSION" == "null" ]]; then
  echo "Error: Failed to get the release version."
  exit 1
fi

echo "Installing Pipeliner $RELEASE_VERSION into $CURRENT_DIR"

# Create a temporary directory using mktemp
TMP_DIR=$(mktemp -d)

# Ensure the temporary directory is created
if [[ ! -d $TMP_DIR ]]; then
  echo "Failed to create temporary directory"
  exit 1
fi

# Trap to handle cleanup on exit or error
trap cleanup EXIT

# Navigate to the temporary directory
cd "$TMP_DIR" || exit

# Perform operations in the temporary directory
FILE_URL="https://github.com/$REPO/releases/download/$RELEASE_VERSION/verifyica-pipeliner.tar.gz"
curl -s -L -o verifyica-pipeliner.tar.gz "$FILE_URL"
if [[ $? -ne 0 ]]; then
  echo "Error: Failed to download file from $FILE_URL."
  exit 1
fi

# Extract the tar.gz file
tar -xzf verifyica-pipeliner.tar.gz
if [[ $? -ne 0 ]]; then
  echo "Error: Failed to extract the downloaded file."
  exit 1
fi

# Create .pipeliner directory in the current directory
mkdir -p "$CURRENT_DIR/.pipeliner"

# Copy the required files to the current directory
cp pipeliner "$CURRENT_DIR/"
cp .pipeliner/verifyica-pipeliner.jar "$CURRENT_DIR/.pipeliner/"

# Cleanup and return to the current directory explicitly on success
cleanup
