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

/**
 * Represents a location in the source code, defined by a line number and a column number.
 */
public class Location {

    /**
     * The line number in the source code.
     */
    public final int lineNumber;

    /**
     * The column number in the source code.
     */
    public final int columnNumber;

    /**
     * Constructor
     *
     * @param lineNumber the line number
     * @param columnNumber the column number
     */
    public Location(int lineNumber, int columnNumber) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    @Override
    public String toString() {
        return "line " + lineNumber + ":" + columnNumber;
    }
}
