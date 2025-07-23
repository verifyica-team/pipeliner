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

import java.util.List;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.parser.Parser;

/**
 * A statement that executes a named macro containing other statements.
 */
public final class MacroStatement implements Statement {

    private final String name;
    private final List<Statement> statements;

    /**
     * Constructor
     *
     * @param name the name of the macro
     * @param statements the list of statements that make up the macro
     */
    public MacroStatement(String name, List<Statement> statements) {
        this.name = name;
        this.statements = statements;
    }

    /**
     * Get the name of the macro.
     *
     * @return the name of the macro
     */
    public String getName() {
        return name;
    }

    @Override
    public void execute(Context context) {
        context.println("@info macro >> %s", name);

        for (Statement statement : statements) {
            statement.execute(context);
        }

        context.println("@info macro << %s", name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MacroStatement{");
        sb.append("name='").append(name).append('\'');
        sb.append(", statements [");
        for (int i = 0; i < statements.size(); i++) {
            sb.append(statements.get(i).toString());
            if (i < statements.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Parses a MacroStatement from the given parser.
     *
     * @param parser the parser to read from
     * @return a new MacroStatement instance
     */
    public static Statement parse(Parser parser) {
        /*
        parser.parseKeyword("macro", "Expected 'macro' keyword");
        parser.parseRemaining();

        return new MacroStatement("TBD", new ArrayList<>());
        */
        return null;
    }
}
