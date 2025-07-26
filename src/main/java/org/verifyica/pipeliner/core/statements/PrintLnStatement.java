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

import java.util.List;
import java.util.stream.Collectors;
import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.core.Statement;
import org.verifyica.pipeliner.core.parser.DelimitedBlockParser;
import org.verifyica.pipeliner.core.parser.EolParser;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.LineLexer;
import org.verifyica.pipeliner.core.parser.LiteralParser;
import org.verifyica.pipeliner.core.parser.OptionalParser;
import org.verifyica.pipeliner.core.parser.Token;
import org.verifyica.pipeliner.core.statements.expression.LiteralExpression;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * A statement that prints a line.
 */
public final class PrintLnStatement implements Statement {

    private static final LiteralParser KEYWORD_PARSER = LiteralParser.of("println");

    private static final OptionalParser OPTIONAL_WHITESPACE_PARSER = OptionalParser.of(Token.Type.WHITESPACE);

    private static final DelimitedBlockParser BLOCK_ARGUMENTS_PARSER = DelimitedBlockParser.of("]");

    private static final EolParser EOL_PARSER = EolParser.singleton();

    /*
    private static final LineMatcher LINE_MATCHER_1 =
            new LineMatcher().literal("println").whitespace().literal("[").eol();

    private static final LineMatcher LINE_MATCHER_2 =
            new LineMatcher().literal("println").whitespace().anyLiteral();

    private static final LineMatcher LINE_MATCHER_3 = new LineMatcher().literal("println");

    private static final LineMatcher BLOCK_END_MATCHER =
            new LineMatcher().size(1).literal("]").eol();
    */

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
        Line line = lineLexer.consume();

        KEYWORD_PARSER.parse(line); // println
        OPTIONAL_WHITESPACE_PARSER.parse(line); // optional whitespace

        Token token = line.peek();
        if (token == null) {
            // println with no arguments
            return new PrintLnStatement(List.of(new LiteralExpression("")));
        }

        if (!"[".equals(token.lexeme)) {
            // println with single expression
            return new PrintLnStatement(List.of(new LiteralExpression(line.asString())));
        } else {
            line.consume();
            EOL_PARSER.parse(line); // consume the opening bracket
            List<String> lines = BLOCK_ARGUMENTS_PARSER.parse(lineLexer);
            List<Expression> expressions =
                    lines.stream().map(LiteralExpression::new).collect(Collectors.toList());
            return new PrintLnStatement(expressions);
        }
    }
}
