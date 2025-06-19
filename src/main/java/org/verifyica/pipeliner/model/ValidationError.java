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

package org.verifyica.pipeliner.model;

/**
 * Represents a validation error.
 */
public class ValidationError {

    private final Node node;
    private final String message;

    /**
     * Constructor
     *
     * @param node    the node where the error occurred
     * @param message the error message
     */
    private ValidationError(Node node, String message) {
        this.node = node;
        this.message = message;
    }

    /**
     * Gets the node where the error occurred.
     *
     * @return the node
     */
    public Node getNode() {
        return node;
    }

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }

    /**
     * Factory method to create a new ValidationError.
     *
     * @param node    the node where the error occurred
     * @param message the error message
     * @return a new ValidationError instance
     */
    public static ValidationError of(Node node, String message) {
        return new ValidationError(node, message);
    }
}
