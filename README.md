### This README.md and other documentation is specific to a branch / release

### It may reference reference unreleased development features or changes

### Consult the documentation for the specific branch / release

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
curl -s https://raw.githubusercontent.com/verifyica-team/pipeliner/main/install.sh | bash -s -- 0.29.0
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
./pipeliner .pipeliner/hello-world-pipeline.yaml
```

Tarball:

```bash
cd <PROJECT DIRECTORY>
tar -xf verifyica-pipeliner.tar.gz
./pipeliner --info
./pipeliner .pipeliner/hello-world-pipeline.yaml
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
@info Verifyica Pipeliner 0.29.0 (https://github.com/verifyica-team/pipeliner)
@info filename=[examples/hello-world-pipeline.yaml]
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
user@machine> ./pipeliner examples/hello-world-pipeline.yaml
```

```shell
@info Verifyica Pipeliner 0.29.0 (https://github.com/verifyica-team/pipeliner)
@info filename=[examples/hello-world-pipeline.yaml]
@pipeline name=[hello-world-pipeline] status=[RUNNING]
@job name=[hello-world-job] status=[RUNNING]
@step name=[hello-world-step-1] status=[RUNNING]
$ echo "Hello World"
> Hello World
@step name=[hello-world-step-1] status=[SUCCESS] exit-code=[0] ms=[43]
@step name=[hello-world-step-2] status=[RUNNING]
$ echo \"Hello World\"
> "Hello World"
@step name=[hello-world-step-2] status=[SUCCESS] exit-code=[0] ms=[11]
@job name=[hello-world-job] status=[SUCCESS] exit-code=[0] ms=[60]
@pipeline name=[hello-world-pipeline] status=[SUCCESS] exit-code=[0] ms=[61]
```

## Variables

A pipeline, job, or step must have a `name`.

- `name` should is can be any string value

A pipeline, job, or step can define variables using a `with` map on the element

These variables can be used in `run` statements as well as a `working-directory` value.

- **Variables are case-insensitive**

- Variables can be defined in a pipeline, job, or step, but are not scoped

- A variable name must match the regular expression `^[a-zA-Z0-9_][a-zA-Z0-9_]*[a-zA-Z0-9_]$`

- If a variable is not defined, then the variable is replaced with an empty string.

- The last value of a variable is used

- Use a prefix as part of the variable to "scope" the variable is recommended
  - `pipeline__variable_1`
  - `pipeline__job_1_variable_1`
  - `pipeline__job_2_variable_1`

### Example

```yaml
pipeline:
  name: properties-1
  enabled: true
  with:
    variable_1: pipeline.foo
    variable_2: pipeline.bar
    pipeline.variable_1: pipeline.foo
    pipeline.variable_2: pipeline.bar
  jobs:
    - name: properties-1-job
      enabled: true
      with:
        variable_1: job.foo
        variable_2: job.bar
        job_variable_1: job.foo
        job_variable_2: job.bar
      steps:
        - name: properties-1-step
          enabled: true
          with:
            variable_1: step.foo
            variable_2: step.bar
            step_variable_1: step.foo
            step_variable_2: step.bar
          run: |
            # echo's the last value of variable_1 and variable_2
            echo globally scoped properties = ${{ variable_1 }} ${{ variable_2 }}
            # echo's the last value of pipeline.variable_1 and pipeline.variable_2
            echo pipeline scoped properties = ${{ pipeline.variable_1 }} ${{ pipeline.variable_2 }}
            # echo's the last value of job_variable_1 and job_variable_2
            echo job scoped properties = ${{ job_variable_1 }} ${{ job_variable_2 }}
            # echo's the last value of step_variable_1 and step_variable_2
            echo step scoped properties = ${{ step_variable_1 }} ${{ step_variable_2 }}
```

**NOTES**

- Variable replacement is recursive
  - a variable can be defined using another variable or environment variable
  - nested variables are not supported (e.g. `${{ variable_${{ variable_1 }} }}`)

- You can set a variable to prevent a step `run` command from echoing resolved variable values
  - `pipeliner_mask_variables: true`
  - great for security sensitive variables


- You can set a variable to prevent the step `run` command from echoing to the console
  - `pipeliner_mask_commands: true`
  - overrides `pipeliner_mask_variables`

### Command

```shell
./pipeliner examples/variables.yaml
```

### Output

```shell
@info Verifyica Pipeliner 0.29.0-post (https://github.com/verifyica-team/pipeliner)
@info filename [variables.yaml]
@pipeline name=[properties-1] status=[RUNNING]
@job name=[properties-1-job] status=[RUNNING]
@step name=[properties-1-step] status=[RUNNING]
$ echo globally scoped properties = step.foo step.bar
> globally scoped properties = step.foo step.bar
$ echo pipeline scoped properties = pipeline.foo pipeline.bar
> pipeline scoped properties = pipeline.foo pipeline.bar
$ echo job scoped properties = job.foo job.bar
> job scoped properties = job.foo job.bar
$ echo step scoped properties = step.foo step.bar
> step scoped properties = step.foo step.bar
@step name=[properties-1-step] status=[SUCCESS] exit-code=[0] ms=[430]
@job name=[properties-1-job] status=[SUCCESS] exit-code=[0] ms=[435]
@pipeline name=[properties-1] status=[SUCCESS] exit-code=[0] ms=[436
```

## Capture Variables

The output of a step can be captured as a variable to be used in subsequent jobs and steps.

Examples...

- [test-capture-append.yaml](https://github.com/verifyica-team/pipeliner/blob/main/tests/test-capture-append.yaml)
- [test-capture-overwrite.yaml](https://github.com/verifyica-team/pipeliner/blob/main/tests/test-capture-overwrite.yaml)

## Environment Variables

A pipeline, job, or step can define environment variables using an `env` map.

**Environment variables are case sensitive**

**Notes**

- Environment variables are flat


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

For a variable and value...

- `test_variable` with a value of `test_value`

The `PIPELINER_IPC_IN` and/or `PIPELINER_IPC_OUT` file contents would be...

- `test_variable=dGVzdC52YWx1ZQ==`

For empty properties...

- `test_variable=`

**Notes**

- Each variable is on a separate line


- Lines (after being trimmed) and starting with `#` are ignored

- Empty lines are ignored

### PIPELINER_IPC_IN

Environment variable that contains the full path name of the properties file passed from Pipeliner to your application.

Read the properties file to get properties.

### PIPELINER_IPC_OUT

Environment variable that contains the full path name of the properties file your application should write to capture properties.

Write to this properties file to capture properties.

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

An extension is a `zip` or `tar.gz` file containing code and a shell script named `execute.sh` or `run.sh`.

- `execute.sh` takes precedence over `run.sh` if both files exist

Extensions must use Pipeliner IPC to get and capture variables.

## Example

Example pipeline that create an extension and uses it.

- Example pipeline using `--extension` [examples/extensions.yaml](examples/extensions.yaml)

```yaml
pipeline:
  name: Extensions Pipeline
  enabled: true
  jobs:
    - name: Extensions Job
      steps:
        - name: Create Example Extension
          run: |
            mkdir -p TMP
            rm -Rf TMP/*
            echo "echo executing example extension" > TMP/execute.sh
            cd TMP && zip -qr tmp.zip *
        - name: Execute Example Extension 1
          run: --extension TMP/tmp.zip
        - name: Execute Example Extension 2
          run: --extension TMP/tmp.zip
        - name: Execute Remote Extension
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
  - `pipeliner.extension.http.username`
  - `pipeliner.extension.http.password`

# Pipeliner Options

Pipeliner options...

- `--information` or `--info`
  - shows information


- `--version` or `--ver`
  - emits the version


- `--timestamps`
  - include timestamps in output


- `--trace`
  - include trace messages in output


- `--minimal` or `--min`
  - only emits commands, command output, and errors in output


- `--with <variable>=<value>` or `-P <variable>=<value>`
  - sets a variable
  - repeatable


- `--with-file <filename>`
  - loads and set variables from a file
  - repeatable


- `--env <environment variable>=<value>` or `-E <environment variable>=<value>`
  - sets an environment variable
  - repeatable


- `--validate` or `--val`
  - performs basic validation of (a) pipeline file(s)


- `--help`
  - shows help

Optionally, some options can be set using environment variables:

- `PIPELINER_TIMESTAMPS=true`
- `PIPELINER_TRACE=true`
- `PIPELINER_MINIMAL=true`

**Notes**

- Command line options override environment variables

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

For basic debugging, use the option `--trace`

For code level debugging, set the environment variable `PIPELINER_LOG_LEVEL=trace`

## Packaging

The `OUTPUT` directory will contain the release packages and associated SHA1 checksum files.

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