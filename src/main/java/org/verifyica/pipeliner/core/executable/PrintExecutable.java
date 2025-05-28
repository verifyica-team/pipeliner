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

import java.util.Map;
import java.util.TreeMap;
import org.verifyica.pipeliner.Environment;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.Step;
import org.verifyica.pipeliner.core.support.Resolver;

/** Class to implement PrintExecutable */
public class PrintExecutable extends AbstractExecutable {

    private static final int INDEX = "--print ".length();

    /**
     * Constructor
     *
     * @param step        the step
     * @param commandLine the command line
     */
    public PrintExecutable(Step step, String commandLine) {
        super(step, commandLine);
    }

    @Override
    public int execute(Context context) throws Throwable {
        String message = getCommandLine().substring(INDEX);

        // Resolve variables
        Map<String, String> variables = Resolver.resolveVariables(context.getVariables());

        // Create the environment variables
        Map<String, String> environmentVariables = new TreeMap<>(Environment.getenv());

        // Resolve environment variables
        environmentVariables.putAll(Resolver.resolveEnvironmentVariables(context.getEnvironmentVariables(), variables));

        String resolveMessage = Resolver.resolveAllVariables(environmentVariables, variables, message);

        context.getConsole().print("> " + resolveMessage);

        return 0;
    }
}
