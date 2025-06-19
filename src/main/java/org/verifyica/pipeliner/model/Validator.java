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

import static java.lang.String.format;

import java.util.Map;
import java.util.function.Consumer;
import org.verifyica.pipeliner.model.support.Enabled;
import org.verifyica.pipeliner.model.support.EnvironmentVariable;
import org.verifyica.pipeliner.model.support.Variable;

/**
 * Validator class for validating a pipeline.
 */
public class Validator {

    private static final int MAX_VARIABLE_NAME_LENGTH = 64;

    /**
     * Constructor
     */
    public Validator() {
        // INTENTIONALLY BLANK
    }

    /**
     * Validates the given pipeline.
     *
     * @param pipeline the pipeline to validate
     * @param  consumer the consumer to handle validation errors
     */
    public void validate(Pipeline pipeline, Consumer<ValidationError> consumer) {
        pipeline.setType("pipeline");

        // Validate the pipeline
        validateNode(pipeline, consumer);

        // For each job in the pipeline
        for (Job job : pipeline.getJobs()) {
            job.setType("job");

            // Validate the job
            validateNode(job, consumer);

            // For each step in the job
            for (Step step : job.getSteps()) {
                step.setType("step");

                // Validate the step
                validateNode(step, consumer);
            }
        }
    }

    /**
     * Validates the given node.
     *
     * @param node the node to validate
     * @param consumer the consumer to handle validation errors
     */
    private void validateNode(Node node, Consumer<ValidationError> consumer) {
        // Validate the enabled property
        validateEnabled(node, consumer);

        // Validate the environment variable names
        validateEnvironmentVariables(node, consumer);

        // Validate the variable names
        validateVariables(node, consumer);
    }

    /**
     * Validates the enabled property of the given node.
     *
     * @param node the node to validate
     * @param consumer the consumer to handle validation errors
     */
    private void validateEnabled(Node node, Consumer<ValidationError> consumer) {
        // Get the enabled property
        String enabled = node.getEnabled();

        // If the enabled property is not null and is invalid
        if (enabled != null && Enabled.isInvalid(enabled)) {
            // Create a validation error
            consumer.accept(ValidationError.of(node, format("invalid enabled value [%s]", enabled)));
        }
    }

    /**
     * Validates the environment variables of the given node.
     *
     * @param node the node to validate
     * @param consumer the consumer to handle validation errors
     */
    private void validateEnvironmentVariables(Node node, Consumer<ValidationError> consumer) {
        // For each environment variable in the node
        for (Map.Entry<String, String> entry : node.getEnvironmentVariables().entrySet()) {
            // Get the environment variable name
            String name = entry.getKey();

            // if the environment variable name is invalid
            if (EnvironmentVariable.isInvalid(name)) {
                // Create a validation error
                consumer.accept(ValidationError.of(node, format("invalid environment variable name [%s]", name)));

                // Return early as we found an invalid environment variable name
                return;
            }

            // If the key length exceeds the maximum variable name length
            if (name.length() > MAX_VARIABLE_NAME_LENGTH) {
                // Create a validation error
                consumer.accept(ValidationError.of(
                        node,
                        format(
                                "environment variable name [%s] exceeds maximum length of %d characters",
                                name, MAX_VARIABLE_NAME_LENGTH)));
            }
        }
    }

    /**
     * Validates the variables of the given node.
     *
     * @param node the node to validate
     * @param consumer the consumer to handle validation errors
     */
    private void validateVariables(Node node, Consumer<ValidationError> consumer) {
        // For each variable in the node
        for (Map.Entry<String, String> entry : node.getVariables().entrySet()) {
            // Get the variable name
            String name = entry.getKey();

            // If the variable name is invalid
            if (Variable.isInvalid(name)) {
                // Create a validation error
                consumer.accept(ValidationError.of(node, format("invalid environment variable name [%s]", name)));

                // Return early as we found an invalid variable name
                return;
            }

            // If the variable name length exceeds the maximum variable name length
            if (name.length() > MAX_VARIABLE_NAME_LENGTH) {
                // Create a validation error
                consumer.accept(ValidationError.of(
                        node,
                        format(
                                "invalid variable name [%s] exceeds maximum length of %d characters",
                                name, MAX_VARIABLE_NAME_LENGTH)));
            }
        }
    }
}
