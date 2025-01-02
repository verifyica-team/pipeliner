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
@info Verifyica Pipeliner 0.19.0 (https://github.com/verifyica-team/pipeliner)
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
@info Verifyica Pipeliner 0.19.0 (https://github.com/verifyica-team/pipeliner)
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

## Properties

A pipeline, job, or step can define properties using a `with` map.

These properties can be used in `run` statements as well as a `working-directory` value.

A property must match the regular expression `^[A-Za-z0-9][A-Za-z0-9-_.]*$`

### Example

```yaml
pipeline:
  name: Hello World Pipeline
  id: hello-world-pipeline
  enabled: true
  with:
    property.1: pipeline.foo
    property.2: pipeline.bar
  jobs:
    - name: Hello World Job
      id: hello-world-job
      enabled: true
      with:
        property.1: job.foo
        property.2: job.bar
      steps:
        - name: Hello World Step
          id: hello-world-step
          enabled: true
          with:
            property.1: step.foo
            property.2: step.bar
          run: |
            echo global scoped properties - ${{ property.1 }} ${{ property.2 }}
            echo step scoped properties - ${{ hello-world-step.property.1 }} ${{ hello-world-step.property.2 }}
            echo step scoped properties - ${{ hello-world-job.hello-world-step.property.1 }} ${{ hello-world-job.hello-world-step.property.2 }}
            echo step scoped properties - ${{ hello-world-pipeline.hello-world-job.hello-world-step.property.1 }} ${{ hello-world-pipeline.hello-world-job.hello-world-step.property.2 }}
            echo job scoped properties - ${{ hello-world-job.property.1 }} ${{ hello-world-job.property.2 }}
            echo job scoped properties - ${{ hello-world-pipeline.hello-world-job.property.1 }} ${{ hello-world-pipeline.hello-world-job.property.2 }}
            echo pipeline scoped properties - ${{ hello-world-pipeline.property.1 }} ${{ hello-world-pipeline.property.2 }}
```

**NOTES**

- To referenced scoped properties, a unique `id` is required for each pipeline, jobs, and steps

- Scoped properties can be referenced using the `id` of the pipeline, job, or step
  - `${{ hello-world-pipeline.property.1 }}`
  - `${{ hello-world-pipeline/property.1 }}`


- An `id` must match the regular expression `^[a-zA-Z_][a-zA-Z0-9_-]*$` 


- Property replacement is recursive
  - a property can be defined using another property or environment variable

- You can set a property to prevent a step `run` command from showing property values
  - `pipeliner.mask.properties: true` 
  - great for security sensitive properties

### Command

```shell
./pipeliner examples/properties-1.yaml
```

### Output

```shell
@info Verifyica Pipeliner 0.19.0 (https://github.com/verifyica-team/pipeliner)
@info filename=[properties-1.yaml]
@pipeline name=[Hello World Pipeline] id=[hello-world-pipeline] status=[RUNNING]
@job name=[Hello World Job] id=[hello-world-job] status=[RUNNING]
@step name=[Hello World Step] id=[hello-world-step] status=[RUNNING]
$ echo globally scoped properties - step.foo step.bar
$ echo step scoped properties - step.foo step.bar
$ echo step scope properties - step.foo step.bar
$ echo step scoped properties - step.foo step.bar
$ echo job scoped properties - job.foo job.bar
$ echo job scoped properties - job.foo job.bar
$ echo pipeline scoped properties - pipeline.foo pipeline.bar
@step name=[Hello World Step] id=[hello-world-step] status=[SUCCESS] exit-code=[0] ms=[56]
@job name=[Hello World Job] id=[hello-world-job] status=[SUCCESS] exit-code=[0] ms=[77]
@pipeline name=[Hello World Pipeline] id=[hello-world-pipeline] status=[SUCCESS] exit-code=[0] ms=[78]
```

## Capture Properties

The output of a step can be captured as a property to be used in subsequent jobs and steps.

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

For more complex scenarios, where you need to pass properties and capture properties, you can use Pipeliner IPC.

Pipeliner creates two temporary files. The filenames are passed to the application as environment variables.

- `PIPELINER_IPC_IN`
- `PIPELINER_IPC_OUT` 

**Notes**

- The properties file use a `name=value` format
  - property names must match the regular expression `^[a-zA-Z_][a-zA-Z0-9_-.]*$`
  - values are escaped...
    - '\\' is escaped as '\\\\'
    - '\r' is escaped as '\\\r'
    - '\n' is escaped as '\\\n'
  - lines starting with `#` are ignored
  - empty lines are ignored

### PIPELINER_IPC_IN

Environment variable that contains the full path name of the properties file passed from Pipeliner to your application.

Read the properties file to get properties.

### PIPELINER_IPC_OUT

Environment variable that contains the full path name of the properties file your application should write to capture properties.

Write to this properties file to capture properties.

### Example

- example IPC pipeline [examples/ipc.yaml](examples/ipc.yaml)
  - uses a class `Extension` packaged in the jar as an example


- additional examples in other languages
  - [examples/ipc](examples/ipc)

## Project Installation

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

## Executing

```shell
./pipeliner <YOUR PIPELINE YAML>
```

# Pipeliner Extensions

Pipeliner allows you to create extensions to add additional functionality.

An extension is a `zip` or `tar.gz` file containing code and a shell script named `execute.sh` or `run.sh`.

- `execute.sh` takes precedence over `run.sh` if both files exist

Extensions must use Pipeliner IPC to get and capture properties.

## Example

Example pipeline that create an extension and uses it.

- Example pipeline using `--extension` [examples/extensions.yaml](examples/extensions.yaml)

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
          run: --extension file://TMP/tmp.zip
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
  - optionally, you can use a file URL (e.g. `file://<EXTENSION>`)


- Use an SHA-256 checksum to verify the integrity of the extension
  - `run: --extension file://<EXTENSION> <EXTENSION_SHA-256_CHECKSUM>`


- Extensions are cached during a pipeliner execution
  - nested pipeliner executions will not use the cached extensions


- BASIC HTTP authentication can be enabled for password protected remote extensions
  - set via properties
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
- `--minimal`
  - only emits commands, command output, and errors in output
- `--with <property name>=<value>` or `-P <property name>=<value>` 
  - sets a property
  - repeatable
- `--with-file <filename>`
  - loads and set properties from a file
  - repeatable 
- `--env <environment variable>=<value>` or `-E <environment variable>=<value>` 
  - sets an environment variable
  - repeatable
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

## Packages

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

![YourKit logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](https://www.yourkit.com/) supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications.

YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>,
<a href="https://www.yourkit.com/dotnet-profiler/">YourKit .NET Profiler</a>,
and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.

---

Copyright (C) 2024-present Pipeliner project authors and contributors