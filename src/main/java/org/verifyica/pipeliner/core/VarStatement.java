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

package org.verifyica.pipeliner.core;

import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.exception.SyntaxException;
import org.verifyica.pipeliner.util.VariableName;

/**
 * A statement to set or remove an environment variable in the context.
 */
public class VarStatement implements Statement {

    private static final LineMatcher LINE_MATCHER_1 = new LineMatcher()
            .literal("var")
            .whitespace()
            .anyLiteral()
            .whitespace()
            .literal(":=")
            .whitespace()
            .anyLiteral();

    private static final LineMatcher LINE_MATCHER_2 = new LineMatcher()
            .literal("var")
            .whitespace()
            .anyLiteral()
            .whitespace()
            .literal(":=");

    private final String name;
    private final Expression expression;

    /**
     * Constructor
     *
     * @param name the name of the environment variable to set
     * @param expression the expression that evaluates to the value to set
     */
    public VarStatement(String name, Expression expression) {
        this.name = name;
        this.expression = expression;
    }

    @Override
    public void execute(Context context) {
        String value = expression.evaluate(context).asString();

        if (value == null) {
            context.currentScope().removeVariable(name);
        } else {
            context.currentScope().setVariable(name, value);
        }
    }

    @Override
    public String toString() {
        return "VarInstruction{" + "name='" + name + "', expression=" + expression + "}";
    }

    /**
     * Parses a var statement from the given line lexer.
     *
     * @param lineLexer the line lexer to read from
     * @return a new VarStatement instance
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.next();

        if (LINE_MATCHER_1.isMatch(line)) {
            line.consume(); // var
            line.consume(); // whitespace
            String name = line.consume().lexeme; // name
            if (VariableName.isInvalid(name)) {
                throw new SyntaxException("Invalid variable name '" + name + "' at "
                        + line.location().adjust(-name.length()));
            }
            line.consume(); // whitespace
            line.consume(); // :=
            line.consume(); // whitespace

            return new VarStatement(name, ExpressionParser.parseExpression(line));
        }

        if (LINE_MATCHER_2.isMatch(line)) {
            line.consume(); // var
            line.consume(); // whitespace
            String name = line.consume().lexeme; // name
            if (VariableName.isInvalid(name)) {
                throw new SyntaxException("Invalid variable name '" + name + "' at "
                        + line.location().adjust(-name.length()));
            }
            line.consume(); // whitespace
            line.consume(); // :=

            return new VarStatement(name, NullExpression.SINGLETON);
        }

        throw new SyntaxException("Expected var statement at " + line.location());
    }
}
