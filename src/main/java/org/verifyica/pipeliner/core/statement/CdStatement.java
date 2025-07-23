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

import static java.lang.String.format;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.expression.Expression;
import org.verifyica.pipeliner.core.expression.LiteralExpression;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.Parser;
import org.verifyica.pipeliner.core.parser.Token;

/**
 * A statement to change the current directory.
 */
public class CdStatement implements Statement {

    private final Expression expression;

    /**
     * Constructor
     *
     * @param expression the expression that evaluates to the directory path to change to
     */
    public CdStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(Context context) {
        String string = expression.evaluate(context).asString().trim();

        Path path = Paths.get(string);

        Path base = context.resolveWorkingDirectory();

        // Ensure base is absolute before resolving against it
        if (!base.isAbsolute()) {
            base = base.toAbsolutePath().normalize();
        }

        Path resolved =
                path.isAbsolute() ? path.normalize() : base.resolve(path).normalize();

        context.println("@info cd %s", string);

        File file = resolved.toFile();

        if (!file.exists()) {
            throw new RuntimeException(format("path [%s] does not exist", resolved));
        }

        if (!file.canRead()) {
            throw new RuntimeException(format("path [%s] is not readable", resolved));
        }

        if (!file.isDirectory()) {
            throw new RuntimeException(format("path [%s] is not a directory", resolved));
        }

        context.currentFrame().setWorkingDirectory(resolved);
    }

    /**
     * Parses a cd statement from the given parser.
     *
     * @param parser the parser to read from
     * @return a new CdStatement instance
     */
    public static Statement parse(Parser parser) {
        Line line = parser.nextSequence();

        line.expect(Token.Type.LITERAL, "cd");
        line.expectWhitespace();
        Token pathToken = line.expect(Token.Type.LITERAL);
        Expression expression = new LiteralExpression(pathToken.lexeme);

        return new CdStatement(expression);
    }

    @Override
    public String toString() {
        return "CdStatement{" + "expression=" + expression + "}";
    }
}
