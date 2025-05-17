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

package org.verifyica.pipeliner.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Class to implement SetOf */
public class SetOf {

    /**
     * Constructor
     */
    private SetOf() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get create a set from a variable number of values
     *
     * @param <T> the type
     * @param values the values
     * @return a set
     */
    @SafeVarargs
    public static <T> Set<T> of(T... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
