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
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;

/** Class to implement ModelParser */
public class ModelParser {

    /** Constructor */
    public ModelParser() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to parse a Pipeline
     *
     * @param filename filename
     * @return a Pipeline
     * @throws IOException IOException
     */
    public Pipeline parse(String filename) throws IOException {
        return parse(new File(filename));
    }

    /**
     * Method to parse a Pipeline
     *
     * @param file file
     * @return a Pipeline
     * @throws IOException IOException
     */
    public Pipeline parse(File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            return parse(reader);
        }
    }

    /**
     * Method to parse a Pipeline
     *
     * @param reader reader
     * @return a Pipeline
     * @throws IOException IOException
     */
    public Pipeline parse(Reader reader) throws IOException {
        try {
            Root root = new Yaml(new YamlStringConstructor()).loadAs(reader, Root.class);
            Pipeline pipeline = root.getPipeline();
            pipeline.validate();
            return pipeline;
        } catch (MarkedYAMLException e) {
            throw new IOException("Exception parsing YAML", e);
        }
    }
}
