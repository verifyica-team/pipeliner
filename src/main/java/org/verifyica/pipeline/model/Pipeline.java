package org.verifyica.pipeline.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

public class Pipeline {

    private String id;
    private String name;
    private List<Property> property;
    private List<Job> job;

    public Pipeline() {
        initialize();
    }

    private void initialize() {
        id = UUID.randomUUID().toString();
        name = id;
        property = new ArrayList<>();
        job = new ArrayList<>();
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

    public List<Property> getProperty() {
        return property;
    }

    public void setProperty(List<Property> property) {
        this.property = new ArrayList<>(new LinkedHashSet<>(property));
    }

    public List<Job> getJob() {
        return job;
    }

    public void setJob(List<Job> job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return "Pipeline {" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

