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
 * A conditional statement that executes a block if the condition evaluates to false.
 */
public final class IfFalseStatement implements Statement {

    private final Expression condition;
    private final Statement statement;

    /**
     * Constructor
     *
     * @param condition the condition to evaluate
     * @param statement the statement to execute if the condition is false
     */
    public IfFalseStatement(Expression condition, Statement statement) {
        this.condition = condition;
        this.statement = statement;
    }

    @Override
    public void execute(Context context) {
        statement.execute(context);
    }

    /**
     * Parses an if statement from the given parser.
     *
     * @param parser the parser to read from
     * @return a new IfStatement instance
     */
    public static Statement parse(Parser parser) {
        /*
        parser.expectToken(Token.Type.LITERAL, "if::false");
        parser.expectToken(Token.Type.WHITESPACE);

        StringBuilder sb = new StringBuilder();
        Token lastToken = null;

        while (!parser.isEOF()) {
            Token token = parser.peekSequence();

            // Stop before `{`, but ensure it's preceded by whitespace
            if (token.type == Token.Type.LITERAL && token.lexeme.equals("{")) {
                if (lastToken == null || lastToken.type != Token.Type.WHITESPACE) {
                    throw new SyntaxException("Expected whitespace before '{' at " + token.position);
                }
                break;
            }

            lastToken = parser.nextTokenList();
            sb.append(lastToken.lexeme);
        }

        LiteralExpression condition = new LiteralExpression(sb.toString().trim());

        // parseBlockStatement will consume { EOL ... } internally
        Statement block = parser.parseBlockStatement();

        return new IfFalseStatement(condition, block);
        */
        return null;
    }

    @Override
    public String toString() {
        return "IfFalseStatement{" + "condition=" + condition + ", statement=" + statement + '}';
    }
}
