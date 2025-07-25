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

package org.verifyica.pipeliner.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.exception.SyntaxException;
import org.verifyica.pipeliner.util.ProcessExecutor;

/**
 * A statement to execute a command.
 */
public final class ExecStatement implements Statement {

    private static final LineMatcher LINE_MATCHER_1 =
            new LineMatcher().literal("exec").whitespace().literal("[").eol();

    private static final LineMatcher LINE_MATCHER_2 =
            new LineMatcher().literal("exec").whitespace().anyLiteral();

    private static final LineMatcher END_MATCHER =
            new LineMatcher().size(1).literal("]").eol();

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
                    environmentVariables, workingDirectory, arguments, line -> context.println("> " + line));
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
     * Parses an exec statement from the given line lexer.
     *
     * @param lineLexer the line lexer to read from
     * @return a new ExecStatement instance
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.next();

        // exec + <whitespace> + [
        if (LINE_MATCHER_1.isMatch(line)) {
            line.consume(); // exec
            line.consume(); // whitespace
            line.consume(); // [

            List<Expression> expressions = new ArrayList<>();

            while (true) {
                Line statementLine = lineLexer.peek();
                if (statementLine == null) {
                    throw new SyntaxException(
                            "Unexpected end of input while parsing shell statement at " + line.location());
                }

                if (END_MATCHER.isMatch(statementLine)) {
                    statementLine.consume();
                    break;
                }

                statementLine = lineLexer.next();
                expressions.add(ExpressionParser.parseExpression(statementLine));
            }

            return new ExecStatement(expressions);
        }

        // exec + <whitespace> + <any literal>
        if (LINE_MATCHER_2.isMatch(line)) {
            line.consume(); // exec
            line.consume(); // whitespace
            return new ExecStatement(List.of(ExpressionParser.parseExpression(line)));
        }

        // Invalid syntax
        throw new SyntaxException("Invalid println syntax at " + line.location());
    }
}
