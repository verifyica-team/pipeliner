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

package org.verifyica.pipeline.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Pipeline {

    private String id;
    private List<Property> property;
    private List<Job> job;

    public Pipeline() {
        initialize();
    }

    private void initialize() {
        id = UUID.randomUUID().toString();
        property = new ArrayList<>();
        job = new ArrayList<>();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setProperty(List<Property> property) {
        this.property = new ArrayList<>(new LinkedHashSet<>(property));
    }

    public List<Property> getProperty() {
        if (property == null) {
            return new ArrayList<>();
        } else {
            return property.stream()
                    .filter(property -> {
                        String name = property.getName();
                        return name != null && !name.trim().isEmpty();
                    })
                    .collect(Collectors.toList());
        }
    }

    public void setJob(List<Job> job) {
        this.job = job;
    }

    public List<Job> getJob() {
        return job;
    }

    @Override
    public String toString() {
        return "Pipeline{" + "id='" + id + '\'' + '}';
    }
}
