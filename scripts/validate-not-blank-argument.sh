#!/bin/bash

echo "[TEST] $0"

if [ "$#" -ne 1 ]; then
    echo "[FAIL] $0 | expected 1 argument"
    exit 1
fi

# Assign arguments to variables
arg1=$1
arg2=$2

# Compare the arguments
if [ "$arg1" != "" ]; then
    echo "[PASS] $0 | argument is valid"
    exit 0
else
    echo "[FAIL] $0 | arguments is empty"
    exit 1
fi