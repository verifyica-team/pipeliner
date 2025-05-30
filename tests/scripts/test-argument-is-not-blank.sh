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

if [ "$#" -ne 1 ]; then
    echo "[FAIL] $basename expected 1 argument"
    exit 1
fi

basename=$(basename "$0")
arg1=$1

echo "[TEST] $basename [$arg1] is not blank"

if [ "$arg1" != "" ]; then
    echo "[PASS] $basename [$arg1] is not blank"
    exit 0
else
    echo "[FAIL] $basename [$arg1] is blank"
    exit 1
fi