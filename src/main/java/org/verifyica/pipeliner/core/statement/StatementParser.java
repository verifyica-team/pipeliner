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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.verifyica.pipeliner.core.exception.SyntaxException;

/**
 * StatementParser for Pipeliner DSL.
 */
public class StatementParser {

    private static final Map<String, Function<StatementParser, Statement>> KEYWORD_PARSERS = Map.ofEntries(
            Map.entry("{", ScopeStatement::parse),
            Map.entry("cd", CdStatement::parse),
            Map.entry("env", EnvStatement::parse),
            Map.entry("exec", ExecStatement::parse),
            Map.entry("halt", HaltStatement::parse),
            Map.entry("if", IfStatement::parse),
            Map.entry("macro", ScopeStatement::parse),
            Map.entry("print", PrintStatement::parse),
            Map.entry("println", PrintLnStatement::parse),
            Map.entry("shell", ShellStatement::parse),
            Map.entry("sleep", SleepStatement::parse),
            Map.entry("var", VarStatement::parse));

    private final LineLexer lineLexer;
    private final Deque<Line> lineBuffer;
    private int scopeDepth = 0;

    /**
     * Constructor
     *
     * @param lineLexer the lexer to read lines from
     */
    public StatementParser(LineLexer lineLexer) {
        this.lineLexer = lineLexer;
        this.lineBuffer = new ArrayDeque<>();
    }

    /**
     * Increments the scope depth.
     */
    public void incrementScopeDepth() {
        scopeDepth++;
    }

    /**
     * Decrements the scope depth.
     */
    public void decrementScopeDepth() {
        scopeDepth--;
    }

    /**
     * Peeks the next {@code Line} from the reader.
     *
     * @return the next statement or {@code null} if EOF is reached
     */
    public Line peekLine() {
        if (lineBuffer.isEmpty()) {
            Line next = lineLexer.nextLine();
            if (next != null) {
                lineBuffer.addFirst(next);
            }
        }
        return lineBuffer.peekFirst();
    }

    /**
     * Returns the next {@code Line} from the reader and advances the reader.
     *
     * @return the next {@code Line} or {@code null} if EOF is reached
     */
    public Line nextLine() {
        if (!lineBuffer.isEmpty()) {
            return lineBuffer.removeFirst();
        }
        return lineLexer.nextLine();
    }

    /**
     * Pushes a {@code Line} back onto the buffer.
     *
     * @param line the line to push back
     */
    public void pushBack(Line line) {
        if (line != null) {
            lineBuffer.addFirst(line);
        } else {
            throw new IllegalArgumentException("Cannot push back null line");
        }
    }

    /**
     * Parses a program from the given reader.
     *
     * @return the root statement of the parsed program
     */
    public Statement parse() {
        List<Statement> statements = new ArrayList<>();

        while (true) {
            Statement statement = parseStatement();
            if (statement == null) {
                break;
            }

            statements.add(statement);
        }

        if (scopeDepth < 0) {
            throw new SyntaxException("Unexpected closing brace '}'");
        }

        if (scopeDepth > 0) {
            throw new SyntaxException("Expected closing brace '}'");
        }

        return new ScopeStatement(statements);
    }

    /**
     * Parses a single statement from the input.
     *
     * @return the parsed statement or null if no statement could be parsed
     */
    public Statement parseStatement() {
        while (true) {
            // Peek the next line
            Line line = peekLine();

            // If no line is available, return null to indicate end of input
            if (line == null) {
                return null;
            }

            // Get the keyword token from the statement
            Token token = line.peek();

            // Get the lexeme of the token
            String keyword = token.lexeme;

            // Switch on the lexeme
            switch (keyword) {
                case "#":
                case "//": {
                    nextLine();
                    continue;
                }
                case "/*": {
                    parseComment();
                    continue;
                }
                case "}": {
                    return null;
                }
                case "macro": {
                    if (scopeDepth != 1) {
                        throw new SyntaxException(
                                "Macro not allowed here (depth " + scopeDepth + ") at " + token.location);
                    }

                    return KEYWORD_PARSERS.get("macro").apply(this);
                }
            }

            // Lookup statement parser
            Function<StatementParser, Statement> instructionParserFunction = KEYWORD_PARSERS.get(keyword);

            // If we found a function for parsing the keyword
            if (instructionParserFunction != null) {
                // Parse the statement using the corresponding function
                return instructionParserFunction.apply(this);
            }

            // Handle error for unknown keyword
            throw new SyntaxException("Unknown keyword: '" + keyword + "' at " + token.location);
        }
    }

    /**
     * Parses a block comment from the input.
     */
    private void parseComment() {
        while (true) {
            Line line = peekLine();
            if (line == null) {
                throw new SyntaxException("Unterminated block comment");
            }

            List<Token> tokens = line.tokens();
            int size = tokens.size();

            // "*/" must be the last token in the statement
            if (size >= 1) {
                Token last = tokens.get(size - 1);

                if ("*/".equals(last.lexeme)) {
                    nextLine();
                    return;
                }
            }

            nextLine();
        }
    }
}
