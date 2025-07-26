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
 * A statement to execute a command.
 */
public final class ExecStatement implements Statement {

    private static final Set<String> KEYWORDS = Set.of("execute", "exec");

    private static final LiteralInSetParser KEYWORD_PARSER = LiteralInSetParser.of(KEYWORDS);

    private static final OptionalParser OPTIONAL_WHITESPACE_PARSER = OptionalParser.of(Token.Type.WHITESPACE);

    private static final LiteralParser START_PARSER = LiteralParser.of("(");

    private static final EolParser EOL_PARSER = EolParser.singleton();

    private static final DelimitedBlockParser BLOCK_ARGUMENTS_PARSER = DelimitedBlockParser.of(")");

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
        return "ExecStatement{expressions=" + expressions + "}";
    }

    /**
     * Parses an exec statement from the given {@code LineLexer}.
     *
     * @param lineLexer the {@code LineLexer} to read from
     * @return a new ExecStatement instance
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.consume();

        KEYWORD_PARSER.parse(line); // execute or exec
        OPTIONAL_WHITESPACE_PARSER.parse(line); // optional whitespace
        START_PARSER.parse(line); // opening parenthesis
        EOL_PARSER.parse(line); // end of line

        // Parse the block arguments
        List<String> arguments = BLOCK_ARGUMENTS_PARSER.parse(lineLexer);
        if (arguments.isEmpty()) {
            throw new SyntaxException(line, "expected at least one argument for exec statement");
        }

        List<Expression> expressions = new ArrayList<>();
        for (String argument : arguments) {
            expressions.add(new LiteralExpression(argument));
        }

        return new ExecStatement(expressions);
    }
}
