#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "[FAIL] $basename | argument count [$#] != [2]"
    exit 1
fi

basename=$(basename $0)
arg1=$1
arg2=$2

echo "[TEST] $basename | [$arg1] == [$arg2]"

if [ "$arg1" == "$arg2" ]; then
    echo "[FAIL] $basename | [$arg1] == [$arg2]"
    exit 1
else
    echo "[PASS] $basename | [$arg1] != [$arg2]"
    exit 0
fi