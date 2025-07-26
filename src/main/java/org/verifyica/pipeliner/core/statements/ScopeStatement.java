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

import java.util.ArrayList;
import java.util.List;
import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.core.Statement;
import org.verifyica.pipeliner.core.parser.EolParser;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.LineLexer;
import org.verifyica.pipeliner.core.parser.LiteralParser;
import org.verifyica.pipeliner.core.parser.StatementParser;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * Represents a block of statements defined in a scope.
 */
public class ScopeStatement implements Statement {

    private static final StatementParser STATEMENT_PARSER = StatementParser.singleton();

    private static final LiteralParser SCOPE_START_PARSER = LiteralParser.of("{");

    private static final EolParser EOL_PARSER = EolParser.singleton();

    private final List<Statement> statements;

    /**
     * Constructor
     *
     * @param statements the list of instructions in this scope
     */
    public ScopeStatement(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public void execute(Context context) {
        // Enter a new scope in the context
        context.enterScope();

        // Execute each statement in the scope
        for (Statement statement : statements) {
            statement.execute(context);
        }

        // Leave the scope
        context.leaveScope();
    }

    /**
     * Parses a scope statement from the given {@code LineLexer}.
     *
     * @param lineLexer the {@code LineLexer} to read lines from
     * @return a ScopeStatement object containing the parsed statements
     * @throws SyntaxException if the syntax is incorrect or the scope is not properly closed
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.consume();

        SCOPE_START_PARSER.parse(line); // {
        EOL_PARSER.parse(line); // eol

        List<Statement> statements = new ArrayList<>();

        while (true) {
            // Peek at the inner line
            Line innerLine = lineLexer.peek();

            // If no line, we have an unterminated scope
            if (innerLine == null) {
                throw new SyntaxException("Unterminated scope: missing closing '}'");
            }

            // End of scope
            if ("}".equals(innerLine.asString())) {
                lineLexer.consume(); // }
                break;
            }

            // Beginning of new scope
            if ("{".equals(innerLine.asString())) {
                // Scope start detected, parse nested scope
                Statement nested = ScopeStatement.parse(lineLexer);
                statements.add(nested);
                continue;
            }

            // Regular statement
            Statement statement = STATEMENT_PARSER.parse(lineLexer);
            statements.add(statement);
        }

        return new ScopeStatement(statements);
    }
}
