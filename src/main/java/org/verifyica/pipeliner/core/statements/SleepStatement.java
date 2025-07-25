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

package org.verifyica.pipeliner.core.statements;

import java.util.Set;
import org.verifyica.pipeliner.Context;
import org.verifyica.pipeliner.core.Statement;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.LineLexer;
import org.verifyica.pipeliner.core.parser.LineMatcher;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * A statement that pauses execution for a given number of milliseconds.
 */
public final class SleepStatement implements Statement {

    private static final Set<String> UNITS =
            Set.of("ms", "millisecond", "milliseconds", "s", "second", "seconds", "m", "minute", "minutes");

    private static final LineMatcher LINE_MATCHER_1 = new LineMatcher()
            .literal("sleep")
            .whitespace()
            .numberInRange(1, Long.MAX_VALUE)
            .eol();

    private static final LineMatcher LINE_MATCHER_2 = new LineMatcher()
            .literal("sleep")
            .whitespace()
            .numberInRange(1, Long.MAX_VALUE)
            .whitespace()
            .literalInSet(UNITS)
            .eol();

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
     * Parses a sleep statement from the given line lexer.
     *
     * @param lineLexer the line lexer to parse from
     * @return a SleepStatement instance
     * @throws SyntaxException if the syntax is invalid
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.next();

        if (LINE_MATCHER_1.isMatch(line)) {
            line.consume(); // sleep
            line.consume(); // whitespace
            String duration = line.consume().lexeme; // duration
            long milliseconds;

            try {
                milliseconds = Long.parseLong(duration);
            } catch (NumberFormatException e) {
                throw new SyntaxException("Expected numeric value for sleep duration at TODO");
            }

            return new SleepStatement(milliseconds);
        }

        if (LINE_MATCHER_2.isMatch(line)) {
            line.consume(); // sleep
            line.consume(); // whitespace
            String duration = line.consume().lexeme; // duration
            line.consume(); // whitespace
            String units = line.consume().lexeme; // units
            long milliseconds;

            try {
                milliseconds = Long.parseLong(duration);
            } catch (NumberFormatException e) {
                throw new SyntaxException("Expected numeric value for sleep duration at " + line.location());
            }

            long unitsMultiplier = resolveUnitToMilliseconds(units);
            milliseconds = milliseconds * unitsMultiplier;

            return new SleepStatement(milliseconds);
        }

        throw new SyntaxException("Invalid sleep statement at " + line.location());
    }

    /**
     * Resolves the given time unit to milliseconds.
     *
     * @param units the time units to resolve
     * @return the number of milliseconds corresponding to the unit
     * @throws SyntaxException if the unit is unknown
     */
    private static long resolveUnitToMilliseconds(String units) {
        switch (units) {
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
                throw new SyntaxException("Unknown time units '" + units + "'");
        }
    }
}
