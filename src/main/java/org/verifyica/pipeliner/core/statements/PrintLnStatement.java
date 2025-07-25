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
import org.verifyica.pipeliner.core.parser.ExpressionParser;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.LineLexer;
import org.verifyica.pipeliner.core.parser.LineMatcher;
import org.verifyica.pipeliner.core.statements.expression.LiteralExpression;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * A statement that prints a line.
 */
public final class PrintLnStatement implements Statement {

    private static final LineMatcher LINE_MATCHER_1 =
            new LineMatcher().literal("println").whitespace().literal("[").eol();

    private static final LineMatcher LINE_MATCHER_2 =
            new LineMatcher().literal("println").whitespace().anyLiteral();

    private static final LineMatcher LINE_MATCHER_3 = new LineMatcher().literal("println");

    private static final LineMatcher END_1_MATCHER =
            new LineMatcher().size(1).literal("|").eol();

    private static final LineMatcher END_2_MATCHER =
            new LineMatcher().size(1).literal("]").eol();

    private final List<Expression> expressions;

    /**
     * Constructor
     *
     * @param expressions the list of expressions to evaluate and print
     */
    public PrintLnStatement(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public void execute(Context context) {
        for (Expression expression : expressions) {
            String message = expression.evaluate(context).asString();
            context.println(message);
        }
    }

    @Override
    public String toString() {
        return "PrintLnStatement2{" + "expressions=" + expressions + '}';
    }

    /**
     * Parses a PrintLn statement from the given LineLexer.
     *
     * @param lineLexer the LineLexer to read from
     * @return a new PrintLnStatement instance
     * @throws SyntaxException if the syntax is invalid
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.next();

        // println + <whitespace> + [
        if (LINE_MATCHER_1.isMatch(line)) {
            line.consume(); // println
            line.consume(); // whitespace
            line.consume(); // [

            List<Expression> expressions = new ArrayList<>();

            while (true) {
                Line statementLine = lineLexer.peek();
                if (statementLine == null) {
                    throw new SyntaxException(
                            "Unexpected end of input while parsing shell statement at " + line.location());
                }

                if (END_1_MATCHER.isMatch(statementLine) || END_2_MATCHER.isMatch(statementLine)) {
                    statementLine.consume();
                    break;
                }

                statementLine = lineLexer.next();
                expressions.add(ExpressionParser.parseExpression(statementLine));
            }

            return new PrintLnStatement(expressions);
        }

        // println + <whitespace> + <any literal>
        if (LINE_MATCHER_2.isMatch(line)) {
            line.consume(); // println
            line.consume(); // whitespace
            return new PrintLnStatement(List.of(ExpressionParser.parseExpression(line)));
        }

        // println
        if (LINE_MATCHER_3.isMatch(line)) {
            return new PrintLnStatement(List.of(new LiteralExpression("")));
        }

        // Invalid syntax
        throw new SyntaxException("Invalid println syntax at " + line.location());
    }
}
