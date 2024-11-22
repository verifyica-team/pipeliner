#!/bin/bash

echo "[TEST] $0"

if [ "$#" -ne 2 ]; then
    echo "[FAIL] $0 | argument count [$#] != [2]"
    exit 1
fi

arg1=$1
arg2=$2

if [ "$arg1" == "$arg2" ]; then
    echo "[PASS] $0 | [$arg1] == [$arg2]"
    exit 0
else
    echo "[FAIL] $0 | [$arg1] != [$arg2]"
    exit 1
fi