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
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.Parser;
import org.verifyica.pipeliner.core.parser.Token;

/**
 * Represents a block of statements enclosed in curly braces.
 */
public class BlockStatement implements Statement {

    private final List<Statement> statements;

    /**
     * Constructor
     *
     * @param statements the list of statements in this block
     */
    public BlockStatement(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public void execute(Context context) {
        context.pushFrame();

        for (Statement statement : statements) {
            statement.execute(context);
        }

        context.popFrame();
    }

    /**
     * Parses a block statement from the given parser.
     *
     * @param parser the parser to read from
     * @return a new BlockStatement instance
     */
    public static Statement parse(Parser parser) {
        Line line = parser.nextSequence();

        line.trim();
        line.expect(Token.Type.LITERAL, "{");

        parser.incrementScopeDepth();

        List<Statement> statements = new ArrayList<>();

        while (true) {
            Statement statement = parser.parseStatement();
            if (statement == null) {
                break;
            }
            statements.add(statement);
        }

        parser.decrementScopeDepth();

        line = parser.peekSequence();
        if (line == null) {
            throw new SyntaxException("Unexpected end of input in block");
        }

        line = parser.nextSequence();
        line.trim();
        line.expect(Token.Type.LITERAL, "}");

        return new BlockStatement(statements);
    }
}
