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

package org.verifyica.pipeliner.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.Verbosity;
import org.verifyica.pipeliner.Version;
import org.verifyica.pipeliner.model.Pipeline;
import org.verifyica.pipeliner.model.PipelineFactory;
import org.verifyica.pipeliner.model.ValidationError;
import org.verifyica.pipeliner.model.Validator;

/**
 * The Engine to execute a pipeline.
 */
public class Engine {

    /**
     * The context in which the instructions will be executed.
     */
    private final Context context;

    /**
     * Constructor
     *
     * @param context the context in which the instructions will be executed.
     */
    public Engine(Context context) {
        this.context = context;
    }

    /**
     * Execute the pipeline defined in the specified file.
     *
     * @param file the file containing the pipeline definition
     * @return the exit code
     * @throws EngineException if an error occurs during execution
     */
    public int execute(File file) throws EngineException {
        // Get the verbosity
        Verbosity verbosity = context.getConsole().getVerbosity();

        // Force the version to be set in the environment variables
        context.getEnvironmentVariables().put(Constants.PIPELINER_VERSION, Version.getVersion());

        // If the PIPELINER_TMP environment variable is not set
        if (!context.getEnvironmentVariables().containsKey(Constants.PIPELINER_TMP)) {
            // Set the PIPELINER_TMP environment variable to the system's temporary directory
            context.getEnvironmentVariables().put(Constants.PIPELINER_TMP, System.getProperty("java.io.tmpdir"));
        }

        // Initialize the exit code
        int exitCode = 0;

        try {
            // Create a pipeline factory
            PipelineFactory pipelineFactory = new PipelineFactory();

            // Create the pipeline from the specified filename
            Pipeline pipeline = pipelineFactory.createPipeline(file);

            // Create a validator to validate the pipeline
            Validator validator = new Validator();

            // Create a list to hold validation errors
            List<ValidationError> validationErrors = new ArrayList<>();

            // Validate the pipeline
            validator.validate(pipeline, validationErrors::add);

            // If we have validation errors
            if (!validationErrors.isEmpty()) {
                // For each validation error
                for (ValidationError error : validationErrors) {
                    // Print the error
                    context.getConsole().error("%s -> %s%n", error.getNode(), error.getMessage());
                }

                // Set exit code to indicate validation failure
                exitCode = 1;

                // Return the exit code
                return exitCode;
            }

            // Create a list to hold instructions
            List<Instruction> instructions = new ArrayList<>();

            // Create a generator to generate instructions
            Generator generator = new Generator();

            // Generate the instructions for the pipeline
            generator.generate(pipeline, instructions::add);

            // Create an interpreter to execute the instructions
            Interpreter interpreter = new Interpreter(context);

            // Execute the instructions
            interpreter.execute(instructions);

            // Return success exit code
            return 0;
        } catch (ExitException e) {
            // Get the exit code from the StopException
            exitCode = e.getExitCode();

            // Get a descending iterator for the frames
            Iterator<Frame> frameIterator = context.getFrames().descendingIterator();

            while (frameIterator.hasNext()) {
                // Get the next frame from the iterator
                Frame frame = frameIterator.next();

                // Set the status based on the exit code
                String status = exitCode == 0 ? "success" : "failure";

                // Get the elapsed time from the frame's stopwatch as milliseconds
                String milliseconds =
                        String.format("%.4f", frame.getStopwatch().elapsedTime().toNanos() / 1_000_000.0f);

                // Print the frame information
                context.getConsole().error("%s status=[%s] ms=[%s]", frame.toConsoleString(), status, milliseconds);
            }

            // Return the exit code
            return exitCode;
        } catch (InterpreterException e) {
            // Get a descending iterator for the frames
            Iterator<Frame> frameIterator = context.getFrames().descendingIterator();

            while (frameIterator.hasNext()) {
                // Get the next frame from the iterator
                Frame frame = frameIterator.next();

                // Get the elapsed time from the frame's stopwatch as milliseconds
                String milliseconds =
                        String.format("%.4f", frame.getStopwatch().elapsedTime().toNanos() / 1_000_000.0f);

                // Print the frame information
                context.getConsole().error("%s status=[%s] ms=[%s]", frame.toConsoleString(), "failure", milliseconds);
            }

            // Set exit code to indicate execution failure
            exitCode = 1;

            // Return the exit code
            return exitCode;
        } catch (Throwable t) {
            throw new EngineException("engine exception", t);
        }
    }
}
