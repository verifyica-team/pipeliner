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

package org.verifyica.pipeliner.model;

import static java.lang.String.format;

import java.util.List;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.common.LineParser;
import org.verifyica.pipeliner.execution.support.Shell;

/** Class to implement StepModel */
public class StepModel extends Model {

    private String shell;
    private String capturePrefix;
    private String run;

    /** Constructor */
    public StepModel() {
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
     * Method to get the capture prefix
     *
     * @param capturePrefix the capture prefix
     */
    public void setCapturePrefix(String capturePrefix) {
        if (capturePrefix != null) {
            this.capturePrefix = capturePrefix.trim();
        }
    }

    /**
     * Method to get the capture prefix
     *
     * @return the capture prefix
     */
    public String getCapturePrefix() {
        return capturePrefix;
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
        validateName();
        validateEnabled();
        validateEnv();
        validateWith();
        validateWorkingDirectory();
        validateTimeoutMinutes();
        validateShell();
        validateCapturePrefix();
        validateRun();
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
     * Method to validate the capture prefix value
     */
    private void validateCapturePrefix() {
        if (capturePrefix != null) {
            if (capturePrefix.isEmpty()) {
                throw new PipelineDefinitionException(format("%s -> capturePrefix is blank", this));
            }

            if (Variable.isInvalid(capturePrefix)) {
                throw new PipelineDefinitionException(
                        format("%s -> capture-prefix=[%s] is invalid", this, capturePrefix));
            }
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

        List<String> lines = LineParser.parse(run);
        if (lines.isEmpty()) {
            throw new PipelineDefinitionException(format("%s -> run is blank", this));
        }

        for (String line : lines) {
            // If the line is a directive, validate it is a known directive
            if (line.startsWith(Constants.DIRECTIVE_COMMAND_PREFIX)
                    && (!(line.startsWith(Constants.EXTENSION_DIRECTIVE_COMMAND_PREFIX + " ")
                            || line.startsWith(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX + " ")))) {
                throw new PipelineDefinitionException(format("%s -> unknown directive [%s]", this, line));
            }
        }
    }

    @Override
    public String toString() {
        return "@step " + super.toString();
    }
}
