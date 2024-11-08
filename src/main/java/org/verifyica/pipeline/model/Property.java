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

import java.util.Objects;

/** Class to implement Property */
public class Property {

    private String name;
    private String value;

    /**
     * Method to set the name
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method to get the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Method to set the value
     *
     * @param value value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Method to get the value
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Property{" + "name='" + name + '\'' + ", value='" + value + '\'' + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Property property = (Property) object;
        return Objects.equals(name, property.name) && Objects.equals(value, property.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}
