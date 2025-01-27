/*
 * Copyright (C) 2024-present Pipeliner project authors and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.verifyica.pipeliner.core;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.common.LineParser;
import org.verifyica.pipeliner.core.command.Command;
import org.verifyica.pipeliner.core.command.ExtensionCommand;
import org.verifyica.pipeliner.core.command.StandardCommand;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement Step */
public class Step extends Node {

    private static final Logger LOGGER = LoggerFactory.getLogger(Step.class);

    private String shell;
    private String run;
    private List<Command> commands;

    /**
     * Constructor
     */
    public Step() {
        super();

        this.shell = "default";
    }

    /**
     * Method to set the shell
     *
     * @param shell the shell
     */
    public void setShell(String shell) {
        if (shell != null) {
            this.shell = shell;
        }
    }

    /**
     * Method to get the shell
     *
     * @return the shell
     */
    public String getShell() {
        return shell;
    }

    /**
     * Method to set the run
     *
     * @param run the run
     */
    public void setRun(String run) {
        if (run != null) {
            this.run = run.trim();
        }
    }

    /**
     * Method to get the run
     *
     * @return the run
     */
    public String getRun() {
        return run;
    }

    @Override
    public void validate() {
        validateId();
        validateEnabled();
        validateEnv();
        validateWith();
        validateWorkingDirectory();
        validateTimeoutMinutes();
        validateShell();
        validateRun();

        // Parse the commands
        parseCommands();
    }

    @Override
    public int execute(Context context) {
        getStopwatch().reset();

        Console console = context.getConsole();
        int exitCode = 0;

        if (Boolean.TRUE.equals(Enabled.decode(getEnabled()))) {
            // Emit the status
            console.emit("%s status=[%s]", this, Status.RUNNING);

            // Add the step environment variables to the context
            getEnv().forEach((name, value) -> {
                context.getEnv().put(name, value);
            });

            String pipelineId = getParent(Job.class).getParent(Pipeline.class).getId();
            String jobId = getParent(Job.class).getId();
            String stepId = getId();

            // Add the step variables to the context
            getWith().forEach((name, value) -> {
                // Add the unscoped variable
                context.getWith().put(name, value);

                if (stepId != null) {
                    // Add the step scoped variable
                    context.getWith().put(stepId + Constants.SCOPE_SEPARATOR + name, value);

                    if (jobId != null) {
                        // Add the job + step scoped variable
                        context.getWith()
                                .put(
                                        jobId + Constants.SCOPE_SEPARATOR + stepId + Constants.SCOPE_SEPARATOR + name,
                                        value);

                        if (pipelineId != null) {
                            // Add the pipeline + job + step scoped variable
                            context.getWith()
                                    .put(
                                            pipelineId
                                                    + Constants.SCOPE_SEPARATOR
                                                    + jobId
                                                    + Constants.SCOPE_SEPARATOR
                                                    + stepId
                                                    + Constants.SCOPE_SEPARATOR
                                                    + name,
                                            value);
                        }
                    }
                }
            });

            // Execute the commands
            for (Command command : commands) {
                // Execute the command
                exitCode = command.execute(context);

                // If the exit code is not 0, break the loop
                if (exitCode != 0) {
                    break;
                }
            }

            // Get the status based on the exit code
            Status status = exitCode == 0 ? Status.SUCCESS : Status.FAILURE;

            // Emit the status
            console.emit(
                    "%s status=[%s] exit-code=[%d] ms=[%s]",
                    this, status, exitCode, getStopwatch().elapsedTime().toMillis());
        }

        return exitCode;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    public String toString() {
        return "@step" + super.toString();
    }

    /**
     * Method to validate the shell value
     */
    private void validateShell() {
        if (shell == null) {
            throw new PipelineDefinitionException(format("%s -> shell is null", this));
        }

        if (shell.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> shell is blank", this));
        }

        if (Shell.decode(shell) == Shell.INVALID) {
            throw new PipelineDefinitionException(format(
                    "%s -> shell=[%s] is invalid. If defined, must be \"bash\", \"sh\", \"zsh\", or \"none\"",
                    this, shell));
        }
    }

    /**
     * Method to validate the run value
     */
    private void validateRun() {
        if (run == null) {
            throw new PipelineDefinitionException(format("%s -> run is null", this));
        }

        if (run.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> run is blank", this));
        }

        // Parse the commands
        List<String> commands = LineParser.parse(run);

        // Validate the step has at least one command
        if (commands.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> run is blank", this));
        }

        // Loop through the commands
        for (String command : commands) {
            // If the line is a directive, validate it is a known directive
            if (command.startsWith(Constants.DIRECTIVE_COMMAND_PREFIX)
                    && (!(command.startsWith(Constants.EXTENSION_DIRECTIVE_COMMAND_PREFIX + " ")
                            || command.startsWith(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX + " ")
                            || command.equals("--noop")
                            || command.startsWith("--emit")))) {
                throw new PipelineDefinitionException(format("%s -> unknown directive [%s]", this, command));
            }
        }
    }

    /**
     * Method to parse the run content into a list of commands
     */
    private void parseCommands() {
        commands = new ArrayList<>();

        for (String command : LineParser.parse(run)) {
            if (command.startsWith(Constants.DIRECTIVE_COMMAND_PREFIX)) {
                if (command.startsWith(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX)) {
                    commands.add(new StandardCommand(this, command));
                } else if (command.startsWith(Constants.EXTENSION_DIRECTIVE_COMMAND_PREFIX)) {
                    commands.add(new ExtensionCommand(this, command));
                } else {
                    throw new PipelineDefinitionException(format("%s -> unknown directive [%s]", this, command));
                }
            } else {
                commands.add(new StandardCommand(this, command));
            }
        }
    }
}
