# Verifyica Pipelines

Verifyica Pipelines allows you define and run a local pipeline.

Example pipeline YAML file:

```yaml
pipeline:
  name: regression-build
  with:
    global.property: Global Property - Pipeline
    pipeline.directory: .
    pipeline.property: Pipeline Property
  jobs:
    - name: phase-1
      enabled: true
      steps:
        - name: step-1
          enabled: true
          working-directory: ${{pipeline.directory}}
          run: ./mvnw clean package
    - name: phase-2
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
          run: ./echo.sh ${{ USER }}
        - name: echo-pipeline-property
          enabled: true
          run: ./echo.sh ${{ pipeline.property }}
        - name: echo-job-property
          enabled: true
          run: ./echo.sh ${{ job.property }}
        - name: echo-step-property
          enabled: true
          with:
            step.property: Step Property
          run: ./echo.sh "${{ pipeline.property }}" "${{ job.property }} "${{ step.property }}"
```