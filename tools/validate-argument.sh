#!/bin/bash

basename=$(basename "$0")
arg1=$1

if [ "$#" -ne 1 ]; then
    echo "[Error] $basename | argument is required"
    exit 1
fi

if [ "$arg1" != "" ]; then
    exit 0
else
    echo "[Error] $basename | argument is blank"
    exit 1
fi