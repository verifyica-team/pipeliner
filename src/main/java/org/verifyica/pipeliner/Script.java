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

package org.verifyica.pipeliner;

import java.io.Reader;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.parser.Parser;
import org.verifyica.pipeliner.core.statement.Statement;

/**
 * Represents a Pipeliner script that can be executed.
 */
public class Script {

    private final Statement statement;

    /**
     * Constructor
     *
     * @param statement the root statement of the script
     */
    private Script(Statement statement) {
        this.statement = statement;
    }

    /**
     * Executes the script in the given context.
     *
     * @param context the context in which to execute the script
     */
    public void execute(Context context) {
        statement.execute(context);
    }

    /**
     * Factory method to create a Script from a Reader.
     *
     * @param reader the reader to read the script from
     * @return a new Script instance
     */
    public static Script create(Reader reader) {
        Parser parser = new Parser();
        Statement statement = parser.parse(reader);
        return new Script(statement);
    }
}
