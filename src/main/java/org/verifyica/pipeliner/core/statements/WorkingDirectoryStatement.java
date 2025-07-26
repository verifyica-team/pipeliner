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
import java.util.Set;
import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.core.Statement;
import org.verifyica.pipeliner.core.parser.ArgumentParser;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.LineLexer;
import org.verifyica.pipeliner.core.parser.LiteralInSetParser;
import org.verifyica.pipeliner.core.parser.OptionalParser;
import org.verifyica.pipeliner.core.parser.Token;
import org.verifyica.pipeliner.core.statements.expression.LiteralExpression;

/**
 * A statement to change the working directory.
 */
public class WorkingDirectoryStatement implements Statement {

    private static final Set<String> KEYWORDS = Set.of("working-directory", "work-dir");

    private static final LiteralInSetParser KEYWORD_PARSER = LiteralInSetParser.of(KEYWORDS);

    private static final OptionalParser OPTIONAL_WHITESPACE_PARSER = OptionalParser.of(Token.Type.WHITESPACE);

    private static final ArgumentParser ARGUMENT_PARSER = ArgumentParser.singleton();

    private final String keyword;
    private final Expression expression;

    /**
     * Constructor
     *
     * @param keyword the keyword used in the statement (e.g., "working-directory" or "work-dir")
     * @param expression the expression that evaluates to the directory path to change to
     */
    public WorkingDirectoryStatement(String keyword, Expression expression) {
        this.keyword = keyword;
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

        context.println("# %s %s", keyword, string);

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
     * Parses a cd statement from the given {@code LineLexer}.
     *
     * @param lineLexer the {@code LineLexer} to read from
     * @return a new CdStatement instance
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.consume();

        String keyword = KEYWORD_PARSER.parse(line); // working-directory or work-dir
        OPTIONAL_WHITESPACE_PARSER.parse(line); // optional whitespace
        String path = ARGUMENT_PARSER.parse(line); // argument (directory path)
        Expression expression = new LiteralExpression(path);

        return new WorkingDirectoryStatement(keyword, expression);
    }

    @Override
    public String toString() {
        return "WorkingDirectoryStatement{" + "expression=" + expression + "}";
    }
}
