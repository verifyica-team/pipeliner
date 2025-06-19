/*
 * Copyright (C) Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.model.support;

import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

/** Class to implement YamlConstructor */
public class YamlConstructor extends Constructor {

    /**
     * Constructor
     */
    public YamlConstructor() {
        super(Object.class, new LoaderOptions());

        // Set the property utils
        setPropertyUtils(new PropertyMappingUtils());
    }

    /** Class to implement PropertyMappingUtils */
    private static class PropertyMappingUtils extends PropertyUtils {

        private final Map<String, String> yamlToJavaPropertyMappings;

        /**
         * Constructor
         */
        public PropertyMappingUtils() {
            super();

            yamlToJavaPropertyMappings = new HashMap<>();

            // Map YAML property names to Java property names
            yamlToJavaPropertyMappings.put("if", "conditional");
            yamlToJavaPropertyMappings.put("working-directory", "workingDirectory");
            yamlToJavaPropertyMappings.put("timeout-minutes", "timeoutMinutes");
            yamlToJavaPropertyMappings.put("with", "variables");
            yamlToJavaPropertyMappings.put("env", "environmentVariables");
        }

        @Override
        public Property getProperty(Class<?> type, String name) {
            return super.getProperty(type, yamlToJavaPropertyMappings.getOrDefault(name, name));
        }
    }
}
