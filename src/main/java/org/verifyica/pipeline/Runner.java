package org.verifyica.pipeline;

import org.verifyica.pipeline.model.Job;
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

    private void run(String workflowYaml) throws Throwable {
        org.verifyica.pipeline.model.Pipeline workflow = null;
        int workflowExitCode = 0;

        info("** Pipeline **");
        info("YAML workflow [%s]", workflowYaml);

        try {
            workflow = PipelineFactory.load(workflowYaml);
        } catch (Throwable e) {
            error(" YAML workflow format error [%s]", workflowYaml);
            e.printStackTrace(System.err);
            error("** Pipeline [1] **");
            System.exit(1);
        }

        info("Workflow {\"%s\"}", workflow.getName());

        for (Property property : workflow.getProperty()) {
            System.out.printf("[%s] = [%s]%n", property.getName(), property.getValue());
            System.setProperty(property.getName(), property.getValue());
        }

        for (Job job : workflow.getJob()) {
            if (job.getEnabled()) {
                int jobExitCode = 0;
                info("-> Job {\"%s\"}", job.getName());

                for (Step step : job.getStep()) {
                    if (step.getEnabled()) {
                        info("--> Step {\"%s\"}", step.getName());
                        step.execute(System.out, System.err);
                        info("<-- Step {\"%s\"} [%d]", step.getName(), step.getExitCode());
                        if (step.getExitCode() != 0) {
                            jobExitCode = step.getExitCode();
                            workflowExitCode = jobExitCode;
                            break;
                        }
                    }
                }

                info("<- Job {\"%s\"} [%d]", job.getName(), jobExitCode);
            }
        }

        info("Workflow {\"%s\"} [%d]", workflow.getName(), workflowExitCode);
        info("** Pipeline [%s] **", workflowExitCode);

        System.exit(workflowExitCode);
    }

    private void info(String format, Object... objects) {
        System.out.printf(format + "%n", objects);
    }

    private void error(String format, Object... objects) {
        System.err.printf(format + "%n", objects);
    }
}

