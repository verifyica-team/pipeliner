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

/** Class to implement ParsedText */
public class ParsedText extends ParsedToken {

    /**
     * Constructor
     *
     * @param position the position
     * @param text the text
     */
    private ParsedText(int position, String text) {
        super(Type.TEXT, position, text);
    }

    /**
     * Method to get the value
     *
     * @return the value
     */
    public String getValue() {
        return getText();
    }

    @Override
    public String toString() {
        return "ParsedText { position=[" + getPosition() + "] text=[" + getText() + "] }";
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
         * Method to build the ParsedText
         *
         * @return the ParsedText
         */
        public ParsedText build() {
            return new ParsedText(position, text);
        }
    }
}
