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
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.Parser;
import org.verifyica.pipeliner.core.parser.Token;
import org.verifyica.pipeliner.core.parser.VariableName;

/**
 * A statement to set or remove a variable in the context.
 */
public class VarStatement implements Statement {

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
            context.currentFrame().removeVariable(name);
        } else {
            context.currentFrame().setVariable(name, value);
        }
    }

    @Override
    public String toString() {
        return "EnvStatement{" + "name='" + name + "', expression=" + expression + "}";
    }

    /**
     * Parses a variable statement from the given parser.
     *
     * @param parser the parser to read from
     * @return the parsed VarStatement
     */
    public static Statement parse(Parser parser) {
        Line line = parser.nextSequence();

        line.expect(Token.Type.LITERAL, "var");
        line.expect(Token.Type.WHITESPACE);

        Token textToken = line.expect(Token.Type.LITERAL);
        String name = textToken.lexeme;

        if (VariableName.isInvalid(name)) {
            throw new SyntaxException("Invalid var name '" + name + "' at " + textToken.location);
        }

        line.expect(Token.Type.WHITESPACE);
        line.expect(Token.Type.LITERAL, ":=");

        List<Token> tokens = line.tokens();
        if (tokens.isEmpty()) {
            return new PropStatement(textToken.lexeme, NullExpression.SINGLETON);
        }

        line.expect(Token.Type.WHITESPACE);

        Expression expression = ExpressionParser.parseStringExpression(line);

        return new VarStatement(name, expression);
    }
}
