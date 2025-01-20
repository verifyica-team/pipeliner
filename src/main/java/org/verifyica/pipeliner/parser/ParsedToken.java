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

package org.verifyica.pipeliner.parser;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/** Class to implement ParsedToken */
public class ParsedToken {

    /**
     * Enum to implement token type
     */
    public enum Type {

        /**
         * Backslash token
         */
        BACKSLASH,
        /**
         * Text token
         */
        TEXT,
        /**
         * Variable token
         */
        VARIABLE,
        /**
         * Environment variable token
         */
        ENVIRONMENT_VARIABLE,
    }

    private final Type type;
    private final int position;
    private final String text;
    private final String value;
    private final int length;
    private final Set<Modifier> modifiers;

    /**
     * Constructor
     *
     * @param type the type
     * @param position the position
     * @param text the text
     * @param value the value
     */
    public ParsedToken(Type type, int position, String text, String value) {
        this(type, position, text, value, null);
    }

    /**
     * Constructor
     *
     * @param type the type
     * @param position the position
     * @param text the text
     * @param value the value
     * @param modifiers the modifiers
     */
    public ParsedToken(Type type, int position, String text, String value, Set<Modifier> modifiers) {
        this.type = type;
        this.position = position;
        this.text = text;
        this.value = type == Type.VARIABLE ? value.toUpperCase(Locale.ROOT) : value;
        this.length = text.length();
        this.modifiers = modifiers != null
                ? Collections.unmodifiableSet(new TreeSet<>(modifiers))
                : Collections.unmodifiableSet(new TreeSet<>());
    }

    /**
     * Constructor (for testing)
     *
     * @param type the type
     * @param text the text
     * @param value the value
     */
    public ParsedToken(Type type, String text, String value) {
        this.type = type;
        this.position = -1;
        this.text = text;
        this.value = type == Type.VARIABLE ? value.toUpperCase(Locale.ROOT) : value;
        this.length = text.length();
        this.modifiers = Collections.unmodifiableSet(new LinkedHashSet<>());
    }

    /**
     * Method to get the type
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Method to get the position
     *
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Method to get the text
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Method to get the value
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Method to get the text length
     *
     * @return the text length
     */
    public int getLength() {
        return length;
    }

    /**
     * Method to get the set of modifiers
     *
     * @return the set of modifiers
     */
    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    /**
     * Method to return if a modifier is present
     *
     * @param modifier the modifier
     * @return true if the modifier is present, else false
     */
    public boolean hasModifier(Modifier modifier) {
        return modifiers.contains(modifier);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ParsedToken { type=[")
                .append(type)
                .append("] position=[")
                .append(position)
                .append("] text=[")
                .append(text)
                .append("] value=[")
                .append(value)
                .append("] modifiers=[");

        // Join modifiers with a space separator
        if (!modifiers.isEmpty()) {
            sb.append(
                    String.join(" ", modifiers.stream().map(Modifier::toString).toArray(String[]::new)));
        }

        sb.append("] }");
        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ParsedToken)) return false;
        ParsedToken parsedToken = (ParsedToken) object;
        return type == parsedToken.type
                && Objects.equals(text, parsedToken.text)
                && position == parsedToken.position
                && Objects.equals(value, parsedToken.value)
                && Objects.equals(modifiers, parsedToken.modifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, text, value, modifiers);
    }
}
