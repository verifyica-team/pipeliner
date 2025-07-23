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

import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.expression.Expression;
import org.verifyica.pipeliner.core.expression.ExpressionParser;

/**
 * A statement that prints a line.
 */
public final class PrintStatement implements Statement {

    private final Expression expression;

    /**
     * Constructor
     *
     * @param expression the expression to evaluate and print
     */
    public PrintStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(Context context) {
        String message = expression.evaluate(context).asString();
        context.print(message);
    }

    @Override
    public String toString() {
        return "PrintInstruction{ expression=" + expression + '}';
    }

    /**
     * Parses a print statement from the given statementParser.
     *
     * @param statementParser the statement parser to read from
     * @return a new PrintInstruction instance
     */
    public static Statement parse(StatementParser statementParser) {
        Line line = statementParser.nextLine();

        line.expect(Token.Type.LITERAL, "print");
        line.expectWhitespace();

        Expression expression = ExpressionParser.parseOptionalExpression(line);

        return new PrintStatement(expression);
    }
}
