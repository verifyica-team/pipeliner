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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Class to implement Step */
public class Step {

    /** ShellType */
    public enum ShellType {
        /** Invalid */
        INVALID,
        /** Unspecified */
        UNSPECIFIED,
        /** Bash */
        BASH,
        /** sh */
        SH
    }

    private final Job job;
    private final String location;
    private String name;
    private String id;
    private final Map<String, String> environmentVariables;
    private boolean enabled;
    private ShellType shellType;
    private String workingDirectory;
    private final List<Run> runs;
    private int exitCode;

    /**
     * Constructor
     *
     * @param job job
     * @param index index
     */
    public Step(Job job, int index) {
        this.job = job;
        this.location = job.getLocation() + "-step-" + index;
        this.enabled = true;
        this.environmentVariables = new LinkedHashMap<>();
        this.shellType = ShellType.BASH;
        this.workingDirectory = ".";
        this.runs = new ArrayList<>();
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
    public String getLocation() {
        return location;
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
        return id != null ? id : location;
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
     * Method to set environment variables
     *
     * @param environmentVariables environmentVariables
     */
    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables.putAll(environmentVariables);
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
     * Method to set the shell type
     *
     * @param shellType shellType
     */
    public void setShellType(ShellType shellType) {
        this.shellType = shellType;
    }

    /**
     * Method to get the shell type
     *
     * @return the shell type
     */
    public ShellType getShellType() {
        return shellType;
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
     * Method to set the list of runs
     *
     * @param runs runs
     */
    public void setRuns(List<Run> runs) {
        this.runs.addAll(runs);
    }

    /**
     * Method to get the command to run
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
    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Method to get the exit code
     *
     * @return the exit code
     */
    public int getExitCode() {
        return exitCode;
    }
}
