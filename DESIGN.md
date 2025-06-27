# Design

Pipeliner is designed similar to other interpreter-based languages.

## Step 1

Parse and validate the command line arguments.

## Step 2

Parse the pipeline YAML file into a node tree structure.

## Step 3

Process the node tree structure, generating a list of instructions.

## Step 4

Loop through the list of instructions, executing each instruction.

## Why this design?

Flexibility and extensibility.

This design allows for a decoupling of the source pipeline format and the execution engine, making it easier to add new features and functionality in the future.

For example, an instruction or directive can be added that uses other instructions.

### Example

The `--extension` directive allows for passing an optional checksum of the file.

The `Extension` instruction functionality to validate the checksum is implemented by creating a `ShaChecksum` instruction and executing it.

---

Copyright (C) Pipeliner project authors and contributors