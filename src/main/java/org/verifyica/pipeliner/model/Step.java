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

/** Class to implement Step */
public class Step extends Node {

    private List<String> commands;

    /**
     * Constructor
     */
    public Step() {
        super(Type.STEP);

        this.commands = new ArrayList<>();
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
