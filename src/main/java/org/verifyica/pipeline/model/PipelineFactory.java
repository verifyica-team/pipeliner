/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.pipeline.model;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unchecked")
public class PipelineFactory {

    private static final String PIPELINE = "pipeline";

    private PipelineFactory() {
        // INTENTIONALLY BLANK
    }

    public static Pipeline load(String filename) throws Throwable {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = new FileInputStream(filename)) {
            Map<String, Object> yamlMap = yaml.load(inputStream);
            Map<String, Object> pipelineMap = (Map<String, Object>) yamlMap.get(PIPELINE);

            return yaml.loadAs(yaml.dump(pipelineMap), Pipeline.class);
        }
    }
}
