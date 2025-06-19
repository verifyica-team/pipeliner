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

package org.verifyica.pipeliner.engine.instructions.directives;

import java.io.File;
import java.nio.file.Paths;
import org.verifyica.pipeliner.engine.Context;
import org.verifyica.pipeliner.engine.Engine;
import org.verifyica.pipeliner.engine.EngineException;
import org.verifyica.pipeliner.engine.Instruction;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.support.PeekIterator;
import org.verifyica.pipeliner.support.Precondition;

/**
 * Instruction to execute a pipeline.
 */
public class Pipeline implements Instruction {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);

    /**
     * Prefix for the directive
     */
    public static final String PREFIX = "--pipeline";

    /**
     * The instruction line
     */
    private final String line;

    /**
     * Constructor
     *
     * @param line the line
     */
    private Pipeline(String line) {
        this.line = line;
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("line = [%s]", line);

        Precondition.notNull(context, "context is null");
        Precondition.notNull(peekIterator, "peekIterator is null");

        // Get the filename
        String filename = line.substring((PREFIX + " ").length()).trim();

        LOGGER.trace("filename [%s]", filename);

        // Resolve the filename
        String resolvedFilename = context.resolveAllVariables(filename);

        LOGGER.trace("resolvedFilename [%s]", resolvedFilename);

        // Get the working directory
        String workingDirectory = context.getWorkingDirectory();

        LOGGER.trace("workingDirectory [%s]", workingDirectory);

        // Get the first pipeline filename
        File file = Paths.get(workingDirectory, resolvedFilename).toFile();

        LOGGER.trace("file [%s]", file.getAbsolutePath());

        // Validate the file
        // validateFile(file);

        // TODO check/read IPC variables

        // TODO set environment variables from command line options

        // TODO set variables from command line options

        // Create the engine for execution
        Engine engine = new Engine(context);

        // Execute the engine and return the exit code
        int exitCode = engine.execute(file);

        if (exitCode != 0) {
            throw new EngineException("execution failed");
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + " { line [" + line + "] }";
    }

    /**
     * Factory method to create a new instance of PrintDirective.
     *
     * @param line the line
     * @return a new PrintDirective instance
     */
    public static Pipeline of(String line) {
        return new Pipeline(line);
    }
}
