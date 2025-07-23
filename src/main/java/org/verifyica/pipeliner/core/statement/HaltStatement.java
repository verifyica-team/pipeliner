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
import org.verifyica.pipeliner.core.exception.HaltException;
import org.verifyica.pipeliner.core.exception.SyntaxException;
import org.verifyica.pipeliner.core.expression.Expression;
import org.verifyica.pipeliner.core.expression.ExpressionParser;

/**
 * A statement that pauses execution for a given number of milliseconds.
 */
public final class HaltStatement implements Statement {

    private final String qualifier;
    private final int exitCode;
    private final Expression expression;

    /**
     * Constructor
     *
     * @param qualifier the qualifier for the halt statement, e.g., "ok" or "error"
     * @param exitCode the exit code to use when halting execution
     * @param expression the expression that evaluates to a message to display on halt
     */
    public HaltStatement(String qualifier, int exitCode, Expression expression) {
        this.qualifier = qualifier;
        this.exitCode = exitCode;
        this.expression = expression;
    }

    @Override
    public void execute(Context context) {
        String message = expression.evaluate(context).asString();
        if (message != null && !message.isEmpty()) {
            context.println("# halt::%s %d %s", qualifier, exitCode, message);
            throw new HaltException(qualifier, exitCode, message);
        } else {
            context.println("# halt::%s %d", qualifier, exitCode);
            throw new HaltException(qualifier, exitCode);
        }
    }

    @Override
    public String toString() {
        return "HaltInstruction{ status='" + qualifier + "', exitCode=" + exitCode + ", expression='" + expression
                + "'}";
    }

    /**
     * Parses a halt statement from the given statementParser.
     *
     * @param statementParser the statement parser to read from
     * @return a new HaltInstruction instance
     */
    public static Statement parse(StatementParser statementParser) {
        Line line = statementParser.nextLine();

        line.expect(Token.Type.LITERAL, "halt");

        List<Token> qualifierTokens = KeywordQualifierParser.parse(line);
        if (qualifierTokens.isEmpty()) {
            throw new SyntaxException("Expected qualifier for halt statement at " + line.location());
        }
        String qualifier = qualifierTokens.get(0).lexeme;

        if (!qualifier.equals("ok") && !qualifier.equals("error")) {
            throw new SyntaxException("Invalid qualifier '" + qualifier + "' for halt statement at "
                    + new Location(line.location().lineNumber, line.location().columnNumber - qualifier.length()));
        }

        line.expectWhitespace();

        Token codeToken = line.expect(Token.Type.LITERAL);

        int exitCode;
        try {
            exitCode = Integer.parseInt(codeToken.lexeme);
        } catch (NumberFormatException e) {
            throw new SyntaxException("Expected numeric exit code at " + codeToken.location);
        }

        Expression expression = ExpressionParser.parseOptionalExpression(line);

        return new HaltStatement(qualifier, exitCode, expression);
    }
}
