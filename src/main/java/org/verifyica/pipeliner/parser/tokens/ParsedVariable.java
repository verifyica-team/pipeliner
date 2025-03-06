/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.parser.tokens;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.verifyica.pipeliner.parser.lexer.VariableLexer;

/** Class to implement ParsedVariable */
public class ParsedVariable extends ParsedToken {

    /**
     * Constant to implement scope separator
     */
    public static final String SCOPE_SEPARATOR = String.valueOf(VariableLexer.PERIOD);

    private final Set<Modifier> modifiers;
    private final String scope;
    private final String value;

    /**
     * Constructor
     *
     * @param position the position
     * @param text the text
     * @param scope the scope
     * @param value the value
     */
    private ParsedVariable(int position, String text, String scope, String value, Set<Modifier> modifiers) {
        super(Type.VARIABLE, position, text);

        this.scope = scope;
        this.value = value;
        this.modifiers = modifiers != null ? Collections.unmodifiableSet(modifiers) : Collections.emptySet();
    }

    /**
     * Method to check if the variable is scoped
     *
     * @return true if the variable is scoped, else false
     */
    public boolean isScoped() {
        return scope != null;
    }

    /**
     * Method to get the scope
     *
     * @return the scope
     */
    public String getScope() {
        return scope;
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
     * Method to get the scoped value
     *
     * @return the scoped value
     */
    public String getScopedValue() {
        return scope != null && !scope.isEmpty() ? scope + SCOPE_SEPARATOR + value : value;
    }

    /**
     * Method to return if a variable has a modifier
     *
     * @param modifier the modifier
     * @return true if the variable has the modifier, else false
     */
    public boolean hasModifier(Modifier modifier) {
        return modifiers.contains(modifier);
    }

    /**
     * Method to get the modifiers
     *
     * @return the modifiers
     */
    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    @Override
    public String toString() {
        return "ParsedVariable { position=["
                + getPosition()
                + "] text=["
                + getText()
                + "] scope=["
                + scope
                + "] value=["
                + value
                + "] scopedValue=["
                + getScopedValue()
                + "] modifiers=["
                + modifiers
                + "] }";
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ParsedVariable)) return false;
        if (!super.equals(object)) return false;
        ParsedVariable that = (ParsedVariable) object;
        return super.equals(that)
                && Objects.equals(scope, that.scope)
                && Objects.equals(value, that.value)
                && Objects.equals(modifiers, that.modifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), scope, value);
    }

    /**
     * Method to create a builder
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Class to implement Builder */
    public static class Builder {

        private int position = -1;
        private String text;
        private final Set<Modifier> modifiers;
        private String scope;
        private String value;

        /**
         * Constructor
         */
        public Builder() {
            modifiers = new HashSet<>();
        }

        /**
         * Method to set the position
         *
         * @param position the position
         * @return this
         */
        public Builder position(int position) {
            this.position = position;
            return this;
        }

        /**
         * Method to set the text
         *
         * @param text the text
         * @return this
         */
        public Builder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Method to add a modifier
         *
         * @param modifier the modifier
         * @return this
         */
        public Builder addModifier(Modifier modifier) {
            modifiers.add(modifier);
            return this;
        }

        /**
         * Method to add modifiers
         *
         * @param modifiers the modifiers
         * @return this
         */
        public Builder addModifiers(Set<Modifier> modifiers) {
            if (modifiers != null) {
                this.modifiers.addAll(modifiers);
            }
            return this;
        }

        /**
         * Method to add a scope
         *
         * @param scope the scope
         * @return this
         */
        public Builder addScope(String scope) {
            if (this.scope == null) {
                this.scope = scope;
            } else {
                this.scope = this.scope + SCOPE_SEPARATOR + scope;
            }
            return this;
        }

        /**
         * Method to set the value
         *
         * @param value the value
         * @return this
         */
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        /**
         * Method to build the ParsedVariable
         *
         * @return the ParsedVariable
         */
        public ParsedVariable build() {
            return new ParsedVariable(position, text, scope, value, modifiers);
        }
    }
}
