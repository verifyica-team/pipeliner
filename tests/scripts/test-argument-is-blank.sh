#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "[FAIL] $basename | expected 1 argument"
    exit 1
fi

basename=$(basename "$0")
arg1=$1

echo "[TEST] $basename | [$arg1] is blank"

if [ "$arg1" == "" ]; then
    echo "[PASS] $basename | [$arg1] is blank"
    exit 0
else
    echo "[FAIL] $basename | [$arg1] is not blank"
    exit 1
fi