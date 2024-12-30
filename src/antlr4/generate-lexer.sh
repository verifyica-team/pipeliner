#!/bin/bash

# Check if antlr4 is installed
if ! command -v antlr4 &> /dev/null; then
    echo "antlr4 is not installed"
    exit 1
fi

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo "git is not installed"
    exit 1
fi

# Get the root of the project
PROJECT_ROOT=$(git rev-parse --show-toplevel)

# Generate the lexer and parser code
antlr4 \
  -package org.verifyica.pipeliner.tokenizer.lexer \
  "$PROJECT_ROOT/src/antlr4/String.grammar" \
  -o "$PROJECT_ROOT/src/main/java/org/verifyica/pipeliner/tokenizer/lexer"

# Remove the generated files that are not used
rm -Rf "$PROJECT_ROOT/src/main/java/org/verifyica/pipeliner/tokenizer/lexer/StringParser.java"
rm -Rf "$PROJECT_ROOT/src/main/java/org/verifyica/pipeliner/tokenizer/lexer/StringBaseListener.java"
rm -Rf "$PROJECT_ROOT/src/main/java/org/verifyica/pipeliner/tokenizer/lexer/StringListener.java"
rm -Rf "$PROJECT_ROOT/src/main/java/org/verifyica/pipeliner/tokenizer/lexer/String.tokens"
rm -Rf "$PROJECT_ROOT/src/main/java/org/verifyica/pipeliner/tokenizer/lexer/String.interp"
rm -Rf "$PROJECT_ROOT/src/main/java/org/verifyica/pipeliner/tokenizer/lexer/StringLexer.tokens"
rm -Rf "$PROJECT_ROOT/src/main/java/org/verifyica/pipeliner/tokenizer/lexer/StringLexer.interp"
