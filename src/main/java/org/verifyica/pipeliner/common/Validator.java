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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/** Class to implement Validator */
@SuppressWarnings({"unchecked", "PMD.UnusedLocalVariable"})
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

    public Validator validateCondition(boolean condition, String message) throws ValidatorException {
        return validateCondition(condition, of(message));
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

    public Validator validateNotNull(Object object, String message) throws ValidatorException {
        return validateNotNull(object, of(message));
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

    public Validator validateNotBlank(String string, String message) throws ValidatorException {
        return validateNotBlank(string, of(message));
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

    public Validator validateIsBoolean(Object object, String message) throws ValidatorException {
        return validateIsBoolean(object, of(message));
    }

    public Validator validateIsBoolean(Object object, Supplier<String> messageSupplier) throws ValidatorException {
        if (!(object instanceof String)) {
            throw new ValidatorException(messageSupplier.get());
        }

        String string = ((String) object).trim().toLowerCase(Locale.US);

        switch (string) {
            case "true":
            case "yes":
            case "y":
            case "on":
            case "1":
            case "false":
            case "no":
            case "n":
            case "off":
            case "0": {
                break;
            }
            default: {
                throw new ValidatorException(messageSupplier.get());
            }
        }

        return this;
    }

    public Validator validateIsString(Object object, String message) throws ValidatorException {
        return validateIsString(object, of(message));
    }

    public Validator validateIsString(Object object, Supplier<String> messageSupplier) throws ValidatorException {
        if (!(object instanceof String)) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    public Validator validateIsList(Object object, String message) throws ValidatorException {
        return validateIsList(object, of(message));
    }

    public Validator validateIsList(Object object, Supplier<String> messageSupplier) throws ValidatorException {
        try {
            List<Object> list = (List<Object>) object;
        } catch (ClassCastException e) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    public Validator validateIsMap(Object object, String message) throws ValidatorException {
        return validateIsMap(object, of(message));
    }

    public Validator validateIsMap(Object object, Supplier<String> messageSupplier) throws ValidatorException {
        try {
            Map<String, Object> map = (Map<String, Object>) object;
        } catch (ClassCastException c) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    public Validator validateId(String string, String message) throws ValidatorException {
        return validateId(string, of(message));
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

    public Validator validateProperty(String string, String message) throws ValidatorException {
        return validateProperty(string, of(message));
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

    public Validator validateEnvironmentVariable(String string, String message) throws ValidatorException {
        return validateEnvironmentVariable(string, of(message));
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

    public Validator validateFile(File file, String message) throws ValidatorException {
        return validateFile(file, of(message));
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

    public Validator validateDirectory(File file, String message) throws ValidatorException {
        return validateDirectory(file, of(message));
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

    private static MessageSupplier of(String message) {
        return new MessageSupplier(message);
    }
}
