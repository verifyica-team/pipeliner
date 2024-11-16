[![Build](https://github.com/verifyica-team/pipelines/actions/workflows/build.yaml/badge.svg)](https://github.com/verifyica-team/pipelines/actions/workflows/build.yaml)

# Verifyica Pipeliner

Verifyica Pipeliner allows you define and run a local pipeline using a syntax ***similar*** to GitHub actions.

**Pipeliner is not design to be 100% GitHub action compatible**

### Pipeline definition

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
```

### Building

```bash
git clone https://github.com/verifyica-team/pipeliner
cd pipeliner
./mvn clean package
./pipeliner pipelines/release.yaml
```

### Project Installation

```bash
cd <PROJECT DIRECTORY>
unzip verifyica-pipeliner.zip
./pipeliner --version
./pipeliner hello-world-pipeline.yaml
```` 

### Executing

```shell
./pipeliner example-pipeline.yaml
```

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