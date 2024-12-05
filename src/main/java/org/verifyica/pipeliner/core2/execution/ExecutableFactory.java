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

package org.verifyica.pipeliner.core2.execution;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;
import org.verifyica.pipeliner.core2.model.ModelParser;
import org.verifyica.pipeliner.core2.model.Pipeline;

public class ExecutableFactory {

    private final ModelParser modelParser;

    public ExecutableFactory() {
        modelParser = new ModelParser();
    }

    public ExecutablePipeline create(String filename) throws IOException {
        return create(new File(filename));
    }

    public ExecutablePipeline create(File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            return create(reader);
        }
    }

    public ExecutablePipeline create(Reader reader) throws IOException {
        Pipeline pipeline = modelParser.parse(reader);

        List<ExecutableJob> executableJobs = pipeline.getJobs().stream()
                .map(job -> {
                    List<ExecutableStep> executableSteps =
                            job.getSteps().stream().map(ExecutableStep::new).collect(Collectors.toList());
                    ExecutableJob executableJob = new ExecutableJob(job);
                    executableJob.setExecutableSteps(executableSteps);
                    return executableJob;
                })
                .collect(Collectors.toList());

        ExecutablePipeline executablePipeline = new ExecutablePipeline(pipeline);
        executablePipeline.setExecutableJobs(executableJobs);

        return executablePipeline;
    }
}
