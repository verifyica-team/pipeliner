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

import java.util.HashSet;
import java.util.Set;
import org.verifyica.pipeliner.core.PipelineDefinitionException;
import org.verifyica.pipeliner.core.Step;

/** Class to implement ExecutableFactory */
public class ExecutableFactory {

    /** Constant */
    public static final String DIRECTIVE_PREFIX = "--";

    /** Constant */
    public static final String PIPELINE_DIRECTIVE_PREFIX = "--pipeline";

    /** Constant */
    public static final String EXTENSION_DIRECTIVE_PREFIX = "--extension";

    private static final Set<String> SUPPORTED_DIRECTIVE_PREFIXES;

    static {
        SUPPORTED_DIRECTIVE_PREFIXES = new HashSet<>();
        SUPPORTED_DIRECTIVE_PREFIXES.add(PIPELINE_DIRECTIVE_PREFIX);
        SUPPORTED_DIRECTIVE_PREFIXES.add(EXTENSION_DIRECTIVE_PREFIX);
    }

    /**
     * Constructor
     */
    private ExecutableFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to check if a command is supported
     *
     * @param command the command
     * @return true if the command is supported, else false
     */
    public static boolean isSupported(String command) {
        if (command.startsWith(DIRECTIVE_PREFIX)) {
            // Get the directive command prefix
            String directiveCommandPrefix = getDirectiveCommandPrefix(command);

            // Check if the directive command prefix is supported
            return SUPPORTED_DIRECTIVE_PREFIXES.contains(directiveCommandPrefix);
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
        if (command.startsWith(DIRECTIVE_PREFIX)) {
            if (command.startsWith(PIPELINE_DIRECTIVE_PREFIX)) {
                return new DefaultExecutable(step, command);
            }

            if (command.startsWith(EXTENSION_DIRECTIVE_PREFIX)) {
                return new ExtensionExecutable(step, command);
            }
        } else {
            return new DefaultExecutable(step, command);
        }

        throw new PipelineDefinitionException(format("%s -> unknown directive [%s]", step, command));
    }

    /**
     * Method to get the directive command prefix
     *
     * @param command the command
     * @return the directive command prefix
     */
    private static String getDirectiveCommandPrefix(String command) {
        int spaceIndex = command.indexOf(' ');
        return spaceIndex == -1 ? command : command.substring(0, spaceIndex);
    }
}
