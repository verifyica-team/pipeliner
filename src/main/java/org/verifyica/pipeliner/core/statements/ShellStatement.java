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
import java.util.stream.Collectors;
import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.core.Statement;
import org.verifyica.pipeliner.core.parser.DelimitedBlockParser;
import org.verifyica.pipeliner.core.parser.EolParser;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.LineLexer;
import org.verifyica.pipeliner.core.parser.LiteralInSetParser;
import org.verifyica.pipeliner.core.parser.LiteralParser;
import org.verifyica.pipeliner.core.parser.OptionalParser;
import org.verifyica.pipeliner.core.parser.Token;
import org.verifyica.pipeliner.core.statements.expression.LiteralExpression;
import org.verifyica.pipeliner.exception.SyntaxException;
import org.verifyica.pipeliner.util.ProcessExecutor;

/**
 * A statement to run a shell command.
 */
public final class ShellStatement implements Statement {

    private static final LiteralParser KEYWORD_PARSER = LiteralParser.of("shell");

    private static final LiteralParser SCOPE_PARSER = LiteralParser.of("::");

    private static final Set<String> SHELLS = Set.of("ash", "bash", "dash", "fish", "ksh", "sh", "zsh");

    private static final LiteralInSetParser SHELL_PARSER = LiteralInSetParser.of(SHELLS);

    private static final DelimitedBlockParser BLOCK_ARGUMENTS_PARSER = DelimitedBlockParser.of("]");

    private static final OptionalParser OPTIONAL_WHITESPACE_PARSER = OptionalParser.of(Token.Type.WHITESPACE);

    private static final EolParser EOL_PARSER = EolParser.singleton();

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
        return "ShellStatement{shell='" + shell + "', expressions=" + expressions + "}";
    }

    /**
     * Parses a shell statement from the given {@code LineLexer}.
     *
     * @param lineLexer the {@code LineLexer} to read from
     * @return a new ShellStatement2 instance
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.consume();

        KEYWORD_PARSER.parse(line); // shell
        SCOPE_PARSER.parse(line); // ::
        String shell = SHELL_PARSER.parse(line); // shell name
        OPTIONAL_WHITESPACE_PARSER.parse(line); // optional whitespace

        Token token = line.peek();
        if (token == null) {
            throw new SyntaxException(line.location() + ": Expected shell command or block, but found end of line");
        }

        if (!"[".equals(token.lexeme)) {
            // shell with single argument
            return new ShellStatement(shell, List.of(new LiteralExpression(line.asString())));
        } else {
            line.consume(); // [
            EOL_PARSER.parse(line); // eol
            List<String> lines = BLOCK_ARGUMENTS_PARSER.parse(lineLexer);
            List<Expression> expressions =
                    lines.stream().map(LiteralExpression::new).collect(Collectors.toList());

            return new ShellStatement(shell, expressions);
        }
    }
}
