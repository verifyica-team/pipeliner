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
import org.verifyica.pipeliner.core.parser.Parser;

/**
 * A conditional statement that executes a block if the condition evaluates to true.
 */
public final class IfTrueStatement implements Statement {

    private final Expression condition;
    private final Statement statement;

    /**
     * Constructor
     *
     * @param condition the condition to evaluate
     * @param statement the statement to execute if the condition is true
     */
    public IfTrueStatement(Expression condition, Statement statement) {
        this.condition = condition;
        this.statement = statement;
    }

    @Override
    public void execute(Context context) {
        // statement.execute(context);
    }

    /**
     * Parses an if statement from the given parser.
     *
     * @param parser the parser to read from
     * @return a new IfStatement instance
     */
    public static Statement parse(Parser parser) {
        /*
        List<Token> tokens = parser.collectLine();
        for (Token token : tokens) {
            System.out.println("if token " + token);
        }

        parser.expectToken(tokens.get(0), Token.Type.LITERAL, "if::true");
        parser.expectToken(tokens.get(1), Token.Type.WHITESPACE);

        parser.expectToken(tokens.get(tokens.size() - 3), Token.Type.WHITESPACE);
        parser.expectToken(tokens.get(tokens.size() - 2), Token.Type.LITERAL, "{");
        parser.expectToken(tokens.get(tokens.size() - 1), Token.Type.EOL);

        while (!parser.isEOF()) {
            Token token = parser.peekSequence();
            if (token.type == Token.Type.LITERAL && token.lexeme.equals("}")) {
                parser.nextTokenList();
                break;
            }

            parser.skipWhitespace();
            parser.expectToken(Token.Type.LITERAL, "}");
        }

        return new IfTrueStatement(null, null);
        */
        return null;
    }

    @Override
    public String toString() {
        return "IfTrueStatement{" + "condition=" + condition + ", statement=" + statement + '}';
    }
}
