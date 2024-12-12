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

/** Class to implement RootNode */
public class RootNode {

    private PipelineModel pipelineModel;

    /** Constructor */
    public RootNode() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to set the PipelineModel
     *
     * @param pipelineModel pipelineModel
     */
    public void setPipeline(PipelineModel pipelineModel) {
        this.pipelineModel = pipelineModel;
    }

    /**
     * Method to get the PipelineModel
     *
     * @return the PipelineModel
     */
    public PipelineModel getPipeline() {
        return pipelineModel;
    }
}
