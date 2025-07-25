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

import java.util.Set;
import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.core.Statement;
import org.verifyica.pipeliner.core.parser.ExpressionParser;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.LineLexer;
import org.verifyica.pipeliner.core.parser.LineMatcher;
import org.verifyica.pipeliner.core.statements.expression.NullExpression;
import org.verifyica.pipeliner.exception.HaltException;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * A statement that pauses execution for a given number of milliseconds.
 */
public final class HaltStatement implements Statement {

    private static final Set<String> QUALIFIERS = Set.of("ok", "error");

    private static final LineMatcher LINE_MATCHER_1 = new LineMatcher()
            .literal("halt")
            .literal("::")
            .literalInSet(QUALIFIERS)
            .whitespace()
            .numberInRange(0, 255)
            .eol();

    private static final LineMatcher LINE_MATCHER_2 = new LineMatcher()
            .literal("halt")
            .literal("::")
            .literalInSet(QUALIFIERS)
            .whitespace()
            .numberInRange(0, 255)
            .whitespace()
            .anyLiteral();

    private final String qualifier;
    private final int exitCode;
    private final Expression expression;

    /**
     * Constructor
     *
     * @param qualifier the qualifier for the halt statement, e.g., "ok" or "error"
     * @param exitCode the exit code to use when halting execution
     * @param expression the expression that evaluates to a message to display on halt
     */
    public HaltStatement(String qualifier, int exitCode, Expression expression) {
        this.qualifier = qualifier;
        this.exitCode = exitCode;
        this.expression = expression;
    }

    @Override
    public void execute(Context context) {
        String message = expression.evaluate(context).asString();
        if (message != null && !message.isEmpty()) {
            context.println("# halt::%s %d %s", qualifier, exitCode, message);
            throw new HaltException(qualifier, exitCode, message);
        } else {
            context.println("# halt::%s %d", qualifier, exitCode);
            throw new HaltException(qualifier, exitCode);
        }
    }

    @Override
    public String toString() {
        return "HaltStatement{qualifier='" + qualifier + "', exitCode=" + exitCode + ", expression='" + expression
                + "'}";
    }

    /**
     * Parses a halt statement from the given line lexer.
     *
     * @param lineLexer the line lexer to read from
     * @return a new HaltStatement instance
     * @throws SyntaxException if the line does not match a valid halt statement
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.next();

        if (LINE_MATCHER_1.isMatch(line)) {
            line.consume(); // halt
            line.consume(); // ::
            String qualifier = line.consume().lexeme; // qualifier
            line.consume(); // whitespace
            String exitCode = line.consume().lexeme;
            return new HaltStatement(qualifier, Integer.parseInt(exitCode), NullExpression.SINGLETON);
        }

        if (LINE_MATCHER_2.isMatch(line)) {
            line.consume(); // halt
            line.consume(); // ::
            String qualifier = line.consume().lexeme; // qualifier
            line.consume(); // whitespace
            String exitCode = line.consume().lexeme;
            line.consume(); // whitespace

            Expression expression = ExpressionParser.parseExpression(line);
            return new HaltStatement(qualifier, Integer.parseInt(exitCode), expression);
        }

        throw new SyntaxException("Invalid halt statement: " + line);
    }
}
