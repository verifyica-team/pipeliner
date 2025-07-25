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

import java.util.List;
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
 * Utility class for parsing statements from a LineLexer.
 */
public class StatementParser {

    private StatementParser() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Parses a single statement from the input.
     *
     * @param lineLexer the LineLexer to read from
     * @return the parsed statement or null if no statement could be parsed
     */
    public static Statement parse(LineLexer lineLexer) {
        while (true) {
            // Peek the next line
            Line line = lineLexer.peek();

            // If no line is available, return null to indicate end of input
            if (line == null) {
                return null;
            }

            // If the line is empty, skip it
            if (line.isEmpty()) {
                lineLexer.next();
                continue;
            }

            if (line.isComment()) {
                lineLexer.next();
                continue;
            }

            // Get the keyword token from the statement
            Token token = line.peek();

            // Get the lexeme of the token
            String keyword = token.lexeme;

            // Switch on the lexeme
            switch (keyword) {
                case "/*": {
                    parseComment(lineLexer);
                    continue;
                }
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
                    lineLexer.next();
                    return new NoOpStatement(line);
                default:
                    break;
            }

            // Unknown keyword
            throw new SyntaxException("Unknown keyword '" + keyword + "' at " + token.location);
        }
    }

    /**
     * Parses a block comment from the input.
     *
     * @param lineLexer the LineLexer to read from
     */
    private static void parseComment(LineLexer lineLexer) {
        while (true) {
            Line line = lineLexer.peek();
            if (line == null) {
                throw new SyntaxException("Unterminated block comment");
            }

            List<Token> tokens = line.tokens();
            int size = tokens.size();

            // "*/" must be the last token in the statement
            if (size >= 1) {
                Token last = tokens.get(size - 1);

                if ("*/".equals(last.lexeme)) {
                    lineLexer.next();
                    return;
                }
            }

            lineLexer.next();
        }
    }
}
