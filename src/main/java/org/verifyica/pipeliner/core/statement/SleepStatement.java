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

import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.exception.SyntaxException;

/**
 * A statement that pauses execution for a given number of milliseconds.
 */
public final class SleepStatement implements Statement {

    private final long milliseconds;

    /**
     * Constructor
     *
     * @param milliseconds the number of milliseconds to sleep
     */
    public SleepStatement(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    @Override
    public void execute(Context context) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return "SleepInstruction{" + "milliseconds=" + milliseconds + '}';
    }

    /**
     * Parses a sleep statement from the given statementParser.
     *
     * @param statementParser the statement parser to read from
     * @return a new SleepInstruction instance
     */
    public static Statement parse(StatementParser statementParser) {
        Line line = statementParser.nextLine();

        // Expect "sleep" keyword
        line.expect(Token.Type.LITERAL, "sleep");
        line.expectWhitespace();

        // Expect duration value as a LITERAL
        Token numberToken = line.expect(Token.Type.LITERAL);
        long value;
        try {
            value = Long.parseLong(numberToken.lexeme);
        } catch (NumberFormatException e) {
            throw new SyntaxException("Expected numeric value for sleep duration at  " + numberToken.location);
        }

        // Default multiplier is 1 millisecond
        long multiplier = 1;

        if (!line.isEmpty()) {
            line.expectWhitespace();
            Token unitToken = line.expect(Token.Type.LITERAL);
            multiplier = resolveUnitToMilliseconds(unitToken.lexeme, unitToken.location);
        }

        long durationMillis = value * multiplier;
        return new SleepStatement(durationMillis);
    }

    private static long resolveUnitToMilliseconds(String unit, Location location) {
        switch (unit) {
            case "ms":
            case "millisecond":
            case "milliseconds": {
                return 1;
            }
            case "s":
            case "second":
            case "seconds": {
                return 1000;
            }
            case "m":
            case "minute":
            case "minutes": {
                return 60_000;
            }
            default:
                throw new SyntaxException("Unknown time unit '" + unit + "' at " + location);
        }
    }
}
