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

package org.verifyica.pipeliner.common;

import java.io.File;
import java.util.regex.Pattern;

public class Validator {

    private static final Pattern VALID_PROPERTY = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_.-]*$");
    private static final Pattern VALID_ENVIRONMENT_VARIABLE = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private Validator() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to validate a property
     *
     * @param property property
     * @throws ValidatorException ValidatorException
     */
    public static void validateProperty(String property) throws ValidatorException {
        if (property == null) {
            ValidatorException.propagate("property is null");
        }

        if (property.trim().isEmpty()) {
            ValidatorException.propagate("property is empty");
        }

        if (property.trim().getBytes().length > 255) {
            ValidatorException.propagate("property [%s] exceeds maximum length of 255 bytes", property);
        }

        if (!VALID_PROPERTY.matcher(property.trim()).matches()) {
            ValidatorException.propagate("property [%s] contains invalid characters", property);
        }
    }

    /**
     * Method to validate an environment variable
     *
     * @param environmentVariable environmentVariable
     * @throws ValidatorException ValidatorException
     */
    public static void validateEnvironmentVariable(String environmentVariable) throws ValidatorException {
        if (environmentVariable == null) {
            ValidatorException.propagate("environment variable is null");
        }

        if (environmentVariable.trim().isEmpty()) {
            ValidatorException.propagate("environment variable is empty");
        }

        if (environmentVariable.trim().getBytes().length > 255) {
            ValidatorException.propagate(
                    "environment variable [%s] exceeds maximum length of 255 bytes", environmentVariable);
        }

        if (!VALID_ENVIRONMENT_VARIABLE.matcher(environmentVariable.trim()).matches()) {
            ValidatorException.propagate("environment variable [%s] contains invalid characters", environmentVariable);
        }
    }

    public static void validateFile(File file) throws ValidatorException {
        if (!file.exists()) {
            ValidatorException.propagate("file [%s] does not exit", file.getAbsolutePath());
        }

        if (!file.isFile()) {
            ValidatorException.propagate("file [%s] is not a file", file.getAbsolutePath());
        }

        if (!file.canRead()) {
            ValidatorException.propagate("file [%s] is not accessible", file.getAbsolutePath());
        }
    }

    public static void validateDirectory(File directory) throws ValidatorException {
        if (!directory.exists()) {
            ValidatorException.propagate("directory [%s] does not exit", directory.getAbsolutePath());
        }

        if (!directory.isDirectory()) {
            ValidatorException.propagate("directory [%s] is not a directory", directory.getAbsolutePath());
        }

        if (!directory.canRead()) {
            ValidatorException.propagate("directory [%s] is not accessible", directory.getAbsolutePath());
        }
    }
}
