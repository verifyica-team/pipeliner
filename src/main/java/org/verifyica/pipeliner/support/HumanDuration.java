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

package org.verifyica.pipeliner.support;

import java.time.Duration;

/**
 * Utility class to convert a Duration to a human-readable format.
 * <p>
 * This class provides a method to convert a Duration object into a string that represents the duration
 * in a human-readable format, such as milliseconds, seconds, or minutes.
 */
public class HumanDuration {

    /**
     * Constructor
     */
    private HumanDuration() {
        // INTENTIONALLY BLANK
    }

    /**
     * Converts a Duration to a human-readable string.
     *
     * @param duration the duration to convert
     * @return a string representing the duration in a human-readable format
     */
    public static String humanDuration(Duration duration) {
        // Get the nanos from the duration
        long nanos = duration.toNanos();

        if (nanos < 1_000_000) {
            // Less than 1 millisecond
            return String.format("%d ns", nanos);
        }

        // Convert nanos to milliseconds
        double ms = nanos / 1_000_000.0;
        if (ms < 1000) {
            // Less than 1 second
            return String.format("%.3f ms", ms);
        }

        // Convert nanos to seconds
        double secs = nanos / 1_000_000_000.0;
        if (secs < 60) {
            // Less than 1 minute
            return String.format("%.3f s", secs);
        }

        // Convert nanos to minutes, seconds, and milliseconds
        long totalMillis = duration.toMillis();
        long minutes = totalMillis / 60000;
        long seconds = totalMillis / 1000 % 60;
        long subMillis = totalMillis % 1000;

        return String.format("%d:%02d.%03d", minutes, seconds, subMillis);
    }
}
