### This README.md and other documentation is specific to a branch / release

---

[![Build](https://github.com/verifyica-team/pipelines/actions/workflows/build.yaml/badge.svg)](https://github.com/verifyica-team/pipelines/actions/workflows/build.yaml)

# Verifyica Pipeliner

Verifyica Pipeliner allows you define and run a local pipeline using a syntax ***similar*** to GitHub actions.

**Pipeliner is not designed to be 100% GitHub action compatible**

## Why ?

There are many scenarios where you need to perform various processing steps during development, system maintenance, or general workflows.

Example:

- patching source files
- setting up a test environment
- building a custom zip or tar.gz file

#### Maven & Gradle

Maven and Gradle ***can*** and ***are*** useful in development environment ... but usage and configuration of those tools requires a good knowledge of the specific tool.

#### Bash

A common go to is to use a Bash script/scripts ... they have there place.

Implementing logging, exit code checking, etc. is commonly implemented incorrectly.

#### Pipeliner

Pipeliner allows an easily declarative definition of a pipeline/pipelines using YAML. 

## Pipeline YAML definition

```yaml
pipeline:
  name: example-pipeline
  enabled: true
  env:
    FOO: BAR
  with:
    global.property: Global Property - Pipeline
    pipeline.directory: .
    pipeline.property: Pipeline Property
  jobs:
    - name: job-1
      enabled: true
      with:
        job.property: Job Property
        global.property: Global Property - Job
      steps:
        - name: pwd
          enabled: true
          run: pwd
        - name: ls -la
          enabled: true
          run: ls -la
        - name: echo-user
          enabled: true
          run: echo ${{ USER }}
        - name: echo-pipeline-property
          enabled: true
          run: echo ${{ INPUT_PIPELINE_PROPERTY }}
        - name: echo-job-property
          enabled: true
          run: echo ${{ INPUT_JOB_PROPERTY }}
        - name: echo-step-property
          enabled: true
          with:
            step.property: Step Property
          run: echo "${{ INPUT_PIPELINE_PROPERTY }}" "${{ INPUT_JOB_PROPERTY }}" "${{ INPUT_STEP_PROPERTY }}"
        - name: echo-step-property-2
          enabled: true
          with:
            step.property: Step Property
          run: echo "$INPUT_PIPELINE_PROPERTY" "$INPUT_JOB_PROPERTY" "$INPUT_STEP_PROPERTY"
        - name: echo-environment-variable
          enabled: true
          run: echo $FOO
        - name: echo-environment-variable-2
          enabled: true
          run: echo ${{ FOO }}
        - name: run-multiple-commands
          enabled: true
          run: |
            pwd
            du -h -s
```

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


- The pipeline, all jobs, and all steps generate output, regardless if they are enabled or disabled

Starting output ...

```shell
@pipeline name=[hello-world-pipeline] id=[pipeline] ref=[pipeline] enabled=[true]
@job name=[hello-world-job] id=[pipeline-job-1] ref=[pipeline-job-1] enabled=[true]
@step name=[hello-world-step-1] id=[pipeline-job-1-step-1] ref=[pipeline-job-1-step-1] enabled=[true]
```

Finished output ...

```shell
@step name=[hello-world-step-2] id=[pipeline-job-1-step-2] ref=[pipeline-job-1-step-2] enabled=[true] exit-code=[0] ms=[6]
@job name=[hello-world-job] id=[pipeline-job-1] ref=[pipeline-job-1] enabled=[true] exit-code=[0] ms=[49]
@pipeline name=[hello-world-pipeline] id=[pipeline] ref=[pipeline] enabled=[true] exit-code=[0] ms=[49]
```

### Command

- The command executed is prefixed with `$ `
- The command output is prefixed with `> `

### Example Output

```shell
user@machine> ./pipeliner examples/hello-world-pipeline.yaml
```

```shell
@info Verifyica Pipeliner 0.2.0-post
@info https://github.com/verifyica-team/pipeliner
@info filename=[examples/hello-world-pipeline.yaml]
@pipeline name=[hello-world-pipeline] id=[pipeline] ref=[pipeline] enabled=[true]
@job name=[hello-world-job] id=[pipeline-job-1] ref=[pipeline-job-1] enabled=[true]
@step name=[hello-world-step-1] id=[pipeline-job-1-step-1] ref=[pipeline-job-1-step-1] enabled=[true]
$ echo "Hello World"
> Hello World
@step name=[hello-world-step-1] id=[pipeline-job-1-step-1] ref=[pipeline-job-1-step-1] enabled=[true] exit-code=[0] ms=[37]
@step name=[hello-world-step-2] id=[pipeline-job-1-step-2] ref=[pipeline-job-1-step-2] enabled=[true]
$ echo \"Hello World\"
> "Hello World"
@step name=[hello-world-step-2] id=[pipeline-job-1-step-2] ref=[pipeline-job-1-step-2] enabled=[true] exit-code=[0] ms=[6]
@job name=[hello-world-job] id=[pipeline-job-1] ref=[pipeline-job-1] enabled=[true] exit-code=[0] ms=[49]
@pipeline name=[hello-world-pipeline] id=[pipeline] ref=[pipeline] enabled=[true] exit-code=[0] ms=[49]
```

## Project Installation

```bash
cd <PROJECT DIRECTORY>
unzip verifyica-pipeliner.zip
./pipeliner --version
./pipeliner .pipeliner/hello-world-pipeline.yaml
```` 

## Executing

```shell
./pipeliner <YOUR PIPELINE YAML>
```

# Pipeliner Options

Pipeliner has four options:

- `--timestamps`
  - include timestamps in output 
- `--log`
  - log to a file 
- `--trace`
  - include trace messages in output
- `--minimal`
  - only include commands, command output, and errors in output

Optionally, the options can be set using environment variables:

- `PIPELINER_TIMESTAMPS=true`
- `PIPERLINER_LOG=true`
- `PIPELINER_TRACE=true`
- `PIPELINER_MINIMAL=true`

**Notes**

- Command line options override environment variables

# Building

```bash
git clone https://github.com/verifyica-team/pipeliner
cd pipeliner
./mvnw clean package
./pipeliner package.yaml
```

## Packages

The `output` directory will contain the release packages and associated SHA1 checksum files.

- `verifiyica-piperliner.zip`
- `verifiyica-piperliner.zip.sha1`
- `verifyica-piperlinger.tar.gz`
- `verifyica-piperlinger.tar.gz.sha1`

# Experimental

An additional **experimental** tool `converter` has been created to easily convert a text file of shell commands to a pipeline YAML file.

See [Converter](CONVERTER.md) for details.

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