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

package org.verifyica.pipeliner.core.parser;

import org.verifyica.pipeliner.core.Statement;
import org.verifyica.pipeliner.core.statements.ExecStatement;
import org.verifyica.pipeliner.core.statements.HaltStatement;
import org.verifyica.pipeliner.core.statements.NoOpStatement;
import org.verifyica.pipeliner.core.statements.PrintLnStatement;
import org.verifyica.pipeliner.core.statements.ScopeStatement;
import org.verifyica.pipeliner.core.statements.ShellStatement;
import org.verifyica.pipeliner.core.statements.SleepStatement;
import org.verifyica.pipeliner.core.statements.VariableAssignmentStatement;
import org.verifyica.pipeliner.core.statements.WorkingDirectoryStatement;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * Parses statements from a {@code LineLexer}.
 */
public class StatementParser {

    private static final StatementParser SINGLETON = new StatementParser();

    /**
     * Constructor
     */
    private StatementParser() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Parses a single statement from the input.
     *
     * @param lineLexer the LineLexer to read from
     * @return the parsed statement or null if no statement could be parsed
     */
    public Statement parse(LineLexer lineLexer) {
        Line line;

        while ((line = lineLexer.peek()) != null) {
            Token token = line.peek();

            // Switch on the lexeme
            switch (token.lexeme) {
                case "{":
                    return ScopeStatement.parse(lineLexer);
                case "environment-variable":
                case "env":
                case "variable":
                case "var":
                    return VariableAssignmentStatement.parse(lineLexer);
                case "execute":
                case "exec":
                    return ExecStatement.parse(lineLexer);
                case "halt":
                    return HaltStatement.parse(lineLexer);
                case "println":
                    return PrintLnStatement.parse(lineLexer);
                case "shell":
                    return ShellStatement.parse(lineLexer);
                case "sleep":
                    return SleepStatement.parse(lineLexer);
                case "working-directory":
                    return WorkingDirectoryStatement.parse(lineLexer);
                // Ignored
                case "print":
                    lineLexer.consume();
                    return new NoOpStatement(line);
                default:
                    break;
            }

            // Unknown keyword
            throw new SyntaxException("Unknown keyword '" + token.lexeme + "' at " + token.location);
        }

        return null;
    }

    /**
     * Factory method to get a singleton instance of StatementParser.
     *
     * @return the singleton instance of StatementParser
     */
    public static StatementParser singleton() {
        return SINGLETON;
    }
}
