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

package org.verifyica.pipeliner.core.statements.expression;

import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.core.statements.Expression;

/**
 * Expression that represents a null string value.
 */
public final class NullExpression implements Expression {

    /**
     * Singleton instance of NullExpression
     */
    public static final NullExpression SINGLETON = new NullExpression();

    /**
     * Constructor
     *
     */
    private NullExpression() {
        // INTENTIONALLY EMPTY
    }

    @Override
    public Result evaluate(Context context) {
        return new Result(null);
    }

    @Override
    public String toString() {
        return "NullExpression{ }";
    }
}
