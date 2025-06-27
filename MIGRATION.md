# Migration

Pipeliner 1.x.x pipelines require migration to the 2.x.x format.

This document outlines the changes between Pipeliner 1.x.x and 2.x.x, and how to migrate your pipelines.

# Changes

## ID field

1.x.x used the optional `id` field to identify pipelines, jobs, and steps.

2.x.x no longer supports the `id` field.

## Variable Scoping

1.x.x used the optional `id` field for variable scoping in a pipeline, job, or step. It also did not support `.` in variable names.

2.x.x scopes variables globally. Variable names can now contain `.` characters, allowing for hierarchical variable names.

# Capturing Variables

1.x.x captured variables using a command suffix of `> $<variable name>` and `>> $<variable name>`.

2.x.x captures variables using the `--capture` and `--capture:append` directives.

# Directives

1.x.x had the following directives:

- `--scp` - Remotely copy a file using SCP.

- `--ssh` - Remotely execute a command using SSH.

2.x.x removed these directives in favor of direct use of the `scp` and `ssh` commands for better flexibility and compatibility.

# Extension Directive

1.x.x supporting downloading and running an extension using `--extension https://<url>`.

2.x.x removed support for `file://` and `http://` file arguments in favor of direct use of `curl` or `wget` for better flexibility and compatibility.

# Extension Changes

Pipeliner 1.x.x supported extensions with the following entry points (shell scripts):

- `run.sh`
- `execute.sh`
- `entrpoint.sh`
- `ENTRYPOINT`

Pipeliner 2.x.x only supports extensions with the entry point `run.sh`.

---

Copyright (C) Pipeliner project authors and contributors