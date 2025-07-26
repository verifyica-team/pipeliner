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
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.LineLexer;
import org.verifyica.pipeliner.core.parser.LiteralInSetParser;
import org.verifyica.pipeliner.core.parser.LiteralParser;
import org.verifyica.pipeliner.core.parser.MergeParser;
import org.verifyica.pipeliner.core.parser.OptionalParser;
import org.verifyica.pipeliner.core.parser.Token;
import org.verifyica.pipeliner.core.parser.ValueParser;
import org.verifyica.pipeliner.core.statements.expression.LiteralExpression;
import org.verifyica.pipeliner.exception.SyntaxException;
import org.verifyica.pipeliner.util.EnvironmentVariableName;
import org.verifyica.pipeliner.util.VariableName;

/**
 * A statement to set or remove an environment variable or variable in the context.
 */
public class VariableAssignmentStatement implements Statement {

    private static final Set<String> KEYWORDS = Set.of("environment-variable", "env", "variable", "var");

    private static final LiteralInSetParser KEYWORD_PARSER = LiteralInSetParser.of(KEYWORDS);

    private static final LiteralParser SCOPE_PARSER = LiteralParser.of("::");

    private static final ValueParser VALUE_PARSER = ValueParser.singleton();

    private static final OptionalParser OPTIONAL_WHITESPACE_PARSER = OptionalParser.of(Token.Type.WHITESPACE);

    private static final LiteralParser ASSIGNMENT_OPERATOR_PARSER = LiteralParser.of(":=");

    private static final MergeParser MERGE_PARSER = MergeParser.singleton();

    private final String keyword;
    private final String name;
    private final Expression expression;

    /**
     * Constructor
     *
     * @param keyword the keyword for the variable (e.g., "env" or "var")
     * @param name the name of the environment variable to set
     * @param expression the expression that evaluates to the value to set
     */
    public VariableAssignmentStatement(String keyword, String name, Expression expression) {
        this.keyword = keyword;
        this.name = name;
        this.expression = expression;
    }

    @Override
    public void execute(Context context) {
        String value = expression.evaluate(context).asString();

        if (keyword.equals("env") || keyword.equals("environment-variable")) {
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
        return "VariableAssignmentStatement{qualifier='" + keyword + "', name='" + name + "', expression=" + expression
                + "}";
    }

    /**
     * Parses a var statement from the given {@code LineLexer}.
     *
     * @param lineLexer the {@code LineLexer} to read from
     * @return a new VarStatement instance
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.consume();

        String keyword = KEYWORD_PARSER.parse(line); // "env", "environment-variable", "var", or "variable"

        SCOPE_PARSER.parse(line); // ::

        String name = VALUE_PARSER.parse(line); // variable name

        // Validate the keyword and name
        if (keyword.startsWith("env") && EnvironmentVariableName.isInvalid(name)) {
            throw new SyntaxException(
                    line.location().adjust(-name.length()) + ": Invalid environment variable name '" + name + "'");
        } else if (VariableName.isInvalid(name)) {
            throw new SyntaxException(
                    line.location().adjust(-name.length()) + ": Invalid variable name '" + name + "'");
        }

        OPTIONAL_WHITESPACE_PARSER.parse(line); // optional whitespace
        ASSIGNMENT_OPERATOR_PARSER.parse(line); // :=
        OPTIONAL_WHITESPACE_PARSER.parse(line); // optional whitespace
        String value = MERGE_PARSER.parse(line); // variable value
        Expression expression = new LiteralExpression(value);

        return new VariableAssignmentStatement(keyword, name, expression);
    }
}
