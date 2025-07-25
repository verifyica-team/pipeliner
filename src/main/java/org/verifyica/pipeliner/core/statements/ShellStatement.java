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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.core.Statement;
import org.verifyica.pipeliner.core.parser.ExpressionParser;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.LineLexer;
import org.verifyica.pipeliner.core.parser.LineMatcher;
import org.verifyica.pipeliner.exception.SyntaxException;
import org.verifyica.pipeliner.util.ProcessExecutor;

/**
 * A statement to run a shell command.
 */
public final class ShellStatement implements Statement {

    /**
     * The names of supported shells.
     */
    private static final Set<String> SUPPORTED_SHELLS = Set.of("ash", "bash", "dash", "fish", "ksh", "sh", "zsh");

    private static final LineMatcher LINE_MATCHER_1 = new LineMatcher()
            .literal("shell")
            .literal("::")
            .literalInSet(SUPPORTED_SHELLS)
            .whitespace()
            .literal("[")
            .eol();

    private static final LineMatcher LINE_MATCHER_2 = new LineMatcher()
            .literal("shell")
            .literal("::")
            .literalInSet(SUPPORTED_SHELLS)
            .whitespace()
            .anyLiteral();

    private static final LineMatcher END_MATCHER =
            new LineMatcher().size(1).literal("]").eol();

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
                        environmentVariables, workingDirectory, arguments, line -> context.println("> " + line));

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
     * Parses a shell statement from the given line lexer.
     *
     * @param lineLexer the line lexer to read from
     * @return a new ShellStatement2 instance
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.next();

        if (LINE_MATCHER_1.isMatch(line)) {
            line.consume(); // shell
            line.consume(); // ::
            String shell = line.consume().lexeme; // shell name
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

            return new ShellStatement(shell, expressions);
        }

        if (LINE_MATCHER_2.isMatch(line)) {
            line.consume(); // shell
            line.consume(); // ::
            String shell = line.consume().lexeme; // shell name
            line.consume(); // whitespace

            List<Expression> expressions = new ArrayList<>();
            expressions.add(ExpressionParser.parseExpression(line));

            return new ShellStatement(shell, expressions);
        }

        throw new SyntaxException("Invalid shell statement at " + line.location());
    }
}
