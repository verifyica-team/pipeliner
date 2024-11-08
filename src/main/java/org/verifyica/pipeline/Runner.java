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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.verifyica.pipeline.common.Stopwatch;
import org.verifyica.pipeline.common.Timestamp;
import org.verifyica.pipeline.model.Job;
import org.verifyica.pipeline.model.Pipeline;
import org.verifyica.pipeline.model.PipelineFactory;
import org.verifyica.pipeline.model.Property;
import org.verifyica.pipeline.model.Step;

/** Class to implement Runner */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Runner {

    private static final String PROPERTIES_RESOURCE = "/pipeline.properties";

    private static final String KEY_VERSION = "version";

    private static final String VALUE_UNKNOWN = "unknown";

    /**
     * Main method
     *
     * @param args args
     * @throws Throwable Throwable
     */
    public static void main(String[] args) throws Throwable {
        if (args == null) {
            System.exit(1);
        }

        if (args.length == 0) {
            System.exit(2);
        }

        for (String argument : args) {
            if (!argument.trim().isEmpty()) {
                int exitCode = run(argument);
                if (exitCode != 0) {
                    System.exit(exitCode);
                    break;
                }
            }
        }

        System.exit(0);
    }

    /** Constructor */
    private Runner() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to run a pipeline YAML file
     *
     * @param pipelineYamlFilename pipelineYamlFilename
     * @return the exit code
     * @throws Throwable Throwable
     */
    public static int run(String pipelineYamlFilename) throws Throwable {
        Stopwatch runnerStopwatch = new Stopwatch();
        Stopwatch jobStopwatch = new Stopwatch();
        Stopwatch stepStopwatch = new Stopwatch();

        Pipeline pipeline = null;
        int pipelineExitCode = 0;

        info("Info Pipeline " + version());

        try {
            pipeline = PipelineFactory.createPipeline(pipelineYamlFilename);
        } catch (Throwable e) {
            error("YAML [%s] format error", pipelineYamlFilename);
            e.printStackTrace(System.err);
            System.exit(1);
        }

        info("Pipeline {\"%s\"}", pipeline.getId());

        for (Property property : pipeline.getProperty()) {
            System.out.printf("[%s] = [%s]%n", property.getName(), property.getValue());
            System.setProperty(property.getName(), property.getValue());
        }

        for (Job job : pipeline.getJob()) {
            if (job.getEnabled()) {
                jobStopwatch.reset();
                int jobExitCode = 0;
                info("Job {\"%s\"}", job.getId());

                for (Step step : job.getStep()) {
                    if (step.getEnabled()) {
                        info("Step {\"%s\"}", step.getId());
                        stepStopwatch.reset();
                        step.run(pipeline, job, System.out, System.err);
                        info(
                                "Step {\"%s\"} %d ms (%d)",
                                step.getId(), stepStopwatch.elapsedTime().toMillis(), step.getExitCode());
                        if (step.getExitCode() != 0) {
                            jobExitCode = step.getExitCode();
                            pipelineExitCode = jobExitCode;
                            break;
                        }
                    }
                }

                info(
                        "Job {\"%s\"} %d ms (%d)",
                        job.getId(), jobStopwatch.elapsedTime().toMillis(), jobExitCode);
            }
        }

        info("Pipeline %d ms (%d)", runnerStopwatch.elapsedTime().toMillis(), pipelineExitCode);

        return pipelineExitCode;
    }

    /**
     * Method to return the version
     *
     * @return the version
     */
    private static String version() {
        String value = VALUE_UNKNOWN;

        try (InputStream inputStream = Runner.class.getResourceAsStream(PROPERTIES_RESOURCE)) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                value = properties.getProperty(KEY_VERSION).trim();
            }
        } catch (IOException e) {
            // INTENTIONALLY BLANK
        }

        return value;
    }

    /**
     * Method to log info output
     *
     * @param format format
     * @param objects objects
     */
    private static void info(String format, Object... objects) {
        System.out.printf(Timestamp.now() + " @ " + format + "%n", objects);
    }

    /**
     * Method to log error output
     *
     * @param format format
     * @param objects objects
     */
    private static void error(String format, Object... objects) {
        System.err.printf(Timestamp.now() + " @ " + format + "%n", objects);
    }
}
