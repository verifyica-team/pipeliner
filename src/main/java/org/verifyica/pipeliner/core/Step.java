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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.verifyica.pipeliner.common.Console;
import org.verifyica.pipeliner.common.Stopwatch;
import org.verifyica.pipeliner.core2.execution.Shell;

/** Class to implement Step */
@SuppressWarnings("PMD.EmptyControlStatement")
public class Step implements Executable {

    private final Job job;
    private final String reference;
    private String name;
    private String id;
    private final Map<String, String> environmentVariables;
    private final Map<String, String> properties;
    private final Map<String, String> options;
    private boolean enabled;
    private Shell shell;
    private String workingDirectory;
    private final List<Run> runs;
    private int exitCode;
    private final Stopwatch stopwatch;

    /**
     * Constructor
     *
     * @param job job
     * @param index index
     */
    public Step(Job job, int index) {
        this.job = job;
        this.reference = job.getReference() + "-step-" + index;
        this.enabled = true;
        this.environmentVariables = new LinkedHashMap<>();
        this.properties = new LinkedHashMap<>();
        this.options = new LinkedHashMap<>();
        this.shell = Shell.BASH;
        this.workingDirectory = ".";
        this.runs = new ArrayList<>();
        this.stopwatch = new Stopwatch();
    }

    /**
     * Method to get the job
     *
     * @return the job
     */
    public Job getJob() {
        return job;
    }

    /**
     * Method to get the location
     *
     * @return the location
     */
    public String getReference() {
        return reference;
    }

    /**
     * Method to set the name
     *
     * @param name name
     */
    public void setName(String name) {
        if (name != null) {
            this.name = name.trim();
        }
    }

    /**
     * Method to get the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Method to set the id
     *
     * @param id id
     */
    public void setId(String id) {
        if (id != null) {
            this.id = id.trim();
        }
    }

    /**
     * Method to get the id
     *
     * @return the id
     */
    public String getId() {
        return id != null ? id : reference;
    }

    /**
     * Method to set enabled
     *
     * @param enabled enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Method to get enabled
     *
     * @return true if enabled, else false
     */
    public boolean isEnabled() {
        return enabled && job.isEnabled();
    }

    /**
     * Method to get environment variables
     *
     * @return the map of environment variables
     */
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * Method to get properties
     *
     * @return the map of properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Method to get options
     *
     * @return the map of options
     */
    public Map<String, String> getOptions() {
        return options;
    }

    /**
     * Method to set the shell type
     *
     * @param shell shellType
     */
    public void setShellType(Shell shell) {
        this.shell = shell;
    }

    /**
     * Method to get the shell type
     *
     * @return the shell type
     */
    public Shell getShellType() {
        return shell;
    }

    /**
     * Method to set the working directory
     *
     * @param workingDirectory workingDirectory
     */
    public void setWorkingDirectory(String workingDirectory) {
        if (workingDirectory != null && !workingDirectory.trim().isEmpty()) {
            this.workingDirectory = workingDirectory.trim();
        }
    }

    /**
     * Method to get the working directory
     *
     * @return the working directory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Method to get the list of rus
     *
     * @return the command to run
     */
    public List<Run> getRuns() {
        return runs;
    }

    /**
     * Method to set the exit code
     *
     * @param exitCode exitCode
     */
    private void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public void execute(Mode mode, Console console) {
        if (mode == Mode.ENABLED) {
            stopwatch.reset();

            console.log(this);

            if (isEnabled()) {
                Iterator<Run> iterator = getRuns().iterator();

                while (iterator.hasNext()) {
                    Run run = iterator.next();
                    run.execute(Mode.ENABLED, console);
                    if (run.getExitCode() != 0) {
                        break;
                    }
                }

                while (iterator.hasNext()) {
                    iterator.next().execute(Mode.SKIPPED_OR_DISABLED, console);
                }
            }

            getRuns().stream()
                    .filter(run -> run.getExitCode() != 0)
                    .findFirst()
                    .ifPresent(run -> setExitCode(run.getExitCode()));

            if (isEnabled()) {
                console.log(
                        "%s exit-code=[%d] ms=[%d]",
                        this, getExitCode(), stopwatch.elapsedTime().toMillis());
            }
        } else {
            console.log("%s", this);
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return format("@step name=[%s] id=[%s] ref=[%s]", getName() == null ? "" : getName(), getId(), getReference());
    }
}
