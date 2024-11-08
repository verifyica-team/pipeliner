package org.verifyica.pipelines.model;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class PipelineFactory {

    private static final String PIPELINE = "pipeline";

    private PipelineFactory() {
        // INTENTIONALLY BLANK
    }

    public static Pipeline load(String filename) throws Throwable {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = new FileInputStream(filename)) {
            Map<String, Object> yamlMap = yaml.load(inputStream);
            Map<String, Object> workflowMap = (Map<String, Object>) yamlMap.get(PIPELINE);

            return yaml.loadAs(yaml.dump(workflowMap), Pipeline.class);
        }
    }
}
