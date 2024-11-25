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

    /**
     * Method to validate a condition
     *
     * @param condition condition
     * @param message message
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator condition(boolean condition, String message) throws ValidatorException {
        return condition(condition, of(message));
    }

    /**
     * Method to validate a condition
     *
     * @param condition condition
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator condition(boolean condition, Supplier<String> messageSupplier) throws ValidatorException {
        if (!condition) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate an Object is not null
     *
     * @param object object
     * @param message message
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator notNull(Object object, String message) throws ValidatorException {
        return notNull(object, of(message));
    }

    /**
     * Method to validate an Object is not null
     *
     * @param object object
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator notNull(Object object, Supplier<String> messageSupplier) throws ValidatorException {
        if (object == null) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate an Object is not null
     *
     * @param string string
     * @param message message
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator notBlank(String string, String message) throws ValidatorException {
        return notBlank(string, of(message));
    }

    /**
     * Method to validate a String is not blank
     *
     * @param string string
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator notBlank(String string, Supplier<String> messageSupplier) throws ValidatorException {
        if (string.trim().isEmpty()) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate an Object is a boolean
     *
     * @param object object
     * @param message message
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isBoolean(Object object, String message) throws ValidatorException {
        return isBoolean(object, of(message));
    }

    /**
     * Method to validate an Object is a boolean
     *
     * @param object object
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isBoolean(Object object, Supplier<String> messageSupplier) throws ValidatorException {
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

    /**
     * Method to validate an Object is a String
     *
     * @param object object
     * @param message message
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isString(Object object, String message) throws ValidatorException {
        return isString(object, of(message));
    }

    /**
     * Method to validate an Object is a String
     *
     * @param object object
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isString(Object object, Supplier<String> messageSupplier) throws ValidatorException {
        if (!(object instanceof String)) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate an Object is a List
     *
     * @param object object
     * @param message message
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isList(Object object, String message) throws ValidatorException {
        return isList(object, of(message));
    }

    /**
     * Method to validate an Object is a List
     *
     * @param object object
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isList(Object object, Supplier<String> messageSupplier) throws ValidatorException {
        try {
            List<Object> list = (List<Object>) object;
        } catch (ClassCastException e) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate an Object is a Map
     *
     * @param object object
     * @param message message
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isMap(Object object, String message) throws ValidatorException {
        return isMap(object, of(message));
    }

    /**
     * Method to validate an Object is a Map
     *
     * @param object object
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isMap(Object object, Supplier<String> messageSupplier) throws ValidatorException {
        try {
            Map<String, Object> map = (Map<String, Object>) object;
        } catch (ClassCastException c) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate a String is a valid id
     *
     * @param string string
     * @param message message
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isValidId(String string, String message) throws ValidatorException {
        return isValidId(string, of(message));
    }

    /**
     * Method to validate a String is a valid id
     *
     * @param string string
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isValidId(String string, Supplier<String> messageSupplier) throws ValidatorException {
        if (!validIdPattern.matcher(string.trim()).matches()) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate a String is a valid property
     *
     * @param string string
     * @param message message
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isValidProperty(String string, String message) throws ValidatorException {
        return isValidProperty(string, of(message));
    }

    /**
     * Method to validate a String is a valid property
     *
     * @param string string
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isValidProperty(String string, Supplier<String> messageSupplier) throws ValidatorException {
        if (!validPropertyPattern.matcher(string.trim()).matches()) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to validate a String is a valid environment variable
     *
     * @param string string
     * @param message message
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isValidEnvironmentVariable(String string, String message) throws ValidatorException {
        return isValidEnvironmentVariable(string, of(message));
    }

    /**
     * Method to validate a String is a valid environment variable
     *
     * @param string string
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isValidEnvironmentVariable(String string, Supplier<String> messageSupplier)
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
     * @param message message
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isValidFile(File file, String message) throws ValidatorException {
        return isValidFile(file, of(message));
    }

    /**
     * Method to validate a File exists, is a file, and accessible
     *
     * @param file file
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isValidFile(File file, Supplier<String> messageSupplier) throws ValidatorException {
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
     * @param directory directory
     * @param message message
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isValidDirectory(File directory, String message) throws ValidatorException {
        return isValidDirectory(directory, of(message));
    }

    /**
     * Method to validate a File exists, is a directory, and accessible
     *
     * @param directory directory
     * @param messageSupplier messageSupplier
     * @return this
     * @throws ValidatorException ValidatorException
     */
    public Validator isValidDirectory(File directory, Supplier<String> messageSupplier) throws ValidatorException {
        if (!directory.exists()) {
            throw new ValidatorException(messageSupplier.get());
        }

        if (!directory.isDirectory()) {
            throw new ValidatorException(messageSupplier.get());
        }

        if (!directory.canRead()) {
            throw new ValidatorException(messageSupplier.get());
        }

        return this;
    }

    /**
     * Method to create a MessageSupplier using a String
     *
     * @param message message
     * @return a MessageSupplier
     */
    private static MessageSupplier of(String message) {
        return new MessageSupplier(message);
    }
}
