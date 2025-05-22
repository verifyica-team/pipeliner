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

import java.util.HashMap;
import java.util.Map;
import org.verifyica.pipeliner.core.PipelineDefinitionException;
import org.verifyica.pipeliner.core.Step;

/** Class to implement ExecutableFactory */
public class ExecutableFactory {

    /** Constant */
    public static final String DIRECTIVE_PREFIX = "--";

    /** Constant */
    public static final String PIPELINE_DIRECTIVE = DIRECTIVE_PREFIX + "pipeline";

    /** Constant */
    public static final String EXTENSION_DIRECTIVE = DIRECTIVE_PREFIX + "extension";

    /** Constant */
    public static final String SSH_DIRECTIVE = DIRECTIVE_PREFIX + "ssh";

    /** Constant */
    public static final String SCP_DIRECTIVE = DIRECTIVE_PREFIX + "scp";

    private static final Map<String, ExecutableConstructor> EXECUTABLE_CONSTRUCTORS;

    static {
        // Initialize the map of directive constructors
        EXECUTABLE_CONSTRUCTORS = new HashMap<>();
        EXECUTABLE_CONSTRUCTORS.put(PIPELINE_DIRECTIVE, DefaultExecutable::new);
        EXECUTABLE_CONSTRUCTORS.put(EXTENSION_DIRECTIVE, ExtensionExecutable::new);
        EXECUTABLE_CONSTRUCTORS.put(SSH_DIRECTIVE, SshExecutable::new);
        EXECUTABLE_CONSTRUCTORS.put(SCP_DIRECTIVE, ScpExecutable::new);
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
     * @param step the step
     * @param commandLine the command line
     * @throws PipelineDefinitionException if the directive is unknown
     */
    public static void validate(Step step, String commandLine) {
        if (commandLine.startsWith(DIRECTIVE_PREFIX)) {
            // Get the directive
            String directive = getDirective(commandLine);

            // Validate the directive
            if (!EXECUTABLE_CONSTRUCTORS.containsKey(directive)) {
                String message = directive.equals(commandLine)
                        ? format("%s -> unknown directive [%s]", step, directive)
                        : format("%s -> unknown directive [%s] command line [%s]", step, directive, commandLine);

                throw new PipelineDefinitionException(message);
            }
        }
    }

    /**
     * Method to create an executable
     *
     * @param step the step
     * @param commandLine the command line
     * @return an executable
     * @throws PipelineDefinitionException if the directive is unknown
     */
    public static Executable createExecutable(Step step, String commandLine) {
        // Defensive code
        validate(step, commandLine);

        if (commandLine.startsWith(DIRECTIVE_PREFIX)) {
            // Get the directive
            String directive = getDirective(commandLine);

            // Construct the executable for the directive
            return EXECUTABLE_CONSTRUCTORS.get(directive).construct(step, commandLine);
        } else {
            // Construct the default executable
            return new DefaultExecutable(step, commandLine);
        }
    }

    /**
     * Method to get the directive
     *
     * @param commandLine the command line
     * @return the directive
     */
    private static String getDirective(String commandLine) {
        int spaceIndex = commandLine.indexOf(' ');
        return spaceIndex == -1 ? commandLine : commandLine.substring(0, spaceIndex);
    }
}
