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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.verifyica.pipeliner.model.support.YamlConstructor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;

/** Class to implement PipelineFactory */
public class PipelineFactory {

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
     * @throws PipelineDefinitionException if a pipeline definition error occurs
     * @throws IOException if an I/O error occurs
     */
    public Pipeline createPipeline(String filename) throws PipelineDefinitionException, IOException {
        return createPipeline(new File(filename));
    }

    /**
     * Method to parse a Pipeline
     *
     * @param file  this file
     * @return a pipeline
     * @throws PipelineDefinitionException if a pipeline definition error occurs
     * @throws IOException if an I/O error occurs
     */
    public Pipeline createPipeline(File file) throws PipelineDefinitionException, IOException {
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
     * @throws PipelineDefinitionException if a pipeline definition error occurs
     */
    public Pipeline createPipeline(Reader reader) throws PipelineDefinitionException {
        try {
            // Load the YAML file
            Root root = new Yaml(new YamlConstructor()).loadAs(new BufferedReader(reader), Root.class);

            // Return the pipeline
            return root.getPipeline();
        } catch (MarkedYAMLException e) {
            throw new PipelineDefinitionException("YAML syntax error", e);
        }
    }
}
