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

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.core.Id;
import org.verifyica.pipeliner.core.Variable;
import org.verifyica.pipeliner.parser.SyntaxException;

/** Class to implement VariableToken */
public class VariableToken extends Token {

    private static final String SCOPE_SEPARATOR = Constants.SCOPE_SEPARATOR;

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
    private VariableToken(int position, String text, String scope, String value) {
        super(Type.VARIABLE, position, text);

        this.scope = scope;
        this.value = value;
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

    @Override
    public String toString() {
        return "VariableToken{" + "position="
                + getPosition() + ", text='"
                + getText() + '\'' + ", scope='"
                + scope + '\'' + ", value='"
                + value + '\'' + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof VariableToken)) return false;
        if (!super.equals(object)) return false;
        VariableToken that = (VariableToken) object;
        return super.equals(that) && Objects.equals(scope, that.scope) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), scope, value);
    }

    /**
     * Method to create a new variable token
     *
     * @param text the text
     * @param value the value
     * @return a new variable token
     * @throws SyntaxException If the variable is invalid
     */
    public static VariableToken create(String text, String value) throws SyntaxException {
        return create(-1, text, value);
    }

    /**
     * Method to create a new variable token
     *
     * @param position the position
     * @param text the text
     * @param value the value
     * @return a new variable token
     * @throws SyntaxException If the variable is invalid
     */
    public static VariableToken create(int position, String text, String value) throws SyntaxException {
        if (value.startsWith(SCOPE_SEPARATOR) || value.endsWith(SCOPE_SEPARATOR)) {
            throw new SyntaxException("invalid variable [" + text + "]");
        }

        // Split the value by '/'
        String[] parts = value.split(Pattern.quote(SCOPE_SEPARATOR));

        if (parts.length > 1) {
            // Check each part
            for (int i = 0; i < parts.length - 1; i++) {
                if (Id.isInvalid(parts[i])) {
                    throw new SyntaxException("invalid variable [" + text + "]");
                }
            }
        }

        if (Variable.isInvalid(parts[parts.length - 1])) {
            throw new SyntaxException("invalid variable [" + text + "]");
        }

        String scope = null;
        String unscopedValue;

        if (parts.length > 1) {
            // Build the scope
            scope = String.join(SCOPE_SEPARATOR, Arrays.copyOf(parts, parts.length - 1));

            // The unscoped value is the last part
            unscopedValue = parts[parts.length - 1];
        } else {
            // No scope, so the unscoped value is the value
            unscopedValue = value;
        }

        return new VariableToken(position, text, scope, unscopedValue);
    }
}
