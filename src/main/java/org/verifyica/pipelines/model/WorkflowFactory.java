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
        try (InputStream inputStream = new FileInputStream("workflow.yaml")) {
            // Load the YAML as a Map and extract the 'workflow' part
            Map<String, Object> yamlMap = yaml.load(inputStream);
            Map<String, Object> workflowMap = (Map<String, Object>) yamlMap.get("workflow");

            // Convert the extracted 'workflow' map into a Workflow object
            return yaml.loadAs(yaml.dump(workflowMap), Workflow.class);
        }
    }
}
