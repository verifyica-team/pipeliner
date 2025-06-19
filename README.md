### This README.md and other documentation is specific to a branch / release, and may reference unreleased development features.

---

[![Build](https://github.com/verifyica-team/CLI/actions/workflows/build.yaml/badge.svg)](https://github.com/verifyica-team/CLI/actions/workflows/build.yaml) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/b908266740664e8c9985be70babe9262)](https://app.codacy.com/gh/verifyica-team/CLI/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade) <a href="#"><img src="https://img.shields.io/badge/JDK%20compatibility-8+-blue.svg" alt="java 8+"></a> <a href="#"><img src="https://img.shields.io/badge/license-Apache%202.0-blue.svg" alt="Apache 2.0"></a>

# Verifyica Pipeliner

Verifyica Pipeliner allows you to define and run a local pipeline using a syntax ***similar*** to GitHub actions.

**Pipeliner is not designed to be 100% GitHub Action compatible.**

## Why ?

There are many scenarios where you need to perform various processing steps during development, system maintenance, or general workflows.

Example:

- patching source files
- setting up a test environment
- building a custom zip or tar.gz file
- common local CI/CD workflows
- system maintenance tasks

#### Maven & Gradle

Maven and Gradle ***can*** and ***are*** useful in development environment for Java developers.

Developers in other languages use non-Java language specific tools.

Maven and Gradle are not the best tools for system administrators.

#### Bash

A common go to is to use a Bash script(s). Pipeliner uses them for testing.

Implementing logging, exit code checking, etc. is commonly implemented incorrectly and inconsistently.

#### Pipeliner

Pipeliner allows a declarative definition of a pipeline(s) using YAML. The exit code of each command is checked, and the pipeline is aborted if a command fails.

Designing a pipeline(s) using Bash command(s), small purpose-built applications, or small purpose-built shell scripts allows for easier development, reuse, testing, etc.

## Requirements

- Linux
- Java 8 or later

## Installation

### Installation Script

Download and run the installation script.

Install the latest version:

```bash
curl -s https://raw.githubusercontent.com/verifyica-team/pipeliner/main/install.sh | bash
```

Install a specific version:

```bash
curl -s https://raw.githubusercontent.com/verifyica-team/pipeliner/main/install.sh | bash -s -- 1.0.0-RC2
```

**Notes**

- The installation script requires
  - curl
  - tar
  - jq

### Manual Installation

Download the zip or tar.gz file from the [releases]

Zip:

```bash
cd <PROJECT DIRECTORY>
unzip verifyica-pipeliner.zip
./pipeliner --info
```

Tarball:

```bash
cd <PROJECT DIRECTORY>
tar -xf verifyica-pipeliner.tar.gz
./pipeliner --info
```

## Variables

A pipeline, job, or step can define variables using a `with` map.

- A variable id must match the regular expression `^[a-zA-Z_]([a-zA-Z0-9-_.]*[a-zA-Z0-9_])?$`


- Variables are case-sensitive.


- Variables are globally scoped, but do not exist until defined.


- Variables can be used in `run` statements as well as in `shell` and `working-directory` values.


- If a variable is not defined, by default the unresolved variable string is returned.

## Environment Variables

A pipeline, job, or step can define environment variables using a `env` map.

- Environment variables are globally scoped, but do not exist until defined.

- Environment variables are case-sensitive.

## Pipeliner Output

Pipeliner uses various prefixes to indicate output.

### Info

- Information output is prefixed with `@info`

### Error

- Error output is prefixed with `@error`

### Warning

- Warning output is prefixed with `@warning`

## Pipeline

- Pipelines are prefixed with `@pipeline`

## Job

- Jobs are prefixed with `@job`

## Step

- Steps are prefixed with `@step`

### Command

- Commands are prefixed with `@command`

### Command Output

- Command output prefixed with `@output`

# Pipeliner IPC

For more complex scenarios, where you need to pass multiple variables and capture multiple variables, you can use Pipeliner IPC.

Pipeliner creates two temporary files. The filenames are passed to the application as environment variables.

- `PIPELINER_IPC_IN`
- `PIPELINER_IPC_OUT`

#### File Format

For a variable name and value...

- `test_variable` with a value of `test.value`

The `PIPELINER_IPC_IN` and/or `PIPELINER_IPC_OUT` file contents would be...

- `dGVzdF92YXJpYWJsZQ== dGVzdC52YWx1ZQ==`

For empty variables...

- `dGVzdF92YXJpYWJsZQ==`

**Notes**

- A variable must match the regular expression `^[a-zA-Z_][a-zA-Z0-9_]*$`


- The IPC file uses a line-based format, with each line containing `BASE64(name) BASE64(value)`


- IPC file lines that are empty or start with # after trimming are ignored

### PIPELINER_IPC_IN

Environment variable that contains the full path name of the IPC file passed from Pipeliner to your application.

Read the properties file to get variables.

### PIPELINER_IPC_OUT

Environment variable that contains the full path name of the IPC file your application should write to capture output variables.

Write to this file to capture output variables

### Example

Example IPC pipelines [examples/ipc](examples/ipc)

Functional examples for the following languages...
  - Bash script
  - C#
  - Go
  - Groovy
  - Java
  - JavaScript
  - Kotlin
  - Lua
  - Nim
  - Python
  - Rust
  - Zig (**currently broken**)

**Notes**

- The IPC examples are disabled by default since tools used to build/run them may not be installed


- The IPC examples are functional, but should not be considered production quality code

## Pipeline YAML definition

Basic example:

```yaml
pipeline:
  name: hello-world-pipeline # Optional but recommended, Any text
  description: A simple hello world pipeline # Optional, any text
  enabled: true # Optional, default is true
  jobs:
    - name: hello-world-job # Optional but recommended, Any text
      description: A simple hello world job # Optional, any text
      enabled: true # Optional, default is true
      steps:
        - name: hello-world-step-1 # Optional but recommended, Any text
          description: A simple hello world step # Optional, any text
          enabled: true # Optional, default is true
          run: echo "Hello World"
        - name: hello-world-step-2 # Optional but recommended, Any text
          description: A simple hello world step # Optional, any text
          enabled: true # Optional, default is true
          run: echo \"Hello World\"
```

Other examples:

- example pipelines [examples](examples)
- test pipelines [tests](tests)

Advanced examples:

- builds the packages [package.yaml](package.yaml)
- builds the release [release.yaml](release.yaml)
# Building

Java 8 or later is required to build. The code target is Java 8.

```bash
git clone https://github.com/verifyica-team/CLI
cd CLI
./mvnw clean verify
```

**Notes**

- Packaging requires `zip` and `tar` to be installed

## Packaging

The `OUTPUT` directory will contain the release packages and associated SHA1 checksum files.

- `install.sh`
- `install.sh.sha1`
- `verifiyica-piperliner.zip`
- `verifiyica-piperliner.zip.sha1`
- `verifyica-piperliner.tar.gz`
- `verifyica-piperliner.tar.gz.sha1`

# Contributing

See [Contributing](CONTRIBUTING.md) for details.

# License

Apache License 2.0, see [LICENSE](LICENSE).

# DCO

See [DCO](DCO.md) for details.

# Support

If you like the project, please give it a star.

Stars are very much appreciated. It helps others find the project.

---

![YourKit logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](https://www.yourkit.com/) supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications.

YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>,
<a href="https://www.yourkit.com/dotnet-profiler/">YourKit .NET Profiler</a>,
and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.

---

Copyright (C) Pipeliner project authors and contributors