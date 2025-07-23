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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.expression.Expression;
import org.verifyica.pipeliner.core.expression.ExpressionParser;
import org.verifyica.pipeliner.core.expression.LiteralExpression;
import org.verifyica.pipeliner.core.util.ProcessExecutor;

/**
 * A statement to execute a command.
 */
public final class ExecStatement implements Statement {

    private final List<Expression> expressions;

    /**
     * Constructor
     *
     * @param expressions the list of expressions that evaluate to the command to run
     */
    public ExecStatement(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public void execute(Context context) {
        List<String> arguments = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();

        for (Expression expression : expressions) {
            if (expression instanceof LiteralExpression) {
                String argument = expression.evaluate(context).asString();
                arguments.add(argument);
                stringBuilder.append(argument).append(" ");
            }
        }

        String command = stringBuilder.toString().trim();

        context.println("# exec " + command);

        Map<String, String> environmentVariables = context.resolveEnvironmentVariables();
        Path workingDirectory = context.resolveWorkingDirectory();
        int exitCode;

        try {
            exitCode = ProcessExecutor.execute(
                    environmentVariables, workingDirectory, arguments, string -> context.println("> " + string));
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage());
        }

        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code " + exitCode);
        }
    }

    @Override
    public String toString() {
        return "ExecInstruction{ expressions=" + expressions + "}";
    }

    /**
     * Parses a RunInstruction from the given statementParser.
     *
     * @param statementParser the statement parser to read from
     * @return a RunInstruction instance
     */
    public static Statement parse(StatementParser statementParser) {
        Line line = statementParser.nextLine();

        line.expect(Token.Type.LITERAL, "exec");
        line.expectWhitespace();
        line.expect(Token.Type.LITERAL, "[");
        line.expectEmpty();

        List<Expression> expressions = ExpressionParser.parseExpressionList(statementParser, "]");

        return new ExecStatement(expressions);
    }
}
