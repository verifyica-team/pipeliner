# Migration

Pipeliner 1.x.x pipelines require migration to the 2.x.x format.

This document outlines the changes between Pipeliner 1.x.x and 2.x.x, and how to migrate your pipelines.

# Is there a migration tool?

There is no automated migration tool available at this time.

# Changes

## ID field

1.x.x uses the optional `id` field to identify pipelines, jobs, and steps.

2.x.x no longer supports the `id` field.

### Why the change?

Because the `id` field was using for variable scoping, the `id` values had to be unique across the entire pipeline, which was cumbersome.

## Variable Scoping

1.x.x uses the optional `id` field for variable scoping in a pipeline, job, or step. It also did not support `.` in variable names.

2.x.x scopes variables globally. Variable names can now contain `.` characters, allowing for hierarchical variable names.

### Why the change?

Globally scoped variables, using a hierarchical naming convention, simplifies variable usage.

This is also consistent with how environment variables are typically used in many programming languages and tools.

# Capturing Variables

1.x.x captures variables used a command suffix of `> $<variable name>` and `>> $<variable name>`.

2.x.x captures variables using the `--capture ${{ <variable name> }}` and `--capture:append ${{ <variable name> }}` directives.

### Why the change?

The new syntax is less ambiguous, less error-prone, and more consistent with the overall Pipeliner syntax.

# Directives

1.x.x has the following directives:

- `--scp` - Remotely copy a file using `scp`.

- `--ssh` - Remotely execute a command using `ssh`.

2.x.x removes these directives in favor of direct use of the `scp` and `ssh` commands for better flexibility and compatibility.

### Why the change?

The `scp` and `ssh` commands are widely used and well-understood.

Implementing these directive directly means writing a Java version of the `scp` and `ssh` commands, which is not necessary.

# Extension Directive

1.x.x supports downloading and running an extension using `--extension http://<url>`.

2.x.x removes support for `file://` and `http://` file arguments in favor of direct use of `curl` or `wget` for better flexibility and compatibility.

### Why the change?

Supporting downloads using `http://` directly means implementing a Java version of `curl` or `wget`, which is not necessary.

# Extension Changes

Pipeliner 1.x.x supports extensions with the following entry points (shell scripts):

- `run.sh`
- `execute.sh`
- `entrpoint.sh`
- `ENTRYPOINT`

Pipeliner 2.x.x only supports extensions with the entry point `run.sh`.

### Why the change?

The provides a single consistent entry point for extensions.

---

Copyright (C) Pipeliner project authors and contributors