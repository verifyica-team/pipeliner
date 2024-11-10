#!/bin/bash

if [ $# -eq 0 ]; then
    echo "Usage: $(basename "$0") <pipeline.yaml> [pipeline2.yaml...]"
    exit 1
fi

JAR_PATH="target/verifyica-pipeline-0.0.1.jar"

if [ ! -f "$JAR_PATH" ]; then
    JAR_PATH="verifyica-pipeline-0.0.1.jar"
fi

if [ ! -f "$JAR_PATH" ]; then
     JAR_PATH=".verifyica/verifyica-pipeline-0.0.1.jar"
fi

if [ ! -f "$JAR_PATH" ]; then
     JAR_PATH=".vp/verifyica-pipeline-0.0.1.jar"
fi

if [ ! -f "$JAR_PATH" ]; then
    echo "verifyica-pipeline-0.0.1.jar not found"
    exit 1
fi

java -jar "${JAR_PATH}" "$@"
