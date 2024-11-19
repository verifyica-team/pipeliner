# Converter

`converter` is an **experimental** tool to convert text file of shell commands to a pipeline YAML file.

## Input file

```shell
input.txt
```

Content

```shell
./mvnw clean package
cd target
ls -la
```

## Execution

```shell
./converter input.txt > input.yaml
```

## Output Pipeline YAML

```shell
pipeline:
  name: pipeline-input.txt
  id: pipeline-input-txt
  enabled: true
  jobs:
    - name: pipeline-job-1
      id: pipeline-job-1
      enabled: true
      steps:
        - name: pipeline-job-1-step-1
          id: pipeline-job-1-step-1
          enabled: true
          run: ./mvnw clean package
        - name: pipeline-job-1-step-2
          id: pipeline-job-1-step-2
          enabled: true
          working-directory: target
          run: ls -la
```

**Notes**

- Capturing output to either input or environment variables is not supported


- Multiple sequential `cd` commands will be merged into a single `working-directory` value

---

Copyright (C) 2024-present Pipeliner project authors and contributors