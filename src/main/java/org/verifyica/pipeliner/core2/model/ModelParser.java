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

package org.verifyica.pipeliner.core2.model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;

public class ModelParser {

    public ModelParser() {
        // INTENTIONALLY BLANK
    }

    public Pipeline parse(String filename) throws IOException {
        return parse(new File(filename));
    }

    public Pipeline parse(File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            return parse(reader);
        }
    }

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

    public static void main(String[] args) throws IOException {
        List<File> files = new ArrayList<>();

        /**/
        files.add(new File("/home/dhoard/Development/github/verifyica-team/pipeliner/package.yaml"));
        files.add(new File("/home/dhoard/Development/github/verifyica-team/pipeliner/release.yaml"));
        files.addAll(findYamlFiles(new File("/home/dhoard/Development/github/verifyica-team/pipeliner/tests")));
        files.addAll(findYamlFiles(new File("/home/dhoard/Development/github/verifyica-team/pipeliner/examples")));
        /**/
        // files.add(new File("/home/dhoard/Development/github/verifyica-team/pipeliner/tests/test-variables-4.yaml"));

        for (File file : files) {
            Pipeline pipelineModel = new ModelParser().parse(file);

            System.out.printf("pipeline > %s%n", pipelineModel);
            System.out.printf("  enabled > %s%n", pipelineModel.getEnabled());
            printProperties(" ", pipelineModel.getWith());

            pipelineModel.getJobs().forEach(job -> {
                System.out.printf("  job > %s%n", job);
                System.out.printf("    enabled > %s%n", job.getEnabled());
                printProperties("   ", job.getWith());
                job.getSteps().forEach(step -> {
                    System.out.printf("  step > %s%n", step);
                    System.out.printf("    enabled > %s%n", step.getEnabled());
                    printProperties("   ", step.getWith());
                });
            });
        }
    }

    private static void printProperties(String prefx, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.printf("%s property > [%s] = [%s]%n", prefx, entry.getKey(), entry.getValue());
        }
    }

    public static List<File> findYamlFiles(File directory) {
        List<File> yamlFiles = new ArrayList<>();

        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".yaml"));

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        yamlFiles.add(file);
                    }
                }
            }
        }

        Collections.sort(yamlFiles);

        return yamlFiles;
    }
}
