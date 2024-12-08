/*
 * Copyright (C) 2024-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.execution;

import java.util.Locale;
import org.verifyica.pipeliner.common.Stopwatch;
import org.verifyica.pipeliner.execution.support.Status;

/** Class to implement Executable */
public abstract class Executable {

    private final Stopwatch stopwatch;
    private int exitCode;

    /** Constructor */
    public Executable() {
        stopwatch = new Stopwatch();
    }

    /**
     * Method to execute
     */
    public abstract void execute();

    /**
     * Method to skip
     *
     * @param status status
     */
    public abstract void skip(Status status);

    /**
     * Method to get the Stopwatch
     *
     * @return the Stopwatch
     */
    protected Stopwatch getStopwatch() {
        return stopwatch;
    }

    /**
     * Method to set the exit code
     *
     * @param exitCode exitCode
     */
    protected void setExitCode(int exitCode) {
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

    /**
     * Method to decode the enable String
     *
     * @param string string
     * @return true if enabled, else false
     */
    protected boolean decodeEnabled(String string) {
        if (string == null) {
            return true;
        }

        switch (string.trim().toLowerCase(Locale.US)) {
            case "false":
            case "no":
            case "n":
            case "off":
            case "0": {
                return false;
            }
            case "true":
            case "yes":
            case "y":
            case "on":
            case "1":
            default: {
                return true;
            }
        }
    }
}
