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

package org.verifyica.pipeliner.model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import org.verifyica.pipeliner.common.YamlStringConstructor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;

/** Class to implement PipelineModelFactory */
public class PipelineModelFactory {

    /** Constructor */
    public PipelineModelFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to parse a PipelineModel
     *
     * @param filename filename
     * @return a PipelineModel
     * @throws IOException IOException
     */
    public PipelineModel create(String filename) throws IOException {
        return create(new File(filename));
    }

    /**
     * Method to parse a PipelineModel
     *
     * @param file file
     * @return a PipelineModel
     * @throws IOException IOException
     */
    public PipelineModel create(File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            return create(reader);
        }
    }

    /**
     * Method to parse a PipelineModel
     *
     * @param reader reader
     * @return a PipelineModel
     * @throws IOException IOException
     */
    public PipelineModel create(Reader reader) throws IOException {
        try {
            RootNode rootNode = new Yaml(new YamlStringConstructor()).loadAs(reader, RootNode.class);
            PipelineModel pipelineModel = rootNode.getPipeline();
            pipelineModel.validate();
            return pipelineModel;
        } catch (MarkedYAMLException e) {
            throw new IOException("Exception parsing YAML", e);
        }
    }
}
