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

import java.util.Set;
import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.core.Statement;
import org.verifyica.pipeliner.core.parser.ExpressionParser;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.LineLexer;
import org.verifyica.pipeliner.core.parser.LineMatcher;
import org.verifyica.pipeliner.core.statements.expression.NullExpression;
import org.verifyica.pipeliner.exception.SyntaxException;
import org.verifyica.pipeliner.util.EnvironmentVariableName;
import org.verifyica.pipeliner.util.VariableName;

/**
 * A statement to set or remove an environment variable or variable in the context.
 */
public class VariableAssignmentStatement implements Statement {

    private static final Set<String> QUALIFIERS = Set.of("environment-variable", "env", "variable", "var");

    private static final LineMatcher LINE_MATCHER_1 = new LineMatcher()
            .literalInSet(QUALIFIERS)
            .literal("::")
            .anyLiteral()
            .whitespace()
            .literal(":=")
            .whitespace()
            .anyLiteral();

    private static final LineMatcher LINE_MATCHER_2 = new LineMatcher()
            .literalInSet(QUALIFIERS)
            .literal("::")
            .anyLiteral()
            .whitespace()
            .literal(":=")
            .eol();

    private final String qualifier;
    private final String name;
    private final Expression expression;

    /**
     * Constructor
     *
     * @param qualifier the qualifier for the variable (e.g., "env" or "var")
     * @param name the name of the environment variable to set
     * @param expression the expression that evaluates to the value to set
     */
    public VariableAssignmentStatement(String qualifier, String name, Expression expression) {
        this.qualifier = qualifier;
        this.name = name;
        this.expression = expression;
    }

    @Override
    public void execute(Context context) {
        String value = expression.evaluate(context).asString();

        if (qualifier.equals("env") || qualifier.equals("environment-variable")) {
            if (value == null) {
                context.currentScope().removeEnvironmentVariable(name);
            } else {
                context.currentScope().setEnvironmentVariable(name, value);
            }
        } else {
            if (value == null) {
                context.currentScope().removeVariable(name);
            } else {
                context.currentScope().setVariable(name, value);
            }
        }
    }

    @Override
    public String toString() {
        return "VariableAssignmentStatement{qualifier='" + qualifier + "', name='" + name + "', expression="
                + expression + "}";
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
            String qualifier = line.consume().lexeme; // qualifier
            line.consume(); // ::
            String name = line.consume().lexeme; // name
            line.consume(); // whitespace
            line.consume(); // :=
            line.consume(); // whitespace

            if (qualifier.equals("env")) {
                if (EnvironmentVariableName.isInvalid(name)) {
                    throw new SyntaxException("Invalid environment variable name '" + name + "' at "
                            + line.location().adjust(-name.length()));
                }
            } else if (VariableName.isInvalid(name)) {
                throw new SyntaxException("Invalid variable name '" + name + "' at "
                        + line.location().adjust(-name.length()));
            }

            return new VariableAssignmentStatement(qualifier, name, ExpressionParser.parseExpression(line));
        }

        if (LINE_MATCHER_2.isMatch(line)) {
            String qualifier = line.consume().lexeme; // qualifier
            line.consume(); // ::
            String name = line.consume().lexeme; // name
            line.consume(); // whitespace
            line.consume(); // :=

            if (qualifier.equals("env")) {
                if (EnvironmentVariableName.isInvalid(name)) {
                    throw new SyntaxException("Invalid environment variable name '" + name + "' at "
                            + line.location().adjust(-name.length()));
                }
            } else if (VariableName.isInvalid(name)) {
                throw new SyntaxException("Invalid variable name '" + name + "' at "
                        + line.location().adjust(-name.length()));
            }

            return new VariableAssignmentStatement(qualifier, name, NullExpression.SINGLETON);
        }

        throw new SyntaxException("Invalid variable assignment statement at " + line.location());
    }
}
