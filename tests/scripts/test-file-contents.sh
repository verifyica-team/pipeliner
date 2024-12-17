#!/bin/bash

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