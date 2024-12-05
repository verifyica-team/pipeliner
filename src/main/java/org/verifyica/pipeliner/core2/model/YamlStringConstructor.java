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

package org.verifyica.pipeliner.core2.model;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

/** Class to implement YamlStringConstructor */
public class YamlStringConstructor extends Constructor {

    /** Constructor */
    public YamlStringConstructor() {
        super(Object.class, new LoaderOptions());

        setPropertyUtils(new PropertyUtils() {

            @Override
            public Property getProperty(Class<? extends Object> type, String name) {
                if (name != null && name.indexOf('-') > -1) {
                    return super.getProperty(type, toCamelCase(name));
                } else {
                    return super.getProperty(type, name);
                }
            }
        });
    }

    /**
     * Method to convert a String to camel case
     *
     * @param string string
     * @return the String in camel case
     */
    private static String toCamelCase(String string) {
        String[] parts = string.split("-");

        StringBuilder stringBuilder = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            stringBuilder
                    .append(parts[i].substring(0, 1).toUpperCase())
                    .append(parts[i].substring(1).toLowerCase());
        }

        return stringBuilder.toString();
    }
}
