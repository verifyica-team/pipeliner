#!/bin/bash

if [ $# -eq 0 ]; then
    echo "Usage: $(basename "$0") <pipeline.yaml> [pipeline2.yaml...]"
    exit 1
fi

JAR_VERSION=0.0.1
JAR_PATH="target/verifyica-pipeline-${JAR_VERSION}.jar"
if [ ! -f "$JAR_PATH" ]; then
    JAR_PATH="verifyica-pipeline-${JAR_VERSION}.jar"
fi

java -jar "${JAR_PATH}" "$@"