### This README.md and other documentation is specific to a branch / release

---

[![Build](https://github.com/verifyica-team/pipelines/actions/workflows/build.yaml/badge.svg)](https://github.com/verifyica-team/pipelines/actions/workflows/build.yaml)

# Verifyica Pipeliner

Verifyica Pipeliner allows you define and run a local pipeline using a syntax ***similar*** to GitHub actions.

**Pipeliner is not designed to be 100% GitHub action compatible**

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

## Pipeline Output

The output format is easy to decode..

### Info

- Information prints are prefixed with `@info`

### Error

- Error prints are prefixed with `@error`

### Trace

- Trace prints are prefixed with `@trace`

### Pipeline, Job, Step

The pipeline, jobs, and steps prints are prefixed with `@<IDENTIFIER>`

- `@pipeline`
- `@job`
- `@step`

**Notes**

- When a pipeline, jobs, and steps complete, and exit code and execution time are printed

### Command

- The command executed is prefixed with `$ `
- The command output is prefixed with `> `

### Example Pipeline Output

```shell
user@machine> ./pipeliner examples/hello-world-pipeline.yaml
```

```shell
@pipeline name=[hello-world-pipeline] id=[pipeline] location=[pipeline]
@job name=[hello-world-job] id=[pipeline-job-1] location=[pipeline-job-1]
@step name=[hello-world-step-1] id=[pipeline-job-1-step-1] location=[pipeline-job-1-step-1]
$ echo "Hello World"
> Hello World
@step name=[hello-world-step-1] id=[pipeline-job-1-step-1] location=[pipeline-job-1-step-1] exit-code=[0] ms=[32]
@step name=[hello-world-step-2] id=[pipeline-job-1-step-2] location=[pipeline-job-1-step-2]
$ echo \"Hello World\"
> "Hello World"
@step name=[hello-world-step-2] id=[pipeline-job-1-step-2] location=[pipeline-job-1-step-2] exit-code=[0] ms=[8]
@job name=[hello-world-job] id=[pipeline-job-1] location=[pipeline-job-1] exit-code=[0] ms=[41]
@pipeline name=[hello-world-pipeline] id=[pipeline] location=[pipeline] exit-code=[0] ms=[47]

```

## Project Installation

```bash
cd <PROJECT DIRECTORY>
unzip verifyica-pipeliner.zip
./pipeliner --version
./pipeliner hello-world-pipeline.yaml
```` 

## Executing

```shell
./pipeliner <YOUR PIPELINE YAML>
```

# Pipeliner Options

Pipeliner has four options:

- `--timestamps`
  - show timestamps 
- `--log`
  - log to a file 
- `--trace`
  - log trace messages
- `--minimal`
  - log commands, output, and errors

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
./mvn clean package
./pipeliner release.yaml
```

**Notes**

- `verifiyica-piperliner.zip` will be place in the `output` directory

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