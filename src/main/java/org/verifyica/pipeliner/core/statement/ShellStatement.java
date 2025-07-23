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
 * A statement to run a shell command.
 */
public final class ShellStatement implements Statement {

    private final String shell;
    private final List<Expression> expressions;

    /**
     * Constructor
     *
     * @param shell the shell to use (e.g., "bash", "sh")
     * @param expressions the list of expressions that evaluate to the command to run
     */
    public ShellStatement(String shell, List<Expression> expressions) {
        this.shell = shell;
        this.expressions = expressions;
    }

    @Override
    public void execute(Context context) {
        Map<String, String> environmentVariables = context.resolveEnvironmentVariables();
        Path workingDirectory = context.resolveWorkingDirectory();
        int exitCode = 0;

        try {
            for (Expression expression : expressions) {
                List<String> arguments = new ArrayList<>();

                arguments.add(0, shell);
                arguments.add(1, "-c");
                String command = expression.evaluate(context).asString();
                arguments.add(command);

                context.println("# shell::%s %s", shell, command);

                exitCode = ProcessExecutor.execute(
                        environmentVariables, workingDirectory, arguments, string -> context.println("> " + string));

                if (exitCode != 0) {
                    break;
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage());
        }

        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code " + exitCode);
        }
    }

    @Override
    public String toString() {
        return "ShellInstruction{ shell='" + shell + "', expressions=" + expressions + "}";
    }

    /**
     * Parses a RunInstruction from the given statementParser.
     *
     * @param statementParser the statement parser to read from
     * @return a RunInstruction instance
     */
    public static Statement parse(StatementParser statementParser) {
        Line line = statementParser.nextLine();

        line.expect(Token.Type.LITERAL, "shell");

        List<Token> qualifierTokens = KeywordQualifierParser.parse(line);
        if (qualifierTokens.isEmpty()) {
            throw new RuntimeException("Expected shell qualifier after 'shell'");
        }
        String shell = qualifierTokens.get(0).lexeme;

        line.expectWhitespace();

        List<Expression> expressions = new ArrayList<>();

        Token nextToken = line.peek();
        if (nextToken != null && nextToken.lexeme.equals("|")) {
            line.next();
            line.expectEmpty();

            expressions = ExpressionParser.parseExpressionList(statementParser, "|");
        } else {
            String command = line.asString();
            Expression expression = new LiteralExpression(command);
            expressions.add(expression);
        }

        return new ShellStatement(shell, expressions);
    }
}
