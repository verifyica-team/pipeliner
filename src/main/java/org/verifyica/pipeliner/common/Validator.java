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
import java.util.function.Supplier;
import java.util.regex.Pattern;

/** Class to implement Validator */
public class Validator {

    private final Pattern validIdPattern;
    private final Pattern validPropertyPattern;
    private final Pattern validEnvironmentVariablePattern;

    /** Constructor */
    public Validator() {
        validIdPattern = Pattern.compile("^[a-zA-Z0-9-_]*$");
        validPropertyPattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_.-]*$");
        validEnvironmentVariablePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    /**
     * Method to validate a condition
     *
     * @param condition condition
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator validateCondition(boolean condition, Supplier<String> messageSupplier) throws ValidatorException {
        if (!condition) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate an Object is not null
     *
     * @param object object
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator validateNotNull(Object object, Supplier<String> messageSupplier) throws ValidatorException {
        if (object == null) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate a String is not blank
     *
     * @param string string
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator validateNotBlank(String string, Supplier<String> messageSupplier) throws ValidatorException {
        if (string.trim().isEmpty()) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate a String is a valid id
     *
     * @param string string
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator validateId(String string, Supplier<String> messageSupplier) throws ValidatorException {
        if (!validIdPattern.matcher(string.trim()).matches()) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate a String is a valid property
     *
     * @param string string
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator validateProperty(String string, Supplier<String> messageSupplier) throws ValidatorException {
        if (!validPropertyPattern.matcher(string.trim()).matches()) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate a String is a valid environment variable
     *
     * @param string string
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator validateEnvironmentVariable(String string, Supplier<String> messageSupplier)
            throws ValidatorException {
        if (!validEnvironmentVariablePattern.matcher(string.trim()).matches()) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate a File exists, is a file, and accessible
     *
     * @param file file
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator validateFile(File file, Supplier<String> messageSupplier) throws ValidatorException {
        if (!file.exists()) {
            throw new ValidatorException(messageSupplier.get());
        }

        if (!file.isFile()) {
            throw new ValidatorException(messageSupplier.get());
        }

        if (!file.canRead()) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate a File exists, is a directory, and accessible
     *
     * @param file file
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator validateDirectory(File file, Supplier<String> messageSupplier) throws ValidatorException {
        if (!file.exists()) {
            throw new ValidatorException(messageSupplier.get());
        }

        if (!file.isDirectory()) {
            throw new ValidatorException(messageSupplier.get());
        }

        if (!file.canRead()) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }
}
