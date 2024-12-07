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

package org.verifyica.pipeliner.model;

import java.util.List;

public class Job extends Base {

    private List<Step> steps;

    public Job() {
        super();
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public List<Step> getSteps() {
        return steps;
    }

    @Override
    public void validate() {
        validateName(this);
        validateId(this);
        validateEnv(this);
        validateWith(this);
        validateOpt(this);

        getSteps().forEach(Step::validate);
    }

    @Override
    public String toString() {
        return "@job " + super.toString();
    }
}
