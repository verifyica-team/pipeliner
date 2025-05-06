### This README.md and other documentation is specific to a branch / release, and may reference unreleased development features.

---

[![Build](https://github.com/verifyica-team/pipeliner/actions/workflows/build.yaml/badge.svg)](https://github.com/verifyica-team/pipeliner/actions/workflows/build.yaml) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/b908266740664e8c9985be70babe9262)](https://app.codacy.com/gh/verifyica-team/pipeliner/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade) <a href="#"><img src="https://img.shields.io/badge/JDK%20compatibility-8+-blue.svg" alt="java 8+"></a> <a href="#"><img src="https://img.shields.io/badge/license-Apache%202.0-blue.svg" alt="Apache 2.0"></a>

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

Pipeliner allows a declarative definition of a pipeline(s) using YAML.

Designing a pipeline(s) using Bash command(s), small purpose-built applications, or small purpose-built shell scripts allows for easier development, reuse, testing, etc.

## Requirements

Java 8 or later is required to run Pipeliner.

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
./pipeliner .pipeliner/hello-world-3.yaml
```

Tarball:

```bash
cd <PROJECT DIRECTORY>
tar -xf verifyica-pipeliner.tar.gz
./pipeliner --info
./pipeliner .pipeliner/hello-world-3.yaml
```

## Pipeline YAML definition

Basic example:

```yaml
pipeline:
  name: hello-world-pipeline
  jobs:
    - name: hello-world-job
      enabled: true
      steps:
        - name: hello-world-step-1
          enabled: true
          run: echo "Hello World"
        - name: hello-world-step-2
          enabled: true
          run: echo \"Hello World\"
```

Other examples:

- example pipelines [examples](examples)
- test pipelines [tests](tests)

Advanced examples:

- builds the packages [package.yaml](package.yaml)
- builds the release [release.yaml](release.yaml)

## Pipeliner Output

Pipeliner uses various prefixes to indicate output.

### Info

- Information output is prefixed with `@info`

### Error

- Error output is prefixed with `@error`

### Trace

- Trace output is prefixed with `@trace`

### Pipeline, Job, Step

The pipeline, jobs, and steps output is prefixed with `@<IDENTIFIER>`

- `@pipeline`
- `@job`
- `@step`

**Notes**

- When a pipeline, jobs, and steps complete, an exit code and execution time in milliseconds is included


- The all jobs and all steps generate output, regardless if they are enabled, disabled, or skipped

Starting output ...

```shell
@info Verifyica Pipeliner 1.0.0-RC2 (https://github.com/verifyica-team/pipeliner)
@info filename=[examples/hello-world-3.yaml]
@pipeline name=[hello-world-pipeline] status=[RUNNING]
@job name=[hello-world-job] status=[RUNNING]
@step name=[hello-world-step-1] status=[RUNNING]
...
```

Finished output ...

```shell
...
@step name=[hello-world-step-2] status=[SUCCESS] exit-code=[0] ms=[11]
@job name=[hello-world-job] status=[SUCCESS] exit-code=[0] ms=[60]
@pipeline name=[hello-world-pipeline] status=[SUCCESS] exit-code=[0] ms=[61]
```

### Command

- The command executed is prefixed with `$ `


- The command output is prefixed with `> `

### Example Output

```shell
user@machine> ./pipeliner examples/hello-world-1.yaml
```

```shell
@info Verifyica Pipeliner 1.0.0-RC2-post (https://github.com/verifyica-team/pipeliner)
@pipeline status=[running]
@job status=[running]
@step status=[running]
$ echo "Hello World"
> Hello World
@step status=[success] exit-code=[0] ms=[11]
@job status=[success] exit-code=[0] ms=[16]
@pipeline status=[success] exit-code=[0] ms=[17]
```

## Variables

A pipeline, job, or step can define variables using a `with` map.

- Variables can be used in `run` statements as well as a `working-directory` value.


- A variable id must match the regular expression `^[a-zA-Z_]([a-zA-Z0-9-_]*[a-zA-Z0-9_])?$`

If a variable is not defined, by default the variable value is resolved to an empty string.

To require a variable, you can use the `required:` modifier

- `echo "Hello ${{ required:name }}`

### Example

```yaml
pipeline:
  name: Hello World Pipeline
  id: hello-world-pipeline
  enabled: true
  with:
    variable_1: pipeline.foo
    variable_2: pipeline.bar
  jobs:
    - name: Hello World Job
      id: hello-world-job
      enabled: true
      with:
        variable_1: ${{ hello-world-pipeline.variable_1 }}_job.foo
        variable_2: ${{ hello-world-pipeline.variable_2 }}_job.bar
      steps:
        - name: Hello World Step
          id: hello-world-step
          enabled: true
          with:
            variable_1: ${{ hello-world-job.variable_1 }}_step.foo
            variable_2: ${{ hello-world-job.variable_2 }}_step.bar
          run: |
            echo globally scoped variables = ${{ variable_1 }} ${{ variable_2 }}
            echo step scoped variables = ${{ hello-world-step.variable_1 }} ${{ hello-world-step.variable_2 }}
            echo job scoped variables = ${{ hello-world-job.hello-world-step.variable_1 }} ${{ hello-world-job.hello-world-step.variable_2 }}
            echo pipeline scoped variables = ${{ hello-world-pipeline.hello-world-job.hello-world-step.variable_1 }} ${{ hello-world-pipeline.hello-world-job.hello-world-step.variable_2 }}
```

**NOTES**

- Variables are case-sensitive


- Scoped variables can be referenced in a `run:` command using the `id` of the pipeline, job, or step (or a combination)
  - `${{ hello-world-pipeline.variable_1 }}


- An `id` must match the regular expression `^[a-zA-Z_]([a-zA-Z0-9-_]*[a-zA-Z0-9_])?$`


- Variable replacement is not recursive (nested)

### Command

```shell
./pipeliner examples/variables-3.yaml
```

### Output

```shell
@info Verifyica Pipeliner 1.0.0-RC2-post (https://github.com/verifyica-team/pipeliner)
@pipeline id=[hello-world-pipeline] name=[Hello World Pipeline] status=[running]
@job id=[hello-world-job] name=[Hello World Job] status=[running]
@step id=[hello-world-step] name=[Hello World Step] status=[running]
$ echo globally scoped variables = ${{ variable_1 }} ${{ variable_2 }}
> globally scope properties = pipeline.foo_job.foo_step.foo pipeline.bar_job.bar_step.bar
$ echo step scoped variables = ${{ hello-world-step.variable_1 }} ${{ hello-world-step.variable_2 }}
> step scoped variables = pipeline.foo_job.foo_step.foo pipeline.bar_job.bar_step.bar
$ echo job scoped variables = ${{ hello-world-job.hello-world-step.variable_1 }} ${{ hello-world-job.hello-world-step.variable_2 }}
> job scoped variables = pipeline.foo_job.foo_step.foo pipeline.bar_job.bar_step.bar
$ echo pipeline scoped variables = ${{ hello-world-pipeline.hello-world-job.hello-world-step.variable_1 }} ${{ hello-world-pipeline.hello-world-job.hello-world-step.variable_2 }}
> pipeline scoped variables = pipeline.foo_job.foo_step.foo pipeline.bar_job.bar_step.bar
@step id=[hello-world-step] name=[Hello World Step] status=[success] exit-code=[0] ms=[18]
@job id=[hello-world-job] name=[Hello World Job] status=[success] exit-code=[0] ms=[22]
@pipeline id=[hello-world-pipeline] name=[Hello World Pipeline] status=[success] exit-code=[0] ms=[23]
```

## Capture Variables

The output of a step can be captured as a variable to be used in subsequent jobs and steps.

Examples...

- [test-capture-append.yaml](https://github.com/verifyica-team/pipeliner/blob/main/tests/test-capture-append.yaml)
- [test-capture-overwrite.yaml](https://github.com/verifyica-team/pipeliner/blob/main/tests/test-capture-overwrite.yaml)

## Environment Variables

A pipeline, job, or step can define environment variables using an `env` map.

**Notes**

- Environment variables are **not** scoped
- Environment variables can't be captured

## Other Examples

The example and test pipelines provide other examples...

- example pipelines [examples](examples)
- test pipelines [tests](tests)

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


- The IPC file use a `BASE64(name) BASE64(value)` format


- IPC file lines that, after being trimmed, that are empty or start with `#` should be ignored

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

**Notes**

- The IPC examples are disabled by default since tools used to build/run them may not be installed


- The IPC examples are functional, but should not be considered production quality code

## Executing

```shell
./pipeliner <YOUR PIPELINE YAML FILE>
```

# Pipeliner Extensions

Pipeliner allows you to create extensions to add additional functionality.

An extension is a `zip` or `tar.gz` file containing code and a shell script.

Pipeline looks for the following shell scripts, in order of preference:

- `run.sh`
- `execute.sh`
- `entrypoint.sh`
- `ENTRYPOINT`

Extensions must use Pipeliner IPC to get and capture variables.

## Example

Example pipeline that create an extension and uses it.

- Example pipeline using `--extension` [examples/extensions.yaml](examples/extension.yaml)

```yaml
pipeline:
  name: Extensions Pipeline
  id: extensions-pipeline
  enabled: true
  jobs:
    - name: Extensions Job
      id: extensions-job
      steps:
        - name: Create Example Extension
          id: create-example-extension
          run: |
            mkdir -p TMP
            rm -Rf TMP/*
            echo "echo executing example extension" > TMP/execute.sh
            cd TMP && zip -qr tmp.zip *
        - name: Execute Example Extension 1
          id: execute-example-extension-1
          run: --extension TMP/tmp.zip
        - name: Execute Example Extension 2
          id: execute-example-extension-2
          run: --extension TMP/tmp.zip
        - name: Execute Remote Extension
          id: execute-remote-extension
          enabled: false
          # Example using extension downloaded from an HTTP server
          run: --extension http://<YOUR_SERVER>/tmp.zip
```

**Notes**

- Remote extensions are referenced using a URL (e.g. `http://<YOUR_SERVER>/<EXTENSION>`)
  - The URL must be accessible by pipeliner
  - If the URL is HTTPS, the server certificate must be trusted by the JVM


- Local extensions are referenced using the file path


- Use an SHA-1, SHA-256, or SHA-512 (preferred) checksum to verify the integrity of the extension
  - `run: --extension <EXTENSION_ARCHIVE> <EXTENSION_SHA-1_CHECKSUM>`
  - `run: --extension <EXTENSION_ARCHIVE> <EXTENSION_SHA-256_CHECKSUM>`
  - `run: --extension <EXTENSION_ARCHIVE> <EXTENSION_SHA-512_CHECKSUM>`

- Extensions are cached during a pipeliner execution
  - nested pipeliner executions will not use the cached extensions


- BASIC HTTP authentication can be enabled for password protected remote extensions
  - set via variables
  - `pipeliner_extension_http_username`
  - `pipeliner_extension_http_password`

# Pipeliner Options

Pipeliner options...

- `--information` or `--info`
  - shows version information


- `--version`
  - emits only the version number


- `--timestamps` or `-ts`
  - include timestamps in output


- `--trace` or `-t`
  - enables trace output


- `--quiet` or `-q`
  - only emits commands, command output, and errors


- `--quieter` or `-qq`
  - only emits command output, and errors


- `--with <variable name>=<value>` or `-V <variable name>=<value>`
  - sets a variable
  - repeatable


- `--with-file <filename>`
  - loads variables from Java properties file
  - property names must be variable name compliant
  - repeatable


- `--env <environment variable>=<value>` or `-E <environment variable>=<value>`
  - sets an environment variable
  - repeatable


- `--validate`
  - performs basic validation of (a) pipeline file(s)


- `--help` or `-h`
  - shows help

Optionally, some options can be set using environment variables:

- `PIPELINER_TIMESTAMPS=true`
- `PIPELINER_TRACE=true`
- `PIPELINER_QUIET=true`
- `PIPELINER_QUIETER`=true`

# Building

Java 8 or later is required to build. The code target is Java 8.

```bash
git clone https://github.com/verifyica-team/pipeliner
cd pipeliner
./mvnw clean verify
```

**Notes**

- Packaging requires `zip` and `tar` to be installed

# Debugging

For debugging, use the option `--trace`

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

Copyright (C) 2024-present Pipeliner project authors and contributors