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

package org.verifyica.pipeliner.execution;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.verifyica.pipeliner.model.PipelineModel;
import org.verifyica.pipeliner.model.PipelineModelFactory;

/** Class to implement PipelineFactory */
public class PipelineFactory {

    /** Constructor */
    public PipelineFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to create a Pipeline
     *
     * @param filename filename
     * @param environmentVariables environmentVariables
     * @param properties properties
     * @return a Pipeline
     * @throws IOException IOException
     */
    public Pipeline create(String filename, Map<String, String> environmentVariables, Map<String, String> properties)
            throws IOException {
        return create(new File(filename), environmentVariables, properties);
    }

    /**
     * Method to create a Pipeline
     *
     * @param file file
     * @param environmentVariables environmentVariables
     * @param properties properties
     * @return a Pipeline
     * @throws IOException IOException
     */
    public Pipeline create(File file, Map<String, String> environmentVariables, Map<String, String> properties)
            throws IOException {
        try (Reader reader = new FileReader(file)) {
            return create(reader, environmentVariables, properties);
        }
    }

    /**
     * Method to create a Pipeline
     *
     * @param reader reader
     * @param environmentVariables environmentVariables
     * @param properties properties
     * @return a Pipeline
     * @throws IOException IOException
     */
    public Pipeline create(Reader reader, Map<String, String> environmentVariables, Map<String, String> properties)
            throws IOException {
        PipelineModel pipelineModel = new PipelineModelFactory().create(reader);
        pipelineModel.getEnv().putAll(environmentVariables);
        pipelineModel.getWith().putAll(properties);

        List<Job> jobs = pipelineModel.getJobs().stream()
                .map(job -> {
                    List<Step> steps = job.getSteps().stream().map(Step::new).collect(Collectors.toList());
                    Job Job = new Job(job);
                    Job.setSteps(steps);
                    return Job;
                })
                .collect(Collectors.toList());

        Pipeline pipeline = new Pipeline(pipelineModel);
        pipeline.setJobs(jobs);

        return pipeline;
    }
}
