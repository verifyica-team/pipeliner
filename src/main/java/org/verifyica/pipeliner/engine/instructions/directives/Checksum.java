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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.verifyica.pipeliner.engine.Context;
import org.verifyica.pipeliner.engine.Instruction;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.support.PeekIterator;
import org.verifyica.pipeliner.support.QuotedStringTokenizer;
import org.verifyica.pipeliner.support.ShaChecksum;
import org.verifyica.pipeliner.support.SyntaxException;

/**
 * Instruction to check the checksum of a file.
 */
public class Checksum implements Instruction {

    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Checksum.class);

    /**
     * Prefix for the directive
     */
    public static final String PREFIX = "--sha-checksum";

    /**
     * The instruction line
     */
    private final String line;

    /**
     * Constructor
     *
     * @param line the instruction line
     */
    private Checksum(String line) {
        this.line = line.trim();
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace(line);

        if (context.getConsole().getVerbosity().isNormal()) {
            context.getConsole().println("@command %s", line);
        }

        // Resolve the line
        String resolvedLine = context.resolveAllVariables(line);

        LOGGER.trace("resolvedLine [%s]", resolvedLine);

        // Get the working directory
        String workingDirectory = context.getWorkingDirectory();

        LOGGER.trace("workingDirectory [%s]", workingDirectory);

        // Parse the line into tokens
        List<String> tokens = QuotedStringTokenizer.tokenize(resolvedLine);

        if (tokens.size() != 3) {
            throw new SyntaxException("invalid syntax for " + PREFIX + " directive [" + line + "]");
        }

        // Get the filename
        String filename = tokens.get(1);

        LOGGER.trace("filename [%s]", filename);

        // Get the expected checksum
        String expectedChecksum = tokens.get(2);

        LOGGER.trace("checksum [%s]");

        // Get the algorithm from the checksum
        ShaChecksum.Algorithm algorithm = ShaChecksum.getAlgorithm(expectedChecksum);

        LOGGER.trace("algorithm [%s]", algorithm);

        Path filePath = Paths.get(workingDirectory, filename);

        LOGGER.trace("filePath [%s]", filePath);

        // Get the checksum of the file
        String actualChecksum = ShaChecksum.checksum(algorithm, filePath);

        LOGGER.trace("actualChecksum [%s]", actualChecksum);

        if (!actualChecksum.equals(expectedChecksum)) {
            throw new SyntaxException("invalid checksum for file [" + filename + "]");
        }

        if (context.getConsole().getVerbosity().isNormal()) {
            context.getConsole().println("@output checksum is valid");
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + " { line [" + line + "] }";
    }

    /**
     * Factory method to create a new instance of Checksum.
     *
     * @param line the line
     * @return a new Checksum instance
     */
    public static Checksum of(String line) {
        return new Checksum(line);
    }
}
