# Verifyica Pipeline

Verifyica Pipeline allows you define and run a local pipeline.

Example pipeline YAML file:

```yaml
pipeline:
  name: regression-build
  properties:
    - name: global.property
      value: Global Property - Pipeline
    - name: pipeline.directory
      value: .
    - name: pipeline.property
      value: Pipeline Property
  jobs:
    - name: phase-1
      enabled: true
      steps:
        - name: step-1
          enabled: true
          working-directory: '{{pipeline.directory}}'
          run: ./mvnw clean package
    - name: phase-2
      enabled: true
      properties:
        - name: job.property
          value: Job Property
        - name: global.property
          value: Global Property - Job
      steps:
        - name: pwd
          enabled: true
          run: 'pwd'
        - name: echo-user
          enabled: true
          run: 'echo.sh {{USER}}'
        - name: echo-pipeline-property
          enabled: true
          run: 'echo.sh {{pipeline.property}}'
        - name: echo-job-property
          enabled: true
          run: 'echo.sh {{job.property}}'
        - name: echo-step-property
          enabled: true
          properties:
            - name: step.property
              value: Step Property
            - name: x.global.property
              value: Global Property - Step
          run: 'echo.sh "{{pipeline.property}}" "{{job.property}} "{{step.property}}"'
```
