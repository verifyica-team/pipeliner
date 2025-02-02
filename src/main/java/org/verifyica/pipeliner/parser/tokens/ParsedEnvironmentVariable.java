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

import java.util.Objects;

/** Class to implement ParsedEnvironmentVariable */
public class ParsedEnvironmentVariable extends ParsedToken {

    private final String value;

    /**
     * Constructor
     *
     * @param position the position
     * @param text the text
     * @param value the value
     */
    private ParsedEnvironmentVariable(int position, String text, String value) {
        super(Type.ENVIRONMENT_VARIABLE, position, text);

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
        return "ParsedEnvironmentVariable { position=["
                + getPosition()
                + "] text=["
                + getText()
                + "] value=["
                + value
                + "] }";
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ParsedEnvironmentVariable)) return false;
        ParsedEnvironmentVariable that = (ParsedEnvironmentVariable) object;
        return super.equals(that) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
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
        private String value;

        /**
         * Constructor
         */
        public Builder() {
            // INTENTIONALLY BLANK
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
         * Method to build the ParsedEnvironmentVariable
         *
         * @return the ParsedEnvironmentVariable
         */
        public ParsedEnvironmentVariable build() {
            return new ParsedEnvironmentVariable(position, text, value);
        }
    }
}
