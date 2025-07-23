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

import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.expression.Expression;

/**
 * A conditional statement that executes a block if the condition evaluates to true.
 */
public final class IfStatement implements Statement {

    private final Expression condition;
    private final Statement statement;

    /**
     * Constructor
     *
     * @param condition the condition to evaluate
     * @param statement the statement to execute if the condition is true
     */
    public IfStatement(Expression condition, Statement statement) {
        this.condition = condition;
        this.statement = statement;
    }

    @Override
    public void execute(Context context) {
        // statement.execute(context);
    }

    /**
     * Parses an if statement from the given statementParser.
     *
     * @param statementParser the statement parser to read from
     * @return a new IfInstruction instance
     */
    public static Statement parse(StatementParser statementParser) {
        /*
        List<Token> tokens = statementParser.collectLine();
        for (Token token : tokens) {
            System.out.println("if token " + token);
        }

        statementParser.expectToken(tokens.get(0), Token.Type.LITERAL, "if::true");
        statementParser.expectToken(tokens.get(1), Token.Type.WHITESPACE);

        statementParser.expectToken(tokens.get(tokens.size() - 3), Token.Type.WHITESPACE);
        statementParser.expectToken(tokens.get(tokens.size() - 2), Token.Type.LITERAL, "{");
        statementParser.expectToken(tokens.get(tokens.size() - 1), Token.Type.EOL);

        while (!statementParser.isEOF()) {
            Token token = statementParser.peekSequence();
            if (token.type == Token.Type.LITERAL && token.lexeme.equals("}")) {
                statementParser.nextTokenList();
                break;
            }

            statementParser.skipWhitespace();
            statementParser.expectToken(Token.Type.LITERAL, "}");
        }

        return new IfTrueInstruction(null, null);
        */
        return null;
    }

    @Override
    public String toString() {
        return "IfTrueInstruction{" + "condition=" + condition + ", statement=" + statement + '}';
    }
}
