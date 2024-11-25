#!/bin/bash

echo "[TEST] $0"

if [ "$#" -ne 2 ]; then
    echo "[FAIL] $0 | argument count [$#] != [2]"
    exit 1
fi

arg1=$1
arg2=$2

if [ ! -f "$arg1" ]; then
    echo "[FAIL] $0 | file [$arg1] not found"
    exit 1
fi

FILE_CONTENT=$(<"$arg1")

if [ "$FILE_CONTENT" == "$arg2" ]; then
    echo "[PASS] $0 | file [$arg1] contents == [$arg2]"
    exit 0
else
    echo "[FAIL] $0 | file [$arg1] contents !== [$arg2]"
    exit 1
fi