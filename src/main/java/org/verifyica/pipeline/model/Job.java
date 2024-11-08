/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.pipeline.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Class to implement Job */
public class Job {

    private String id;
    private boolean enabled;
    private List<Property> property;
    private List<Step> step;
    private int exitCode;

    /** Constructor */
    public Job() {
        initialize();
    }

    /**
     * Method to initialize the job
     */
    private void initialize() {
        id = UUID.randomUUID().toString();
        enabled = true;
    }

    /**
     * Method to set the id
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Method to get the id
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Method to set the list of properties
     *
     * @param property property
     */
    public void setProperty(List<Property> property) {
        this.property = new ArrayList<>(new LinkedHashSet<>(property));
    }

    /**
     * Method to get the list of properties
     *
     * @return the list of properties
     */
    public List<Property> getProperty() {
        if (property == null) {
            return new ArrayList<>();
        } else {
            return property.stream()
                    .filter(property -> {
                        String name = property.getName();
                        return name != null && !name.trim().isEmpty();
                    })
                    .collect(Collectors.toList());
        }
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
    public boolean getEnabled() {
        return enabled;
    }

    /**
     * Method to set the list of steps
     *
     * @param step step
     */
    public void setStep(List<Step> step) {
        this.step = step;
    }

    /**
     * Method to get the list of steps
     *
     * @return the list of steps
     */
    public List<Step> getStep() {
        return step;
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

    @Override
    public String toString() {
        return "Job{" + "id='" + id + '\'' + '}';
    }
}
