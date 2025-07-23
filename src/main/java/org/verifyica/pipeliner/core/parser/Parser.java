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

import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.verifyica.pipeliner.core.exception.SyntaxException;
import org.verifyica.pipeliner.core.statement.BlockStatement;
import org.verifyica.pipeliner.core.statement.CdStatement;
import org.verifyica.pipeliner.core.statement.EnvStatement;
import org.verifyica.pipeliner.core.statement.HaltStatement;
import org.verifyica.pipeliner.core.statement.IfFalseStatement;
import org.verifyica.pipeliner.core.statement.IfTrueStatement;
import org.verifyica.pipeliner.core.statement.PrintStatement;
import org.verifyica.pipeliner.core.statement.PropStatement;
import org.verifyica.pipeliner.core.statement.RunStatement;
import org.verifyica.pipeliner.core.statement.SleepStatement;
import org.verifyica.pipeliner.core.statement.Statement;
import org.verifyica.pipeliner.core.statement.VarStatement;

/**
 * Parser for Pipeliner DSL.
 */
public class Parser {

    private static final Map<String, Function<Parser, Statement>> STATEMENT_PARSERS = Map.ofEntries(
            Map.entry("{", BlockStatement::parse),
            Map.entry("cd", CdStatement::parse),
            Map.entry("env", EnvStatement::parse),
            Map.entry("halt::ok", HaltStatement::parse),
            Map.entry("halt::error", HaltStatement::parse),
            Map.entry("if::true", IfTrueStatement::parse),
            Map.entry("if::false", IfFalseStatement::parse),
            Map.entry("macro", BlockStatement::parse),
            Map.entry("prop", PropStatement::parse),
            Map.entry("print", PrintStatement::parse),
            Map.entry("print::info", PrintStatement::parse),
            Map.entry("print::warning", PrintStatement::parse),
            Map.entry("print::error", PrintStatement::parse),
            Map.entry("run", RunStatement::parse),
            Map.entry("run::bash", RunStatement::parse),
            Map.entry("run::dash", RunStatement::parse),
            Map.entry("run::direct", RunStatement::parse),
            Map.entry("run::fish", RunStatement::parse),
            Map.entry("run::ksh", RunStatement::parse),
            Map.entry("run::sh", RunStatement::parse),
            Map.entry("run::zsh", RunStatement::parse),
            Map.entry("sleep", SleepStatement::parse),
            Map.entry("var", VarStatement::parse));

    private LineReader lineReader;
    private Statement rootStatement;
    private final Map<String, Statement> macroStatements;
    private int scopeDepth = 0;

    /**
     * Constructor
     */
    public Parser() {
        macroStatements = new LinkedHashMap<>();
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
     * Returns the next line from the reader.
     *
     * @return the next line or {@code null} if EOF is reached
     */
    public Line peekSequence() {
        return lineReader.peekSequence();
    }

    /**
     * Returns the next line from the reader and advances the reader.
     *
     * @return the next line {@code null} if EOF is reached
     */
    public Line nextSequence() {
        return lineReader.nextSequence();
    }

    /**
     * Parses a program from the given reader.
     *
     * @param reader the reader to read the program from
     * @return the root statement of the parsed program
     */
    public Statement parse(Reader reader) {
        lineReader = new LineReader(reader);

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

        return new BlockStatement(statements);
    }

    /**
     * Returns the root statement of the parsed program.
     *
     * @return the root statement
     */
    public Statement getRootStatement() {
        return rootStatement;
    }

    /**
     * Parses a single statement from the input.
     *
     * @return the parsed statement or null if no statement could be parsed
     */
    public Statement parseStatement() {
        while (true) {
            // Peek the next line
            Line line = peekSequence();

            // If no line is available, return null to indicate end of input
            if (line == null) {
                return null;
            }

            // Get the first token in the line
            Token token = line.peek();

            // Get the lexeme of the token
            String lexeme = token.lexeme;

            // Switch on the lexeme
            switch (lexeme) {
                case "#":
                case "//": {
                    nextSequence();
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

                    return STATEMENT_PARSERS.get("macro").apply(this);
                }
            }

            // Lookup statement parser
            Function<Parser, Statement> parseFunction = STATEMENT_PARSERS.get(lexeme);

            // If we found a function for parsing the keyword/symbol
            if (parseFunction != null) {
                // Parse the statement using the corresponding function
                return parseFunction.apply(this);
            }

            // Handle error for unknown keyword/symbol
            throw new SyntaxException("Unknown keyword: '" + lexeme + "' at " + token.location);
        }
    }

    /**
     * Parses a block comment from the input.
     */
    private void parseComment() {
        while (true) {
            Line line = peekSequence();
            if (line == null) {
                throw new SyntaxException("Unterminated block comment");
            }

            List<Token> tokens = line.tokens();
            int size = tokens.size();

            // Must end with "*/" followed by EOL or EOF, and nothing else
            if (size >= 1) {
                Token last = tokens.get(size - 1);

                if ("*/".equals(last.lexeme)) {
                    nextSequence();
                    return;
                }
            }

            nextSequence();
        }
    }
}
