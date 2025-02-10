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

/** Class to implement ParsedToken */
public class ParsedToken {

    /**
     * Enum to implement type
     */
    public enum Type {

        /**
         * Text token
         */
        TEXT,

        /**
         * Variable token
         */
        VARIABLE,

        /**
         * Scoped variable token
         */
        SCOPED_VARIABLE,

        /**
         * Environment variable token
         */
        ENVIRONMENT_VARIABLE,
    }

    private final Type type;
    private final int position;
    private final String text;

    /**
     * Constructor
     *
     * @param type the type
     * @param position the position
     * @param text the text
     */
    protected ParsedToken(Type type, int position, String text) {
        this.type = type;
        this.text = text;
        this.position = position;
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
     * Method to get the token position
     *
     * @return the token position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Method to get the token text
     *
     * @return the token text
     */
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "ParsedToken{" + "type=" + type + ", position=" + position + ", text='" + text + '\'' + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ParsedToken)) return false;
        ParsedToken parsedToken = (ParsedToken) object;
        return position == parsedToken.position && type == parsedToken.type && Objects.equals(text, parsedToken.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, text, position);
    }

    /**
     * Method to cast the token to a specific token class
     *
     * @param tokenClass the token class
     * @return a token cast to the specified class
     * @param <T> the token type
     */
    @SuppressWarnings("unchecked")
    public <T extends ParsedToken> T cast(Class<T> tokenClass) {
        if (tokenClass.isInstance(this)) {
            return (T) this;
        } else {
            throw new ClassCastException(
                    "Cannot cast token [" + this.getClass().getName() + "] to [" + tokenClass.getName() + "]");
        }
    }
}
