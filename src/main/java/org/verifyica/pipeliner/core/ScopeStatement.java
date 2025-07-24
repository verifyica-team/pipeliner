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

import java.util.ArrayList;
import java.util.List;
import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * Represents a block of statements defined in a scope.
 */
public class ScopeStatement implements Statement {

    /**
     * The line matcher for the start of a scope statement.
     */
    private static final LineMatcher START_SCOPE_MATCHER =
            new LineMatcher().size(1).literal("{").eol();

    /**
     * The line matcher for the end of a scope statement.
     */
    private static final LineMatcher END_SCOPE_MATCHER =
            new LineMatcher().size(1).literal("}").eol();

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
        context.pushScope();

        for (Statement statement : statements) {
            statement.execute(context);
        }

        context.popScope();
    }

    /**
     * Parses a scope statement from the provided line lexer.
     *
     * @param lineLexer the line lexer to read lines from
     * @return a ScopeStatement object containing the parsed statements
     * @throws SyntaxException if the syntax is incorrect or the scope is not properly closed
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.next();

        START_SCOPE_MATCHER.match(line);

        List<Statement> statements = new ArrayList<>();

        while (true) {
            Line innerLine = lineLexer.peek();

            if (innerLine == null) {
                throw new SyntaxException("Unterminated scope statement at " + line.location());
            }

            if (innerLine.isEmpty()) {
                lineLexer.next();
                continue;
            }

            if (END_SCOPE_MATCHER.isMatch(innerLine)) {
                break;
            }

            Statement statement = StatementParser.parse(lineLexer);
            if (statement == null) {
                break;
            }
            statements.add(statement);
        }

        line = lineLexer.next();
        END_SCOPE_MATCHER.match(line);

        return new ScopeStatement(statements);
    }
}
