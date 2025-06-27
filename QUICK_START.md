# Quick Start

This document assumes you already have Pipeliner installed.

## Create a new pipeline

Let's create a new pipeline called `example-pipeline.yaml`...

```yaml
pipeline:
  name: example-pipeline
  description: An example pipeline
```

Let's run the pipeline...

```bash
./pipeliner example-pipeline.yaml
```

You should see the output:

```
@info Pipeliner 2.0.0 (https://github.com/verifyica-team/pipeliner)
@info file [example-pipeline.yaml]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[running]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[success]
```

- `name` and `description` are optional values for a pipeline, job, or step.

- If declared, they must not be blank.

## Add a Job to the Pipeline

A pipeline should have at least one job.

Let's add a job to our pipeline...

```yaml
pipeline:
  name: example-pipeline
  description: An example pipeline
  jobs:
    - name: example-job
      description: An example job
```

Let's run the pipeline...

```bash
./pipeliner example-pipeline.yaml
```

You should see the output:

```
@info Pipeliner 2.0.0 (https://github.com/verifyica-team/pipeliner)
@info file [example-pipeline.yaml]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[running]
@job name=[example-job] description=[An example job] status=[running]
@job name=[example-job] description=[An example job] status=[success]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[success]
```

## Add a Step to the pipeline

A job should have at least one step.

Let's add a step to our job...

```yaml
pipeline:
  name: example-pipeline
  description: An example pipeline
  jobs:
    - name: example-job
      description: An example job
      steps:
        - name: example-step
          description: An example step
          run: echo "Hello, World!"
```

Let's run the pipeline...

```bash
./pipeliner example-pipeline.yaml
```

You should see the output:

```
@info Pipeliner 2.0.0 (https://github.com/verifyica-team/pipeliner)
@info file [example-pipeline.yaml]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[running]
@job name=[example-job] description=[An example job] status=[running]
@step name=[example-step] description=[An example step] status=[running]
@command echo "Hello, World!"
@output Hello, World!
@step name=[example-step] description=[An example step] status=[success]
@job name=[example-job] description=[An example job] status=[success]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[success]
```

- The `run` value in the `step` is the command to run.

- The `output` field shows the output of the command.

Let's add a second `run` command to the step...

```yaml
pipeline:
  name: example-pipeline
  description: An example pipeline
  jobs:
    - name: example-job
      description: An example job
      steps:
        - name: example-step
          description: An example step
          run: |
            echo "Hello, World!"
            echo "Hello, Universe!"
```

Let's run the pipeline again...

```bash
./pipeliner example-pipeline.yaml
```

You should see the output:

```
@info Pipeliner 2.0.0 (https://github.com/verifyica-team/pipeliner)
@info file [example-pipeline.yaml]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[running]
@job name=[example-job] description=[An example job] status=[running]
@step name=[example-step] description=[An example step] status=[running]
@command echo "Hello, World!"
@output Hello, World!
@command echo "Hello, Universe!"
@output Hello, Universe!
@step name=[example-step] description=[An example step] status=[success]
@job name=[example-job] description=[An example job] status=[success]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[success]
```

## Variables

You can use variables in your pipeline.

Let's add a variables to our pipeline...

```yaml
pipeline:
  name: example-pipeline
  description: An example pipeline
  with:
    world: World
    universe: Universe
  jobs:
    - name: example-job
      description: An example job
      steps:
        - name: example-step
          description: An example step
          run: |
            echo "Hello, ${{ world }}!"
            echo "Hello, ${{ universe }}!"
```

Let's run the pipeline again...

```bash
./pipeliner example-pipeline.yaml
```

You should see the output:

```
@info Pipeliner 2.0.0 (https://github.com/verifyica-team/pipeliner)
@info file [example-pipeline.yaml]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[running]
@job name=[example-job] description=[An example job] status=[running]
@step name=[example-step] description=[An example step] status=[running]
@command echo "Hello, ${{ world }}!"
@output Hello, World!
@command echo "Hello, ${{ universe }}!"
@output Hello, Universe!
@step name=[example-step] description=[An example step] status=[success]
@job name=[example-job] description=[An example job] status=[success]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[success]
```

- A Variable name must match the pattern `^[a-zA-Z_]([a-zA-Z0-9-_.]*[a-zA-Z0-9_])?$`.

- The pattern `${{ <variable> }}` is used to reference variables in the pipeline.

- Variables are globally scoped. You can use them in any job or step once they are defined.

- Variables names are case-sensitive, so `world` and `World` are different variables.

- It's suggested to use a period `.` in a variable names to scope variables.

## Environment Variables

You can also use environment variables in your pipeline.

Let's add an environment variable to our pipeline...

```yaml
pipeline:
  name: example-pipeline
  description: An example pipeline
  with:
    world: World
    universe: Universe
  env:
    ENV_VAR: "This is an environment variable"
  jobs:
    - name: example-job
      description: An example job
      steps:
        - name: example-step
          description: An example step
          run: |
            echo "Hello, ${{ world }}!"
            echo "Hello, ${{ universe }}!"
            echo "$ENV_VAR"
            echo "${ENV_VAR}"
```

Let's run the pipeline again...

```bash
./pipeliner example-pipeline.yaml
```

You should see the output:

```
@info Pipeliner 2.0.0 (https://github.com/verifyica-team/pipeliner)
@info file [example-pipeline.yaml]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[running]
@job name=[example-job] description=[An example job] status=[running]
@step name=[example-step] description=[An example step] status=[running]
@command echo "Hello, ${{ world }}!"
@output Hello, World!
@command echo "Hello, ${{ universe }}!"
@output Hello, Universe!
@command echo "$ENV_VAR"
@output This is an environment variable
@command echo "${ENV_VAR}"
@output This is an environment variable
@step name=[example-step] description=[An example step] status=[success]
@job name=[example-job] description=[An example job] status=[success]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[success]
```

- The patterns `$<environment variable>``${<environment variable>}` is used to reference environment variables in the pipeline.

- Environment variables are globally available in the pipeline, meaning you can use them in any job or step once they are defined.

- System environment variables are also available in the pipeline, so you can use them in your pipeline as well.

Let's add a system environment variable to our pipeline...

```yaml
pipeline:
  name: example-pipeline
  description: An example pipeline
  with:
    world: World
    universe: Universe
  env:
    ENV_VAR: "This is an environment variable"
  jobs:
    - name: example-job
      description: An example job
      steps:
        - name: example-step
          description: An example step
          run: |
            echo "Hello, ${{ world }}!"
            echo "Hello, ${{ universe }}!"
            echo "$ENV_VAR"
            echo "${ENV_VAR}"
            echo The working directory is $PWD
            echo The working directory is ${PWD}
```

Let's run the pipeline again...

```bash
./pipeliner example-pipeline.yaml
```

You should see the output:

```
@info Pipeliner 2.0.0 (https://github.com/verifyica-team/pipeliner)
@info file [example-pipeline.yaml]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[running]
@job name=[example-job] description=[An example job] status=[running]
@step name=[example-step] description=[An example step] status=[running]
@command echo "Hello, ${{ world }}!"
@output Hello, World!
@command echo "Hello, ${{ universe }}!"
@output Hello, Universe!
@command echo "$ENV_VAR"
@output This is an environment variable
@command echo "${ENV_VAR}"
@output This is an environment variable
@command echo The working directory is $PWD
@output The working directory is <your current directory>
@command echo The working directory is ${PWD}
@output The working directory is <your current directory>
@step name=[example-step] description=[An example step] status=[success]
@job name=[example-job] description=[An example job] status=[success]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[success]
```

## Capturing Output to a Variable

You can capture the output of a command and use it in subsequent steps.

Let's modify our pipeline to capture the output of a step...

```yaml
pipeline:
  name: example-pipeline
  description: An example pipeline
  with:
    world: World
    universe: Universe
  env:
    ENV_VAR: "This is an environment variable"
  jobs:
    - name: example-job
      description: An example job
      steps:
        - name: example-step
          description: An example step
          run: |
            echo "Hello, ${{ world }}!"
            echo "Hello, ${{ universe }}!"
            echo "$ENV_VAR"
            echo "${ENV_VAR}"
            echo The working directory is $PWD
            echo The working directory is ${PWD}
            --capture ${{ hostname }} hostname
            echo The hostname is ${{ hostname }}
```

Let's run the pipeline again...

```bash
./pipeliner example-pipeline.yaml
```

You should see the output:

```
@info Pipeliner 2.0.0 (https://github.com/verifyica-team/pipeliner)
@info file [example-pipeline.yaml]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[running]
@job name=[example-job] description=[An example job] status=[running]
@step name=[example-step] description=[An example step] status=[running]
@command echo "Hello, ${{ world }}!"
@output Hello, World!
@command echo "Hello, ${{ universe }}!"
@output Hello, Universe!
@command echo "$ENV_VAR"
@output This is an environment variable
@command echo "${ENV_VAR}"
@output This is an environment variable
@command echo The working directory is $PWD
@output The working directory is <your current directory>
@command echo The working directory is ${PWD}
@output The working directory is <your current directory>
@command --capture ${{ hostname }} hostname
@command echo The hostname is ${{ hostname }}
@output The hostname is <your hostname>
@step name=[example-step] description=[An example step] status=[success]
@job name=[example-job] description=[An example job] status=[success]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[success]
```

You can also append capture output of another command and append it to an existing variable.

Let's modify our pipeline to append the output of a step...

```yaml
pipeline:
  name: example-pipeline
  description: An example pipeline
  with:
    world: World
    universe: Universe
  env:
    ENV_VAR: "This is an environment variable"
  jobs:
    - name: example-job
      description: An example job
      steps:
        - name: example-step
          description: An example step
          run: |
            echo "Hello, ${{ world }}!"
            echo "Hello, ${{ universe }}!"
            echo "$ENV_VAR"
            echo "${ENV_VAR}"
            echo The working directory is $PWD
            echo The working directory is ${PWD}
            --capture ${{ hostname }} hostname
            echo The hostname is ${{ hostname }}
            --capture:append ${{ hostname }} uptime
            echo The uptime is ${{ hostname }}
```

Let's run the pipeline again...

```bash
./pipeliner example-pipeline.yaml
```

You should see the output:

```
@info Pipeliner 2.0.0 (https://github.com/verifyica-team/pipeliner)
@info file [example-pipeline.yaml]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[running]
@job name=[example-job] description=[An example job] status=[running]
@step name=[example-step] description=[An example step] status=[running]
@command echo "Hello, ${{ world }}!"
@output Hello, World!
@command echo "Hello, ${{ universe }}!"
@output Hello, Universe!
@command echo "$ENV_VAR"
@output This is an environment variable
@command echo "${ENV_VAR}"
@output This is an environment variable
@command echo The working directory is $PWD
@output The working directory is <your current directory>
@command echo The working directory is ${PWD}
@output The working directory is <your current directory>
@command --capture ${{ hostname }} hostname
@command echo The hostname is ${{ hostname }}
@output The hostname is <your hostname>
@command --capture:append ${{ hostname }} uptime
@command echo The uptime is ${{ hostname }}
@output The uptime is <your hostname> <your uptime>
@step name=[example-step] description=[An example step] status=[success]
@job name=[example-job] description=[An example job] status=[success]
@pipeline name=[example-pipeline] description=[An example pipeline] status=[success]
```

## Other directives

Pipeliner supports several other directives that can be used in the pipeline YAML file:

- `--pipeline <file>`

This directive is used to execute a pipeline file in another file.

- `--sha-cheksum <file> <sha checksum`

This directive is used to validate the SHA checksum of a file.

It can be used to verify the integrity of files in your pipeline.

SHA-1, SHA-256, and SHA-512 checksums are supported automatically.

`sha1sum`, `sha256sum`, and `sha512sum` must be installed on your system.

- `--print-info`

This directive is used to print an `@info` message.

Variables are resolved before printing the message.

Environment variables are not resolved.

- `--print-warning`

This directive is used to print an `@warning` message.

Variables are resolved before printing the message.

Environment variables are not resolved.

- `--print-error`

This directive is used to print an `@error` message.

Variables are resolved before printing the message.

Environment variables are not resolved.

- `--extension`

This directive is used to run an extension. (Advanced usage)

`tar` and `zip` must be installed on your system.

## Other Examples

The [examples](examples) and [tests](tests) directories contain more examples.

---

Copyright (C) Pipeliner project authors and contributors