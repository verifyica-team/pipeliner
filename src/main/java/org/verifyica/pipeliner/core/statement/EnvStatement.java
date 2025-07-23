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

package org.verifyica.pipeliner.core.statement;

import java.util.List;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.exception.SyntaxException;
import org.verifyica.pipeliner.core.expression.Expression;
import org.verifyica.pipeliner.core.expression.ExpressionParser;
import org.verifyica.pipeliner.core.expression.NullExpression;
import org.verifyica.pipeliner.core.util.VariableName;

/**
 * A statement to set or remove an environment variable in the context.
 */
public class EnvStatement implements Statement {

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
     * Parses an env statement from the given statementParser.
     *
     * @param statementParser the statement parser to read from
     * @return a new EnvInstruction instance
     */
    public static Statement parse(StatementParser statementParser) {
        Line line = statementParser.nextLine();

        line.expect(Token.Type.LITERAL, "env");
        line.expectWhitespace();

        Token textToken = line.expect(Token.Type.LITERAL);
        String name = textToken.lexeme;

        if (VariableName.isInvalid(name)) {
            throw new SyntaxException("Invalid env name '" + name + "' at " + textToken.location);
        }

        line.expectWhitespace();
        line.expect(Token.Type.LITERAL, ":=");

        List<Token> tokens = line.tokens();
        if (tokens.isEmpty()) {
            return new EnvStatement(textToken.lexeme, NullExpression.SINGLETON);
        }

        line.expectWhitespace();

        Expression expression = ExpressionParser.parseExpression(line);

        return new EnvStatement(name, expression);
    }
}
