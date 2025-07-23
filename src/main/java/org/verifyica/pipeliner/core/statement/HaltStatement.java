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
import org.verifyica.pipeliner.core.exception.HaltException;
import org.verifyica.pipeliner.core.exception.SyntaxException;
import org.verifyica.pipeliner.core.expression.Expression;
import org.verifyica.pipeliner.core.expression.ExpressionParser;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.Parser;
import org.verifyica.pipeliner.core.parser.Token;

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
            throw new HaltException(exitCode, message);
        } else {
            throw new HaltException(exitCode);
        }
    }

    @Override
    public String toString() {
        return "HaltStatement{" + "exitCode=" + exitCode + ", expression='" + expression + "'}";
    }

    /**
     * Parses a halt statement from the given parser.
     *
     * @param parser the parser to read from
     * @return a new HaltStatement instance
     */
    public static Statement parse(Parser parser) {
        Line line = parser.nextSequence();

        Token keyword = line.expect(Token.Type.LITERAL, "halt::ok", "halt::error");
        int index = keyword.lexeme.indexOf("::");
        String qualifier = keyword.lexeme.substring(index + 2);

        line.expect(Token.Type.WHITESPACE);

        Token codeToken = line.expect(Token.Type.LITERAL);

        int exitCode;
        try {
            exitCode = Integer.parseInt(codeToken.lexeme);
        } catch (NumberFormatException e) {
            throw new SyntaxException("Expected numeric exit code at " + codeToken.location);
        }

        Expression expression = ExpressionParser.parseOptionalStringExpression(line);

        return new HaltStatement(qualifier, exitCode, expression);
    }
}
