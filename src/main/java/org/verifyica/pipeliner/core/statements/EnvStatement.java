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

package org.verifyica.pipeliner.core.statements;

import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.core.Statement;
import org.verifyica.pipeliner.core.parser.ExpressionParser;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.LineLexer;
import org.verifyica.pipeliner.core.parser.LineMatcher;
import org.verifyica.pipeliner.core.statements.expression.NullExpression;
import org.verifyica.pipeliner.exception.SyntaxException;
import org.verifyica.pipeliner.util.EnvironmentVariableName;

/**
 * A statement to set or remove an environment variable in the context.
 */
public class EnvStatement implements Statement {

    private static final LineMatcher LINE_MATCHER_1 = new LineMatcher()
            .literal("env")
            .whitespace()
            .anyLiteral()
            .whitespace()
            .literal(":=")
            .whitespace()
            .anyLiteral();

    private static final LineMatcher LINE_MATCHER_2 = new LineMatcher()
            .literal("env")
            .whitespace()
            .anyLiteral()
            .whitespace()
            .literal(":=")
            .eol();

    private final String name;
    private final Expression expression;

    /**
     * Constructor
     *
     * @param name the name of the environment variable to set
     * @param expression the expression that evaluates to the value to set
     */
    public EnvStatement(String name, Expression expression) {
        this.name = name;
        this.expression = expression;
    }

    @Override
    public void execute(Context context) {
        String value = expression.evaluate(context).asString();

        if (value == null) {
            context.currentScope().removeEnvironmentVariable(name);
        } else {
            context.currentScope().setEnvironmentVariable(name, value);
        }
    }

    @Override
    public String toString() {
        return "EnvInstruction{" + "name='" + name + "', expression=" + expression + "}";
    }

    /**
     * Parses an env statement from the given line lexer.
     *
     * @param lineLexer the line lexer to read from
     * @return a new EnvStatement instance
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.next();

        if (LINE_MATCHER_1.isMatch(line)) {
            line.consume(); // env
            line.consume(); // whitespace
            String name = line.consume().lexeme; // name
            if (EnvironmentVariableName.isInvalid(name)) {
                throw new SyntaxException("Invalid environment variable name '" + name + "' at "
                        + line.location().adjust(-name.length()));
            }
            line.consume(); // whitespace
            line.consume(); // :=
            line.consume(); // whitespace

            return new EnvStatement(name, ExpressionParser.parseExpression(line));
        }

        if (LINE_MATCHER_2.isMatch(line)) {
            line.consume(); // env
            line.consume(); // whitespace
            String name = line.consume().lexeme; // name
            if (EnvironmentVariableName.isInvalid(name)) {
                throw new SyntaxException("Invalid environment variable name '" + name + "' at "
                        + line.location().adjust(-name.length()));
            }
            line.consume(); // whitespace
            line.consume(); // :=

            return new EnvStatement(name, NullExpression.SINGLETON);
        }

        throw new SyntaxException("Expected env statement at " + line.location());
    }
}
