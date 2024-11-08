package org.verifyica.pipelines.model;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class WorkflowFactory {

    private WorkflowFactory() {
        // INTENTIONALLY BLANK
    }

    public static Workflow load(String filename) throws Throwable {
        Yaml yaml = new Yaml();
        
        try (InputStream inputStream = new FileInputStream(filename)) {
            Map<String, Object> yamlMap = yaml.load(inputStream);
            Map<String, Object> workflowMap = (Map<String, Object>) yamlMap.get("workflow");

            return yaml.loadAs(yaml.dump(workflowMap), Workflow.class);
        }
    }
}
