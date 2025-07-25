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

import static java.lang.String.format;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.core.Statement;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.LineLexer;
import org.verifyica.pipeliner.core.parser.LineMatcher;
import org.verifyica.pipeliner.core.statements.expression.LiteralExpression;

/**
 * A statement to change the working directory.
 */
public class WorkingDirectoryStatement implements Statement {

    private static final LineMatcher LINE_MATCHER =
            new LineMatcher().literal("working-directory").whitespace().anyLiteral();

    private final Expression expression;

    /**
     * Constructor
     *
     * @param expression the expression that evaluates to the directory path to change to
     */
    public WorkingDirectoryStatement(Expression expression) {
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

        context.println("# working-directory %s", string);

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

        context.currentScope().setWorkingDirectory(resolved);
    }

    /**
     * Parses a cd statement from the given line lexer.
     *
     * @param lineLexer the line lexer to read from
     * @return a new CdStatement instance
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.next();

        // cd + <whitespace> + anyLiteral
        LINE_MATCHER.match(line);
        line.consume(); // cd
        line.consume(); // whitespace

        String path = line.consume().lexeme; // path token
        Expression expression = new LiteralExpression(path);

        return new WorkingDirectoryStatement(expression);
    }

    @Override
    public String toString() {
        return "WorkingDirectoryStatement{" + "expression=" + expression + "}";
    }
}
