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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.verifyica.pipeliner.engine.GeneratorException;
import org.verifyica.pipeliner.engine.Instruction;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.support.Precondition;

/**
 * Class to generate directive instructions.
 */
public class DirectiveGenerator {

    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectiveGenerator.class);

    /**
     * Map of directive prefixes to their factory functions
     */
    private final Map<String, Function<String, Instruction>> directiveFactory;

    /**
     * Constructor
     */
    public DirectiveGenerator() {
        LOGGER.trace("DirectiveGenerator()");

        // Create a map of directive factories
        this.directiveFactory = new LinkedHashMap<>();

        // Register the directive factories
        this.directiveFactory.put(PrintInfo.PREFIX, PrintInfo::of);
        this.directiveFactory.put(PrintWarning.PREFIX, PrintWarning::of);
        this.directiveFactory.put(PrintError.PREFIX, PrintError::of);

        this.directiveFactory.put(ShaChecksum.PREFIX, ShaChecksum::of);
        this.directiveFactory.put(Extension.PREFIX, Extension::of);
        this.directiveFactory.put(Pipeline.PREFIX, Pipeline::of);
        this.directiveFactory.put(Print.PREFIX, Print::of);
    }

    /**
     * Generate a directive instruction from a line of text.
     *
     * @param line the line of text to parse
     * @param instructionConsumer the consumer to write the instructions to
     * @throws GeneratorException if an error occurs during generation
     */
    public void generate(String line, Consumer<Instruction> instructionConsumer) throws GeneratorException {
        LOGGER.trace("generate()");
        LOGGER.trace("line [%s]", line);

        Precondition.notNullAndNotBlank(line, "line is null", "line is blank");
        Precondition.notNull(instructionConsumer, "instructionConsumer is null");

        // For each directory factory
        for (Map.Entry<String, Function<String, Instruction>> entry : directiveFactory.entrySet()) {
            // Build the prefix
            String prefix = entry.getKey();

            // Check if the line starts with the prefix
            if (line.startsWith(prefix + " ")) {
                // Apply the factory function to create the instruction
                instructionConsumer.accept(entry.getValue().apply(line));

                // Return since we found a matching directive
                return;
            }
        }

        throw new GeneratorException("unknown directive [" + line + "]");
    }
}
