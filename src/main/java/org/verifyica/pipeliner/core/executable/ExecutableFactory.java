/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.core.PipelineDefinitionException;
import org.verifyica.pipeliner.core.Step;

/** Class to implement ExecutableFactory */
public class ExecutableFactory {

    /**
     * Constructor
     */
    private ExecutableFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to check if a command is valid
     *
     * @param command the command
     * @return true if the command is valid, else false
     */
    public static boolean isSupported(String command) {
        if (command.startsWith(Constants.DIRECTIVE_COMMAND_PREFIX)) {
            return command.startsWith(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX)
                    || command.startsWith(Constants.EXTENSION_DIRECTIVE_COMMAND_PREFIX);
        } else {
            return true;
        }
    }

    /**
     * Method to create a command
     *
     * @param step the step
     * @param command the command
     * @return a command
     */
    public static Executable createExecutable(Step step, String command) {
        if (command.startsWith(Constants.DIRECTIVE_COMMAND_PREFIX)) {
            if (command.startsWith(Constants.PIPELINE_DIRECTIVE_COMMAND_PREFIX)) {
                return new DefaultExecutable(step, command);
            }
            if (command.startsWith(Constants.EXTENSION_DIRECTIVE_COMMAND_PREFIX)) {
                return new ExtensionExecutable(step, command);
            }
        } else {
            return new DefaultExecutable(step, command);
        }

        throw new PipelineDefinitionException(format("%s -> unknown directive [%s]", step, command));
    }
}
