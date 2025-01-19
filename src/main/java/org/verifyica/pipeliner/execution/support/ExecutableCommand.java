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

package org.verifyica.pipeliner.execution.support;

/** Interface to implement ExecutableCommand */
public interface ExecutableCommand {

    /**
     * Method to execute
     *
     * @param timeoutMinutes the timeout Minutes
     * @throws Throwable if an error occurs
     */
    void execute(int timeoutMinutes) throws Throwable;

    /**
     * Method to get the exit code
     *
     * @return the exit code
     */
    int getExitCode();

    /**
     * Method to get the output
     *
     * @return the output
     */
    String getProcessOutput();
}
