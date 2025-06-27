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

import static java.lang.String.format;

import java.util.List;
import java.util.UUID;
import org.verifyica.pipeliner.Verbosity;
import org.verifyica.pipeliner.engine.Context;
import org.verifyica.pipeliner.engine.Instruction;
import org.verifyica.pipeliner.engine.instructions.ExecuteCommand;
import org.verifyica.pipeliner.engine.instructions.RemoveVariable;
import org.verifyica.pipeliner.engine.instructions.SetWorkingDirectory;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.support.PeekIterator;
import org.verifyica.pipeliner.support.QuotedStringTokenizer;
import org.verifyica.pipeliner.support.SyntaxException;

/**
 * Instruction to execute an extension.
 */
public class Extension implements Instruction {

    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Extension.class);

    /**
     * Prefix for the directive
     */
    public static final String PREFIX = "--extension";

    /**
     * The instruction line
     */
    private final String line;

    /**
     * Constructor
     *
     * @param line the line
     */
    private Extension(String line) {
        this.line = line;
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("line [%s]", line);

        // Get the verbosity level
        Verbosity verbosity = context.getConsole().getVerbosity();

        // If the verbosity level is normal
        if (verbosity.isNormal()) {
            // Print the line to the console
            context.getConsole().println("@command %s", line);
        }

        // Set the verbosity level to quieter
        context.getConsole().setVerbosity(Verbosity.NONE);

        // Resolve the line
        String resolvedLine = context.resolveAllVariables(line);

        List<String> tokens = QuotedStringTokenizer.tokenize(resolvedLine);

        String filename;
        String expectedChecksum = null;

        switch (tokens.size()) {
            case 2: {
                filename = tokens.get(1).trim();
                break;
            }
            case 3: {
                filename = tokens.get(1).trim();
                expectedChecksum = tokens.get(2).trim();
                break;
            }
            default: {
                throw new SyntaxException("invalid syntax for " + PREFIX + " directive [" + line + "]");
            }
        }

        if (expectedChecksum != null) {
            try {
                context.getConsole().setVerbosity(Verbosity.NONE);
                ShaChecksum.of("--checksum " + filename + " " + expectedChecksum)
                        .execute(context, peekIterator);
            } finally {
                context.getConsole().setVerbosity(verbosity);
            }
        }

        // TODO validate the file exists and is readable

        // Create a safe filename by escaping double quotes
        String safeFilename = filename.replace("\"", "\\\"");

        // Create a temporary variable name
        String temporaryVariableName = UUID.randomUUID().toString().replaceAll("-", "");

        // Get the working directory
        String workingDirectory = context.getWorkingDirectory();

        try {
            context.getConsole().setVerbosity(Verbosity.NONE);

            // Create a temporary directory
            ExecuteCommand.of(format("--capture ${{ %s }} mktemp -d", temporaryVariableName))
                    .execute(context, peekIterator);

            // Change the permissions of the temporary directory
            ExecuteCommand.of(format("chmod go-rwx ${{ %s }}", temporaryVariableName))
                    .execute(context, peekIterator);

            // Get the lowercase filename
            String filenameLowerCase = filename.toLowerCase();

            // Use the correct command to extract the file based on its extension

            if (filenameLowerCase.endsWith(".tar.gz")) {
                // Extract file into the temporary directory
                ExecuteCommand.of(format("tar -xf \"%s\" -C ${{ %s }}", safeFilename, temporaryVariableName))
                        .execute(context, peekIterator);
            } else if (filenameLowerCase.endsWith(".tar.xz")) {
                // Extract the tar.xz file into the temporary directory
                ExecuteCommand.of(format("tar -xJf \"%s\" -C ${{ %s }}", safeFilename, temporaryVariableName))
                        .execute(context, peekIterator);
            } else if (filenameLowerCase.endsWith(".tar.bz2")) {
                // Extract the tar.bz2 file into the temporary directory
                ExecuteCommand.of(format("tar -xjf \"%s\" -C ${{ %s }}", safeFilename, temporaryVariableName))
                        .execute(context, peekIterator);
            } else {
                // Unzip the file into the temporary directory
                ExecuteCommand.of(format("unzip -q \"%s\" -d ${{ %s }}", safeFilename, temporaryVariableName))
                        .execute(context, peekIterator);
            }

            // Set the permissions on the run script
            ExecuteCommand.of(format("chmod +x ${{ %s }}/run.sh", temporaryVariableName))
                    .execute(context, peekIterator);

            // Set the working directory to the temporary directory
            SetWorkingDirectory.of(format("${{ %s }}", temporaryVariableName)).execute(context, peekIterator);

            // Run the extension script
            ExecuteCommand.of("./run.sh").execute(context, peekIterator);
        } finally {
            // Remove the temporary directory
            ExecuteCommand.of(format("rm -rf ${{ %s }}", temporaryVariableName)).execute(context, peekIterator);

            // Remove the temporary variable
            RemoveVariable.of(temporaryVariableName).execute(context, peekIterator);

            // Restore the original working directory
            SetWorkingDirectory.of(workingDirectory).execute(context, peekIterator);

            // Restore the verbosity level
            context.getConsole().setVerbosity(verbosity);
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
    public static Extension of(String line) {
        return new Extension(line);
    }
}
