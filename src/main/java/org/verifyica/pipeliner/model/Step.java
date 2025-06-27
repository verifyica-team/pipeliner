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

import java.util.List;
import org.verifyica.pipeliner.support.MultiLineParser;

/** Class to implement Step */
public class Step extends Node {

    private String run;
    private List<String> commands;

    /**
     * Constructor
     */
    public Step() {
        super(Type.STEP);
    }

    /**
     * Method to set the run
     *
     * @param run the run
     */
    public void setRun(String run) {
        if (run != null) {
            this.run = run.trim();
            this.commands = MultiLineParser.parse(run);
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

    /**
     * Method to get the list of commands
     *
     * @return the list of commands
     */
    public List<String> getCommands() {
        return commands;
    }
}
