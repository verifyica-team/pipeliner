package org.verifyica.pipelines.model;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Property {

    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Property{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
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
