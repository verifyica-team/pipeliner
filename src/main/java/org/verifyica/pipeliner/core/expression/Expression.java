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

package org.verifyica.pipeliner.core.expression;

import org.verifyica.pipeliner.core.Context;

/**
 * Interface for all DSL expressions that produce string values.
 */
public interface Expression {

    /**
     * Evaluates the expression in the given context and returns the resulting string.
     *
     * @param context the context in which to evaluate the expression
     * @return the result of evaluating the expression
     */
    Result evaluate(Context context);
}
