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

package org.verifyica.pipeliner;

/** Class to implement PipelinerDebug */
class PipelinerDebug {

    private static final String PWD = "PWD";

    private static final String PIPELINER = "/pipeliner";

    /**
     * Main method
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        if (Environment.getenv(Constants.PIPELINER_HOME) == null) {
            Environment.setenv(Constants.PIPELINER_HOME, Environment.getenv(PWD));
        }

        if (Environment.getenv(Constants.PIPELINER) == null) {
            Environment.setenv(Constants.PIPELINER, Environment.getenv(Constants.PIPELINER_HOME) + PIPELINER);
        }

        // Environment.setenv(Constants.PIPELINER_LOG_LEVEL, "trace");

        // Environment.getenv().forEach((name, value) -> System.out.printf("environment variable [%s] = [%s]%n", name,
        // value));

        // Set the arguments to run
        String[] arguments = new String[] {"package.yaml"};

        // Run Pipeliner
        PipelinerCLI.main(arguments);
    }
}
