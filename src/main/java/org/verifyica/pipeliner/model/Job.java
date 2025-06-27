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

package org.verifyica.pipeliner.model;

import java.util.ArrayList;
import java.util.List;

/** Class to implement Job */
public class Job extends Node {

    private final List<Step> steps;

    /**
     * Constructor
     */
    public Job() {
        super(Type.JOB);
        steps = new ArrayList<>();
    }

    /**
     * Method to set the list of steps
     *
     * @param steps this list of steps
     */
    public void setSteps(List<Step> steps) {
        if (steps != null) {
            this.steps.clear();
            this.steps.addAll(steps);
        }
    }

    /**
     * Method to get the list of steps
     *
     * @return the list of steps
     */
    public List<Step> getSteps() {
        return steps;
    }
}
