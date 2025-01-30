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
import org.verifyica.pipeliner.core.EnvironmentVariable;
import org.verifyica.pipeliner.parser.SyntaxException;

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
        return "ParsedEnvironmentVariable{" + "position="
                + getPosition() + ", text='"
                + getText() + '\'' + "value='"
                + value + '\'' + '}';
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
     * Method to create a new environment variable token
     *
     * @param text the text
     * @param value the value
     * @return a new environment variable token
     * @throws SyntaxException If the environment variable is invalid
     */
    public static ParsedEnvironmentVariable create(String text, String value) throws SyntaxException {
        return create(-1, text, value);
    }

    /**
     * Method to create a new environment variable token
     *
     * @param position the position
     * @param text the text
     * @param value the value
     * @return a new environment variable token
     * @throws SyntaxException If the environment variable is invalid
     */
    public static ParsedEnvironmentVariable create(int position, String text, String value) throws SyntaxException {
        if (EnvironmentVariable.isInvalid(value)) {
            throw new SyntaxException("invalid environment variable [" + text + "]");
        }

        return new ParsedEnvironmentVariable(position, text, value);
    }
}
