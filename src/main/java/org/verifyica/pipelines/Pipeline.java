package org.verifyica.pipelines;

import org.verifyica.pipelines.model.Job;
import org.verifyica.pipelines.model.Step;
import org.verifyica.pipelines.model.Workflow;
import org.verifyica.pipelines.model.WorkflowFactory;

public class Pipeline {

    public static void main(String[] args) throws Throwable {
        new Pipeline().run(args[0]);
    }

    public Pipeline() {
        // INTENTIONALLY BLANK
    }

    private void run(String workflowYaml) throws Throwable {
        Workflow workflow = null;
        int workflowExitCode = 0;

        info("------------------");
        info("Verifyica Pipeline");
        info("------------------");
        info("Load YAML workflow [%s]", workflowYaml);

        try {
            workflow = WorkflowFactory.load(workflowYaml);
        } catch (Throwable e) {
            error("Invalid YAML workflow [%s]", workflowYaml);
            System.exit(1);
        }

        info("Workflow {\"%s\"}", workflow.getName());

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

                info("<- Job {\"%s\"} [%d]", job, jobExitCode);
            }
        }

        info("Workflow {\"%s\"} [%d]", workflow.getName(), workflowExitCode);
        System.exit(workflowExitCode);
    }

    private void info(String format, Object... objects) {
        System.out.printf(format + "%n", objects);
    }

    private void error(String format, Object... objects) {
        System.err.printf(format + "%n", objects);
    }
}

