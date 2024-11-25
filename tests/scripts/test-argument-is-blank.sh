#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "[FAIL] $0 | expected 1 argument"
    exit 1
fi

arg1=$1

echo "[TEST] $0 | [$arg1] is blank"

if [ "$arg1" == "" ]; then
    echo "[PASS] $0 | [$arg1] is blank"
    exit 0
else
    echo "[FAIL] $0 | [$arg1] is not blank"
    exit 1
fi