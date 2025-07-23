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

import java.util.ArrayList;
import java.util.List;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.exception.SyntaxException;

/**
 * Represents a block of statements defined in a scope.
 */
public class ScopeStatement implements Statement {

    private final List<Statement> statements;

    /**
     * Constructor
     *
     * @param statements the list of instructions in this block
     */
    public ScopeStatement(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public void execute(Context context) {
        /*
        if (context.getScopeLevel() > 0) {
            context.println("# {");
        }
        */

        context.pushScope();

        for (Statement statement : statements) {
            statement.execute(context);
        }

        context.popScope();

        /*
        if (context.getScopeLevel() > 0) {
            context.println("# }");
        }
        */
    }

    /**
     * Parses a block statement from the given statementParser.
     *
     * @param statementParser the statement parser to read from
     * @return a new BlockInstruction instance
     */
    public static Statement parse(StatementParser statementParser) {
        Line line = statementParser.nextLine();

        line.expect(Token.Type.LITERAL, "{");
        line.expectEmpty();

        statementParser.incrementScopeDepth();

        List<Statement> statements = new ArrayList<>();

        while (true) {
            Statement statement = statementParser.parseStatement();
            if (statement == null) {
                break;
            }
            statements.add(statement);
        }

        statementParser.decrementScopeDepth();

        line = statementParser.peekLine();
        if (line == null) {
            throw new SyntaxException("Unexpected end of input in block");
        }

        line = statementParser.nextLine();
        line.expect(Token.Type.LITERAL, "}");
        line.expectEmpty();

        return new ScopeStatement(statements);
    }
}
