/*
 * Copyright (C) Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.common;

import static java.lang.String.format;

import org.apache.commons.jexl3.*;
import org.apache.commons.jexl3.introspection.JexlSandbox;
import org.verifyica.pipeliner.parser.SyntaxException;

/** Class to implement ConditionalEvaluator */
public class ConditionalEvaluator {

    private static final JexlEngine JEXL;

    static {
        // Create a sandbox that disallows all method and property access
        JexlSandbox sandbox = new JexlSandbox(false);

        // Custom arithmetic to disable string concatenation
        JexlArithmetic arithmetic = new NoStringConcatArithmetic();

        // Build the JexlEngine with the sandbox and custom arithmetic
        JEXL = new JexlBuilder()
                .sandbox(sandbox)
                .arithmetic(arithmetic)
                .strict(true)
                .silent(false)
                .create();
    }

    /** Constructor */
    private ConditionalEvaluator() {
        // INTENTIONALLY BLANK
    }

    /**
     * Evaluates a boolean expression composed of string literals.
     *
     * @param expression the expression to evaluate (e.g., "\"foo\" == \"bar\"")
     * @return true if the expression evaluates to true; false otherwise
     * @throws SyntaxException if the expression is invalid
     */
    public boolean evaluate(String expression) throws SyntaxException {
        try {
            JexlExpression expr = JEXL.createExpression(expression);
            Object result = expr.evaluate(null);

            if (result instanceof Boolean) {
                return (Boolean) result;
            } else {
                throw new SyntaxException(format("Invalid expression [%s]", expression));
            }
        } catch (Exception e) {
            throw new SyntaxException(format("Invalid expression [%s]", expression));
        }
    }

    /**
     * Method to get the singleton instance of ConditionalEvaluator.
     *
     * @return the singleton instance
     */
    public static ConditionalEvaluator getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final ConditionalEvaluator SINGLETON = new ConditionalEvaluator();
    }

    /**
     * Custom JexlArithmetic that disables string concatenation.
     */
    private static class NoStringConcatArithmetic extends JexlArithmetic {
        public NoStringConcatArithmetic() {
            super(true); // strict mode
        }

        @Override
        public Object add(Object left, Object right) {
            if (left instanceof String || right instanceof String) {
                throw new UnsupportedOperationException("String concatenation is not allowed");
            }

            return super.add(left, right);
        }
    }
}
