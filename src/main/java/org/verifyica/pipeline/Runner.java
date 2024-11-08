package org.verifyica.pipeline;

import org.verifyica.pipeline.common.Stopwatch;
import org.verifyica.pipeline.model.Job;
import org.verifyica.pipeline.model.Pipeline;
import org.verifyica.pipeline.model.Property;
import org.verifyica.pipeline.model.Step;
import org.verifyica.pipeline.model.PipelineFactory;

public class Runner {

    public static void main(String[] args) throws Throwable {
        new Runner().run(args[0]);
    }

    public Runner() {
        // INTENTIONALLY BLANK
    }

    private void run(String pipelineYamlFilename) throws Throwable {
        Stopwatch runnerStopwatch = new Stopwatch();
        Stopwatch jobStopwatch = new Stopwatch();
        Stopwatch stepStopwatch = new Stopwatch();

        Pipeline pipeline = null;
        int pipelineExitCode = 0;

        info("Info Pipeline v0.0.1");

        try {
            pipeline = PipelineFactory.load(pipelineYamlFilename);
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
                        step.execute(System.out, System.err);
                        info("Step {\"%s\"} %d ms (%d)", step.getId(), stepStopwatch.elapsedTime().toMillis(), step.getExitCode());
                        if (step.getExitCode() != 0) {
                            jobExitCode = step.getExitCode();
                            pipelineExitCode = jobExitCode;
                            break;
                        }
                    }
                }

                info("Job {\"%s\"} %d ms (%d)", job.getId(), jobStopwatch.elapsedTime().toMillis(), jobExitCode);
            }
        }

        info("Pipeline %d ms (%d)", runnerStopwatch.elapsedTime().toMillis(), pipelineExitCode);

        System.exit(pipelineExitCode);
    }

    private void info(String format, Object... objects) {
        System.out.printf("@ " + format + "%n", objects);
    }

    private void error(String format, Object... objects) {
        System.err.printf("@ " + format + "%n", objects);
    }
}

