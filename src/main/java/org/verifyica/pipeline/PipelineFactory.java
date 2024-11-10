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

package org.verifyica.pipeline;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.verifyica.pipeline.common.StringConstructor;
import org.verifyica.pipeline.common.YamlHelper;
import org.yaml.snakeyaml.Yaml;

/** Class to implement PipelineFactory */
@SuppressWarnings("unchecked")
public class PipelineFactory {

    /** Constructor */
    private PipelineFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to create a pipeline
     *
     * @param pipelineYamlFilename pipelineYamlFilename
     * @return a Pipeline
     * @throws Throwable Throwable
     */
    public static Pipeline createPipeline(String pipelineYamlFilename) throws Throwable {
        Yaml yaml = new Yaml(new StringConstructor());

        try (InputStream inputStream = new FileInputStream(pipelineYamlFilename)) {
            return parsePipeline(yaml.load(inputStream));
        }
    }

    private static Pipeline parsePipeline(Map<Object, Object> map) {
        Map<Object, Object> pipelineMap = (Map<Object, Object>) map.get("pipeline");

        Pipeline pipeline = new Pipeline();
        pipeline.setName(YamlHelper.asString(pipelineMap.get("name")));
        pipeline.setProperties(parseProperties(YamlHelper.asMap(pipelineMap.get("with"))));
        pipeline.setJobs(parseJobs(YamlHelper.asList(pipelineMap.get("jobs"))));

        return pipeline;
    }

    private static List<Job> parseJobs(List<Object> objects) {
        List<Job> jobs = new ArrayList<>();

        for (Object object : objects) {
            jobs.add(parseJob(object));
        }

        return jobs;
    }

    private static Job parseJob(Object object) {
        Map<Object, Object> map = YamlHelper.asMap(object);

        Job job = new Job();
        job.setName(YamlHelper.asString(map.get("name")));
        job.setEnabled(YamlHelper.asBoolean(map.get("enabled"), true));
        job.setProperties(parseProperties(YamlHelper.asMap(map.get("with"))));
        job.setSteps(parseSteps(YamlHelper.asList(map.get("steps"))));

        return job;
    }

    private static List<Step> parseSteps(List<Object> objects) {
        List<Step> steps = new ArrayList<>();

        if (objects != null) {
            for (Object object : objects) {
                steps.add(parseStep(object));
            }
        }

        return steps;
    }

    private static Step parseStep(Object object) {
        Map<Object, Object> map = YamlHelper.asMap(object);

        Step step = new Step();
        step.setName(YamlHelper.asString(map.get("name")));
        step.setEnabled(YamlHelper.asBoolean(map.get("enabled"), true));
        step.setProperties(parseProperties(YamlHelper.asMap(map.get("with"))));
        step.setWorkingDirectory(YamlHelper.asString(map.get("working-directory")));
        step.setRun(YamlHelper.asString(map.get("run")));

        return step;
    }

    private static Map<String, String> parseProperties(Map<Object, Object> map) {
        Map<String, String> properties = new LinkedHashMap<>();

        if (map != null) {
            map.forEach(new BiConsumer<Object, Object>() {
                @Override
                public void accept(Object key, Object value) {
                    properties.put(key.toString(), value.toString());
                }
            });
        }

        return properties;
    }
}
