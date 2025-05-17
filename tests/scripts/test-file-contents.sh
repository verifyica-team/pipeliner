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

if [ "$#" -ne 2 ]; then
    echo "[FAIL] $basename expected 2 arguments"
    exit 1
fi

basename=$(basename "$0")
arg1=$1
arg2=$2

echo "[TEST] $basename file [$arg1] contents == [$arg2]"

if [ ! -f "$arg1" ]; then
    echo "[FAIL] $basename file [$arg1] not found"
    exit 1
fi

FILE_CONTENT=$(<"$arg1")

if [ "$FILE_CONTENT" == "$arg2" ]; then
    echo "[PASS] $basename file [$arg1] contents == [$arg2]"
    exit 0
else
    echo "[FAIL] $basename file [$arg1] contents !== [$arg2]"
    exit 1
fi