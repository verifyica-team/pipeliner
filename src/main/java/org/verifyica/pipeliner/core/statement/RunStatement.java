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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.expression.Expression;
import org.verifyica.pipeliner.core.expression.ExpressionParser;
import org.verifyica.pipeliner.core.expression.LiteralExpression;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.Parser;
import org.verifyica.pipeliner.core.parser.Token;

/**
 * A statement to run a command.
 */
public final class RunStatement implements Statement {

    private final String shell;
    private final List<Expression> expressions;
    private final boolean useShell;

    /**
     * Constructor
     *
     * @param shell the shell to use for executing the command (e.g., "bash", "sh"), or null for direct execution
     * @param expressions the list of expressions that evaluate to the command to run
     * @param useShell whether to use the shell or run directly
     */
    public RunStatement(String shell, List<Expression> expressions, boolean useShell) {
        this.shell = shell;
        this.expressions = expressions;
        this.useShell = useShell;
    }

    @Override
    public void execute(Context context) {
        List<String> arguments = new ArrayList<>();

        if (useShell) {
            arguments.add(shell);
            arguments.add("-c");

            // Join all command parts into a single string
            StringBuilder joined = new StringBuilder();
            for (Expression expression : expressions) {
                if (expression instanceof LiteralExpression) {
                    joined.append(expression.evaluate(context).asString()).append(" ");
                }
            }
            arguments.add(joined.toString().trim());
        } else {
            for (Expression expression : expressions) {
                if (expression instanceof LiteralExpression) {
                    arguments.add(expression.evaluate(context).asString());
                }
            }
        }

        String command = String.join(" ", arguments);
        context.println("@run %s", command);

        Path workingDirectory = context.resolveWorkingDirectory();
        int exitCode;

        try {
            exitCode = runCommand(context, workingDirectory, arguments);
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage());
        }

        context.println("@info exit-code [%d]", exitCode);

        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code " + exitCode);
        }
    }

    @Override
    public String toString() {
        return "RunStatement{" + "expressions=" + expressions + "}";
    }

    /**
     * Parses a RunStatement from the given parser.
     *
     * @param parser the parser to read from
     * @return a RunStatement instance
     */
    public static Statement parse(Parser parser) {
        Line line = parser.nextSequence();

        Token keyword = line.expect(
                Token.Type.LITERAL,
                "run",
                "run::bash",
                "run::dash",
                "run::direct",
                "run::fish",
                "run::ksh",
                "run::sh",
                "run::zsh",
                "run::exec",
                "run::direct");

        int index = keyword.lexeme.indexOf("::");
        String qualifier = (index > 0) ? keyword.lexeme.substring(index + 2) : "bash";

        boolean useShell = !"direct".equals(qualifier);

        line.expect(Token.Type.WHITESPACE);
        line.expect(Token.Type.LITERAL, "[");

        List<Expression> expressions = new ArrayList<>();

        while (true) {
            Line argumentLine = parser.peekSequence();

            if (argumentLine == null) {
                throw new RuntimeException("Unexpected end of input while parsing run statement");
            }

            if (argumentLine.isEmpty()) {
                parser.nextSequence();
                continue;
            }

            if (argumentLine.size() == 1) {
                Token token = argumentLine.tokens().get(0);
                if (token.type == Token.Type.LITERAL && token.lexeme.equals("]")) {
                    parser.nextSequence(); // consume the ]
                    break;
                }
            }

            argumentLine = parser.nextSequence();
            expressions.add(ExpressionParser.parseStringExpression(argumentLine));
        }

        return new RunStatement(qualifier, expressions, useShell);
    }

    /**
     * Runs the given command in a separate process and prints its output.
     *
     * @param context the context in which to run the command
     * @param workingDirectory the working directory for the command
     * @param arguments the command arguments to run
     * @return the exit code of the command
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    private static int runCommand(Context context, Path workingDirectory, List<String> arguments)
            throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(arguments);
        builder.directory(workingDirectory.toFile());
        builder.redirectErrorStream(true);

        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                context.println("@output " + line);
            }
        }

        return process.waitFor();
    }
}
