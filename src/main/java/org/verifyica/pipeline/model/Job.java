package org.verifyica.pipeline.model;

import java.util.List;
import java.util.UUID;

public class Job {

    private String id;
    private String name;
    private boolean enabled;
    private List<Step> step;
    private int exitCode;

    public Job() {
        id = UUID.randomUUID().toString();
        name = id;
        enabled = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Step> getStep() {
        return step;
    }

    public void setStep(List<Step> step) {
        this.step = step;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return "Job {" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

