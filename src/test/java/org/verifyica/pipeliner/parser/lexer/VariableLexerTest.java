/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.parser.lexer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Class to implement VariableLexerTest */
public class VariableLexerTest {

    /**
     * Method to test the VariableLexer, validating the token list returned is equal the expected token list
     *
     * @param testData the test data
     */
    @ParameterizedTest
    @MethodSource("getVariableLexerTestData")
    public void testParser(TestData testData) {
        VariableLexer lexer = new VariableLexer(testData.input());
        List<VariableLexer.Token> tokens = lexer.tokenize();

        assertThat(tokens).isNotNull();
        assertThat(tokens).hasSize(testData.expectedTokens().size());

        // Assert specific variable lexer token values, since the test/TestData currently doesn't use position
        for (int i = 0; i < tokens.size(); i++) {
            assertThat(tokens.get(i).getType())
                    .isEqualTo(testData.expectedTokens().get(i).getType());
            assertThat(tokens.get(i).getText())
                    .isEqualTo(testData.expectedTokens().get(i).getText());
        }
    }

    /**
     * Method to get the test data
     *
     * @return the test data
     */
    public static Stream<TestData> getVariableLexerTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData()
                .input("test")
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.TEXT, "test")));

        list.add(new TestData()
                .input("required:test")
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.MODIFIER, "required"))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.TEXT, "test")));

        list.add(new TestData()
                .input("required:foo.bar.variable_1")
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.MODIFIER, "required"))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.SCOPE, "foo"))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.SCOPE, "bar"))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.TEXT, "variable_1")));

        list.add(new TestData()
                .input("foo:required:foo.bar.variable_1")
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.MODIFIER, "foo"))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.MODIFIER, "required"))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.SCOPE, "foo"))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.SCOPE, "bar"))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.TEXT, "variable_1")));

        list.add(new TestData()
                .input("foo.")
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.SCOPE, "foo"))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.TEXT, "")));

        list.add(new TestData()
                .input(".")
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.SCOPE, ""))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.TEXT, "")));

        list.add(new TestData()
                .input(":")
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.MODIFIER, ""))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.TEXT, "")));

        list.add(new TestData()
                .input("..")
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.SCOPE, ""))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.SCOPE, ""))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.TEXT, "")));

        list.add(new TestData()
                .input("::")
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.MODIFIER, ""))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.MODIFIER, ""))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.TEXT, "")));

        list.add(new TestData()
                .input(":..")
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.MODIFIER, ""))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.SCOPE, ""))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.SCOPE, ""))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.TEXT, "")));

        list.add(new TestData()
                .input("..:")
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.SCOPE, ""))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.SCOPE, ""))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.MODIFIER, ""))
                .expectedToken(new VariableLexer.Token(VariableLexer.Token.Type.TEXT, "")));

        return list.stream();
    }

    /** Class to implement TestData */
    public static class TestData {

        private String input;
        private final List<VariableLexer.Token> expectedTokens;

        /** Constructor */
        public TestData() {
            expectedTokens = new ArrayList<>();
        }

        /**
         * Method to set the input string
         *
         * @param input inputString
         * @return the TestData
         */
        public TestData input(String input) {
            this.input = input;
            return this;
        }

        /**
         * Method to get the input
         *
         * @return the input
         */
        public String input() {
            return input;
        }

        /**
         * Method to add an expected token
         *
         * @param token the token
         * @return the TestData
         */
        public TestData expectedToken(VariableLexer.Token token) {
            this.expectedTokens.add(token);
            return this;
        }

        /**
         * Method to get the expected tokens
         *
         * @return the expected tokens
         */
        public List<VariableLexer.Token> expectedTokens() {
            return expectedTokens;
        }

        @Override
        public String toString() {
            return input;
        }
    }
}
