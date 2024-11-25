#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "[FAIL] $0 | argument count [$#] != [2]"
    exit 1
fi

arg1=$1
arg2=$2

echo "[TEST] $0 | [$arg1] == [$arg2]"

if [ "$arg1" == "$arg2" ]; then
    echo "[FAIL] $0 | [$arg1] == [$arg2]"
    exit 1
else
    echo "[PASS] $0 | [$arg1] != [$arg2]"
    exit 0
fi