#!/bin/bash

echo "[TEST] $0"

if [ "$#" -ne 1 ]; then
    echo "[FAIL] $0 | expected 1 argument"
    exit 1
fi

# Assign arguments to variables
arg1=$1

# Compare the arguments
if [ "$arg1" != "" ]; then
    echo "[PASS] $0 | [$arg1] is not blank"
    exit 0
else
    echo "[FAIL] $0 | [$arg1] is blank"
    exit 1
fi