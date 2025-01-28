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

package org.verifyica.pipeliner.core;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.verifyica.pipeliner.core.support.YamlConstructor;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.parser.SyntaxException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.MarkedYAMLException;

/** Class to implement PipelineFactory */
public class PipelineFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineFactory.class);

    /**
     * Constructor
     */
    public PipelineFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to parse a Pipeline
     *
     * @param filename the filename
     * @return a pipeline
     * @throws SyntaxException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    public Pipeline createPipeline(String filename) throws SyntaxException, IOException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("creating pipeline from filename [%s]", filename);
        }

        return createPipeline(new File(filename));
    }

    /**
     * Method to parse a Pipeline
     *
     * @param file  this file
     * @return a pipeline
     * @throws SyntaxException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    public Pipeline createPipeline(File file) throws SyntaxException, IOException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("creating pipeline from file [%s]", file);
        }

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
            return createPipeline(bufferedReader);
        }
    }

    /**
     * Method to parse a Pipeline
     *
     * @param reader the reader
     * @return a pipeline
     * @throws SyntaxException if a syntax error occurs
     */
    public Pipeline createPipeline(Reader reader) throws SyntaxException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("creating pipeline from reader");
        }

        try {
            // Load the YAML file
            Root root = new Yaml(new YamlConstructor()).loadAs(new BufferedReader(reader), Root.class);

            // Get the pipeline
            Pipeline pipeline = root.getPipeline();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("pipeline created");
            }

            // Validate the pipeline
            pipeline.validate();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("pipeline validated");
            }

            return pipeline;
        } catch (MarkedYAMLException e) {
            Mark problemMark = e.getProblemMark();
            if (problemMark != null) {
                int line = problemMark.getLine() + 1;
                int column = problemMark.getColumn() + 1;
                throw new SyntaxException(format("YAML syntax error at line [%s] column [%s]", line, column));
            } else {
                throw new SyntaxException("YAML syntax error (location not available)");
            }
        }
    }
}
