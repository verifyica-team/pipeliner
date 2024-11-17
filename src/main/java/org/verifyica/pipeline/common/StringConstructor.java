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

package org.verifyica.pipeline.common;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

/** Class to implement StringConstructor */
public class StringConstructor extends Constructor {

    /** Constructor */
    public StringConstructor() {
        super(Object.class, new LoaderOptions());
    }

    @Override
    protected Object constructObject(Node node) {
        if (node instanceof ScalarNode) {
            return ((ScalarNode) node).getValue();
        } else {
            return super.constructObject(node);
        }
    }
}
