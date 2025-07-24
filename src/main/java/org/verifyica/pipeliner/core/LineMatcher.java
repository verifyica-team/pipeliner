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

package org.verifyica.pipeliner.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.verifyica.pipeliner.exception.SyntaxException;

/**
 * A declarative matcher for validating a line of tokens using a sequence of expected matchers.
 * Supports required, optional, wildcard, terminal, and grouped matchers.
 */
public class LineMatcher {

    final List<Matcher> matchers = new ArrayList<>();

    /**
     * Constructor
     */
    public LineMatcher() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Matches a literal token.
     *
     * @param literal the literal string to match
     * @return this LineMatcher instance for chaining
     */
    public LineMatcher literal(String literal) {
        matchers.add(new LiteralMatcher(literal));
        return this;
    }

    /**
     * Matches a literal token from a set of allowed values.
     *
     * @param set the set of allowed literal strings
     * @return this LineMatcher instance for chaining
     */
    public LineMatcher literalInSet(Set<String> set) {
        matchers.add(new LiteralInSetMatcher(set));
        return this;
    }

    /**
     * Matches an optional literal token.
     *
     * @param literal the literal string to match
     * @return this LineMatcher instance for chaining
     */
    public LineMatcher optionalLiteral(String literal) {
        matchers.add(new OptionalLiteralMatcher(literal));
        return this;
    }

    /**
     * Matches an optional literal from a set of allowed values.
     *
     * @param set the set of allowed literal strings
     * @return this LineMatcher instance for chaining
     */
    public LineMatcher optionalLiteralInSet(Set<String> set) {
        matchers.add(new OptionalLiteralInSetMatcher(set));
        return this;
    }

    /**
     * Matches a whitespace token.
     *
     * @return this LineMatcher instance for chaining
     */
    public LineMatcher whitespace() {
        matchers.add(new WhitespaceMatcher());
        return this;
    }

    /**
     * Matches an optional whitespace token.
     *
     * @return this LineMatcher instance for chaining
     */
    public LineMatcher optionalWhitespace() {
        matchers.add(new OptionalWhitespaceMatcher());
        return this;
    }

    /**
     * Matches any literal token.
     *
     * @return this LineMatcher instance for chaining
     */
    public LineMatcher anyLiteral() {
        matchers.add(new AnyLiteralMatcher());
        return this;
    }

    /**
     * Matches a number within a specified range.
     *
     * @param min the minimum value of the range (inclusive)
     * @param max the maximum value of the range (inclusive)
     * @return this LineMatcher instance for chaining
     */
    public LineMatcher numberInRange(long min, long max) {
        matchers.add(new NumberInRange(min, max));
        return this;
    }

    /**
     * Matches the end of the line.
     *
     * @return this LineMatcher instance for chaining
     */
    public LineMatcher eol() {
        matchers.add(new EolMatcher());
        return this;
    }

    /**
     * Matches a group of matchers optionally — either all succeed and are consumed, or none.
     *
     * @param group the LineMatcher representing the group to match optionally
     * @return this LineMatcher instance for chaining
     */
    public LineMatcher optionalGroup(LineMatcher group) {
        matchers.add(new OptionalGroupMatcher(group.matchers));
        return this;
    }

    /**
     * Matches if the line has exactly the specified number of tokens.
     *
     * @param size the exact number of tokens expected in the line
     * @return this LineMatcher instance for chaining
     */
    public LineMatcher size(int size) {
        matchers.add(new SizeMatcher(size));
        return this;
    }

    /**
     * Returns true if all matchers match the line.
     *
     * @param line the input line
     * @return true if the line matches this pattern
     */
    public boolean isMatch(Line line) {
        int index = 0;

        for (Matcher matcher : matchers) {
            if (matcher instanceof OptionalGroupMatcher) {
                OptionalGroupMatcher ogm = (OptionalGroupMatcher) matcher;
                int consumed = ogm.consumed(line, index);
                if (consumed > 0) {
                    index += consumed;
                }
                continue;
            }

            if (matcher instanceof OptionalMatcher) {
                if (index < line.size() && matcher.matches(line, index)) {
                    index++;
                }
                continue;
            }

            if (!matcher.matches(line, index)) {
                return false;
            }

            if (!(matcher instanceof TerminalMatcher)) {
                index++;
            }
        }

        return true;
    }

    /**
     * Validates the line and throws a SyntaxException if it does not match the pattern.
     *
     * @param line the tokenized input line
     */
    public void match(Line line) {
        if (!isMatch(line)) {
            throw new SyntaxException("Expected " + this + " at " + line.location());
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Matcher m : matchers) {
            stringBuilder.append(m).append(" ");
        }
        return stringBuilder.toString().trim();
    }

    /**
     * Base interface for matchers at a given token index.
     */
    private interface Matcher {

        /**
         * Checks if this matcher matches the token at the given index in the line.
         *
         * @param line the line to match against
         * @param index the index of the token to check
         * @return true if it matches, false otherwise
         */
        boolean matches(Line line, int index);
    }

    /**
     * Marker for optional matchers that may not consume input.
     */
    private interface OptionalMatcher {
        // INTENTIONALLY EMPTY
    }

    /**
     * Marker for matchers that do not consume a token even if matched (e.g. eol, size).
     */
    private interface TerminalMatcher {
        // INTENTIONALLY EMPTY
    }

    /**
     * Matches a literal string token.
     */
    private static class LiteralMatcher implements Matcher {

        private final String expected;

        /**
         * Constructor
         *
         * @param expected the expected literal string to match
         */
        LiteralMatcher(String expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Line line, int index) {
            return index < line.size() && expected.equals(line.tokens().get(index).lexeme);
        }

        @Override
        public String toString() {
            return "literal(\"" + expected + "\")";
        }
    }

    /**
     * Matches an optional literal string token.
     */
    private static class OptionalLiteralMatcher implements Matcher, OptionalMatcher {

        private final String expected;

        /**
         * Constructor
         *
         * @param expected the optional literal string to match
         */
        OptionalLiteralMatcher(String expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Line line, int index) {
            return index >= line.size() || expected.equals(line.tokens().get(index).lexeme);
        }

        @Override
        public String toString() {
            return "optionalLiteral(\"" + expected + "\")";
        }
    }

    /**
     * Matches an literal from a set.
     */
    private static class LiteralInSetMatcher implements Matcher, OptionalMatcher {

        private final Set<String> allowed;

        /**
         * Constructor
         *
         * @param allowed the set of allowed literal strings
         */
        LiteralInSetMatcher(Set<String> allowed) {
            this.allowed = new TreeSet<>(allowed);
        }

        @Override
        public boolean matches(Line line, int index) {
            return index >= line.size() || allowed.contains(line.tokens().get(index).lexeme);
        }

        @Override
        public String toString() {
            return "literalInSet(" + allowed + ")";
        }
    }

    /**
     * Matches an optional literal from a set.
     */
    private static class OptionalLiteralInSetMatcher implements Matcher, OptionalMatcher {

        private final Set<String> allowed;

        /**
         * Constructor
         *
         * @param allowed the set of allowed literal strings
         */
        OptionalLiteralInSetMatcher(Set<String> allowed) {
            this.allowed = new TreeSet<>(allowed);
        }

        @Override
        public boolean matches(Line line, int index) {
            return index >= line.size() || allowed.contains(line.tokens().get(index).lexeme);
        }

        @Override
        public String toString() {
            return "optionalLiteralInSet(" + allowed + ")";
        }
    }

    /**
     * Matches a number within a specified range.
     */
    private static class NumberInRange implements Matcher {

        private final long min;
        private final long max;

        /**
         * Constructor
         *
         * @param min the minimum value of the range (inclusive)
         * @param max the maximum value of the range (inclusive)
         */
        public NumberInRange(long min, long max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean matches(Line line, int index) {
            if (index >= line.size()) {
                return false;
            }

            String lexeme = line.tokens().get(index).lexeme;

            try {
                long value = Long.parseLong(lexeme);
                return value >= min && value <= max;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    /**
     * Matches a required WHITESPACE token.
     */
    private static class WhitespaceMatcher implements Matcher {

        /**
         * Constructor
         */
        public WhitespaceMatcher() {
            // INTENTIONALLY EMPTY
        }

        @Override
        public boolean matches(Line line, int index) {
            return index < line.size() && line.tokens().get(index).type == Type.WHITESPACE;
        }

        @Override
        public String toString() {
            return "whitespace()";
        }
    }

    /**
     * Matches an optional WHITESPACE token.
     */
    private static class OptionalWhitespaceMatcher implements Matcher, OptionalMatcher {

        /**
         * Constructor
         */
        public OptionalWhitespaceMatcher() {
            // INTENTIONALLY EMPTY
        }

        @Override
        public boolean matches(Line line, int index) {
            return index >= line.size() || line.tokens().get(index).type == Type.WHITESPACE;
        }

        @Override
        public String toString() {
            return "optionalWhitespace()";
        }
    }

    /**
     * Matches any literal token.
     */
    private static class AnyLiteralMatcher implements Matcher {

        @Override
        public boolean matches(Line line, int index) {
            return index < line.size() && line.tokens().get(index).type == Type.LITERAL;
        }

        @Override
        public String toString() {
            return "anyLiteral()";
        }
    }

    /**
     * Matches end-of-line.
     */
    private static class EolMatcher implements Matcher, TerminalMatcher {

        /**
         * Constructor
         */
        public EolMatcher() {
            // INTENTIONALLY EMPTY
        }

        @Override
        public boolean matches(Line line, int index) {
            return index == line.size();
        }

        @Override
        public String toString() {
            return "eol()";
        }
    }

    /**
     * Matches if the line has exactly the given number of tokens.
     */
    private static class SizeMatcher implements Matcher, TerminalMatcher {

        private final int expected;

        /**
         * Constructor
         *
         * @param expected the exact number of tokens expected in the line
         */
        SizeMatcher(int expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Line line, int index) {
            return line.size() == expected;
        }

        @Override
        public String toString() {
            return "size(" + expected + ")";
        }
    }

    /**
     * Matches a group of matchers optionally — either all match and are consumed, or none.
     */
    private static class OptionalGroupMatcher implements Matcher, OptionalMatcher {

        private final List<Matcher> group;

        /**
         * Constructor
         *
         * @param group the list of matchers that form the optional group
         */
        OptionalGroupMatcher(List<Matcher> group) {
            this.group = List.copyOf(group);
        }

        @Override
        public boolean matches(Line line, int index) {
            return true; // always succeeds; evaluation is done via consumed()
        }

        /**
         * Consumes the group if it matches, returning the number of tokens consumed.
         * If it does not match, returns 0 to indicate failure of the entire group.
         *
         * @param line the line to match against
         * @param startIndex the starting index in the line
         * @return the number of tokens consumed, or 0 if it fails
         */
        public int consumed(Line line, int startIndex) {
            int index = startIndex;

            for (Matcher matcher : group) {
                if (matcher instanceof OptionalMatcher) {
                    if (index < line.size() && matcher.matches(line, index)) {
                        index++;
                    }
                } else {
                    if (!matcher.matches(line, index)) {
                        return 0; // fail entire group
                    }
                    if (!(matcher instanceof TerminalMatcher)) {
                        index++;
                    }
                }
            }

            return index - startIndex;
        }

        @Override
        public String toString() {
            return "optionalGroup(" + group + ")";
        }
    }
}
