/*
 * Copyright (C) Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.core.executable;

import static java.lang.String.format;

import java.io.File;
import java.util.Map;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.core.Job;
import org.verifyica.pipeliner.core.Pipeline;
import org.verifyica.pipeliner.core.Step;
import org.verifyica.pipeliner.core.support.Resolver;
import org.verifyica.pipeliner.core.support.UnresolvedException;
import org.verifyica.pipeliner.parser.SyntaxException;

/** Class to implement AbstractExecutable */
public abstract class AbstractExecutable implements Executable {

    private final Pipeline pipeline;
    private final Job job;
    private final Step step;
    private final String commandLine;

    /**
     * Constructor
     *
     * @param step the step
     * @param commandLine the command line
     */
    public AbstractExecutable(Step step, String commandLine) {
        this.pipeline = step.getParent(Job.class).getParent(Pipeline.class);
        this.job = step.getParent(Job.class);
        this.step = step;
        this.commandLine = commandLine;
    }

    /**
     * Method to get the pipeline
     *
     * @return the pipeline
     */
    protected Pipeline getPipeline() {
        return pipeline;
    }

    /**
     * Method to get the pipeline id
     *
     * @return the pipeline id
     */
    protected String getPipelineId() {
        return getPipeline().getId();
    }

    /**
     * Method to get the job
     *
     * @return the job
     */
    protected Job getJob() {
        return job;
    }

    /**
     * Method to get the job id
     *
     * @return the job id
     */
    protected String getJobId() {
        return getJob().getId();
    }

    /**
     * Method to get the step
     *
     * @return the step
     */
    protected Step getStep() {
        return step;
    }

    /**
     * Method to get the step id
     *
     * @return the step id
     */
    protected String getStepId() {
        return getStep().getId();
    }

    /**
     * Method to get the command line
     *
     * @return the command line
     */
    protected String getCommandLine() {
        return commandLine;
    }

    /**
     * Method to resolve the working directory
     *
     * @param environmentVariables the environment variables
     * @param variables the variables
     * @return the working directory
     * @throws SyntaxException If a syntax error occurs
     * @throws UnresolvedException If a variable can't be resolved
     */
    protected File resolveWorkingDirectory(Map<String, String> environmentVariables, Map<String, String> variables)
            throws SyntaxException, UnresolvedException {
        // Get the step working directory
        String workingDirectory = getStep().getWorkingDirectory();
        if (workingDirectory == null) {
            // Get the job working directory
            workingDirectory = getJob().getWorkingDirectory();
            if (workingDirectory == null) {
                // Get the pipeline working directory
                workingDirectory = getPipeline().getWorkingDirectory();
            }
        }

        if (workingDirectory == null) {
            // No per step, job, or pipeline working directory was specified, so use the default working directory
            workingDirectory = Constants.DEFAULT_WORKING_DIRECTORY;

            return validateWorkingDirectory(new File(workingDirectory).getAbsoluteFile());
        }

        // Resolve the working directory
        String resolvedWorkingDirectory =
                Resolver.resolveAllVariables(environmentVariables, variables, workingDirectory);

        return validateWorkingDirectory(new File(resolvedWorkingDirectory).getAbsoluteFile());
    }

    /**
     * Method to validate the working directory
     *
     * @param workingDirectory the working directory
     * @return the working directory
     */
    private File validateWorkingDirectory(File workingDirectory) {
        // Validate the working directory exists
        if (!workingDirectory.exists()) {
            throw new IllegalStateException(format("working-directory=[%s] doesn't exit", workingDirectory));
        }

        // Validate the working directory is accessible
        if (!workingDirectory.canRead()) {
            throw new IllegalStateException(format("working-directory=[%s] can't be read", workingDirectory));
        }

        // Validate the working directory is a directory
        if (!workingDirectory.isDirectory()) {
            throw new IllegalStateException(format("working-directory=[%s] isn't a directory", workingDirectory));
        }

        return workingDirectory;
    }
}
