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
import org.verifyica.pipeliner.core.parser.EolParser;
import org.verifyica.pipeliner.core.parser.Line;
import org.verifyica.pipeliner.core.parser.LineLexer;
import org.verifyica.pipeliner.core.parser.LiteralInSetParser;
import org.verifyica.pipeliner.core.parser.LiteralParser;
import org.verifyica.pipeliner.core.parser.NumberParser;
import org.verifyica.pipeliner.core.parser.OptionalParser;
import org.verifyica.pipeliner.core.parser.Token;
import org.verifyica.pipeliner.core.parser.WhitespaceParser;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * A statement that pauses execution for a given number of milliseconds.
 */
public final class SleepStatement implements Statement {

    private static final LiteralParser KEYWORD_PARSER = LiteralParser.of("sleep");

    private static final OptionalParser OPTIONAL_WHITESPACE_PARSER = OptionalParser.of(Token.Type.WHITESPACE);

    private static final NumberParser NUMBER_PARSER = NumberParser.of();

    private static final Set<String> UNITS =
            Set.of("ms", "millisecond", "milliseconds", "s", "second", "seconds", "m", "minute", "minutes");

    private static final WhitespaceParser REQUIRED_WHITESPACE_PARSER = WhitespaceParser.singleton();

    private static final LiteralInSetParser UNITS_PARSER = LiteralInSetParser.of(UNITS);

    private static final EolParser EOL_PARSER = EolParser.singleton();

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
        return "SleepStatement{" + "milliseconds=" + milliseconds + '}';
    }

    /**
     * Parses a sleep statement from the given {@code LineLexer}.
     *
     * @param lineLexer the {@code LineLexer} to parse from
     * @return a SleepStatement instance
     * @throws SyntaxException if the syntax is invalid
     */
    public static Statement parse(LineLexer lineLexer) {
        Line line = lineLexer.consume();

        KEYWORD_PARSER.parse(line); // sleep
        OPTIONAL_WHITESPACE_PARSER.parse(line); // optional whitespace
        long duration = NUMBER_PARSER.parse(line); // duration

        Token token = line.peek();
        if (token == null) {
            return new SleepStatement(duration);
        }

        REQUIRED_WHITESPACE_PARSER.parse(line); // required whitespace
        String units = UNITS_PARSER.parse(line); // units
        EOL_PARSER.parse(line); // end of line

        duration = duration * resolveUnitToMilliseconds(line, units);

        return new SleepStatement(duration);
    }

    /**
     * Resolves the given time unit to milliseconds.
     *
     * @param units the time units to resolve
     * @return the number of milliseconds corresponding to the unit
     * @throws SyntaxException if the unit is unknown
     */
    private static long resolveUnitToMilliseconds(Line line, String units) {
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
                throw new SyntaxException(line, "unknown time units '" + units + "'");
        }
    }
}
