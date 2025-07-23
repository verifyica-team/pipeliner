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
 * A statement that calls a macro.
 */
public class CallStatement implements Statement {

    private final Expression expression;

    /**
     * Constructor
     *
     * @param expression the expression that evaluates to the macro name
     */
    public CallStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(Context context) {
        String macroName = expression.evaluate(context).asString();
        System.out.println("@info call >> [" + macroName + "]");
        // Statement statement = context.resolveMacro(macroName);
        // statement.execute(context);
    }

    /**
     * Parses a call statement from the given parser.
     *
     * @param parser the parser to read from
     * @return a new CallStatement instance
     */
    public static Statement parse(Parser parser) {
        /*
        parser.parseKeyword("call", "Expected 'call'");
        parser.parseWhitespace("Expected whitespace after 'call'");

        Expression expression = new LiteralExpression("call placeholder");

        return new CallStatement(expression);
        */
        return null;
    }

    @Override
    public String toString() {
        return "CallStatement{" + "expression=" + expression + '}';
    }
}
