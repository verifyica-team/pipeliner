### This README.md and other documentation is specific to a branch / release, and may reference unreleased development features.

---

[![Build](https://github.com/verifyica-team/pipeliner/actions/workflows/build.yaml/badge.svg)](https://github.com/verifyica-team/pipeliner/actions/workflows/build.yaml) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/b908266740664e8c9985be70babe9262)](https://app.codacy.com/gh/verifyica-team/pipeliner/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade) <a href="#"><img src="https://img.shields.io/badge/JDK%20compatibility-11+-blue.svg" alt="java 11+"></a> <a href="#"><img src="https://img.shields.io/badge/license-Apache%202.0-blue.svg" alt="Apache 2.0"></a>

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
- Java 11 or later

## Installation

### Installation Script

Download and run the installation script.

Install the latest version:

```bash
curl -s https://raw.githubusercontent.com/verifyica-team/pipeliner/main/install.sh | bash
```

Install a specific version:

```bash
curl -s https://raw.githubusercontent.com/verifyica-team/pipeliner/main/install.sh | bash -s -- <release>
```

**Notes**

- The installation script requires
  - curl
  - tar
  - jq

### Manual Installation

Download the zip or tar.gz file from the [releases](https://github.com/verifyica-team/pipeliner/releases)

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

# Usage

Usage is similar to GitHub actions, but not 100% compatible.

The easiest way to get started is to look at the [QUICK_START](QUICK_START.md) guide.

# Building

Java 11 or later is required to build.

```bash
git clone https://github.com/verifyica-team/pipeliner
cd pipeliner
./mvnw clean verify
```

## Packaging

The `OUTPUT` directory will contain the release packages and associated SHA1 checksum files.

- `install.sh`
- `install.sh.sha1`
- `verifiyica-piperliner.zip`
- `verifiyica-piperliner.zip.sha1`
- `verifyica-piperliner.tar.gz`
- `verifyica-piperliner.tar.gz.sha1`

**Notes**

- Packaging requires `zip` and `tar` to be installed

# Design

See [DESIGN](DESIGN.md) for details on the design of Pipeliner.

# Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for details.

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
