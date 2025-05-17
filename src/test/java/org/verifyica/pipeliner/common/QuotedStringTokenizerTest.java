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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Class to implement QuotedStringTokenizerTest */
public class QuotedStringTokenizerTest {

    /**
     * Method to test the QuotedStringTokenizer, validating the token list returned is equal the expected token list
     *
     * @param testData the test data
     */
    @ParameterizedTest
    @MethodSource("getTestData")
    public void testParser(TestData testData) {
        List<String> tokens = QuotedStringTokenizer.tokenize(testData.input());

        assertThat(tokens).isEqualTo(testData.expectedTokens());
    }

    public static Stream<TestData> getTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData().input("echo").expectedToken("echo"));

        list.add(new TestData()
                .input("echo \"Hello, World!\"")
                .expectedToken("echo")
                .expectedToken("Hello, World!"));

        list.add(new TestData()
                .input("echo     \"Hello, World!\"")
                .expectedToken("echo")
                .expectedToken("Hello, World!"));

        list.add(new TestData()
                .input("echo 'Hello, World!'")
                .expectedToken("echo")
                .expectedToken("Hello, World!"));

        list.add(new TestData()
                .input("echo     'Hello, World!'")
                .expectedToken("echo")
                .expectedToken("Hello, World!"));

        list.add(new TestData()
                .input("echo \"Hello, \\\"World!\"")
                .expectedToken("echo")
                .expectedToken("Hello, \"World!"));

        list.add(new TestData()
                .input("echo \"Hello, 'World!\"")
                .expectedToken("echo")
                .expectedToken("Hello, 'World!"));

        list.add(new TestData()
                .input("echo 'Hello, \\'World!'")
                .expectedToken("echo")
                .expectedToken("Hello, 'World!"));

        list.add(new TestData()
                .input("echo 'Hello, \"World!'")
                .expectedToken("echo")
                .expectedToken("Hello, \"World!"));

        list.add(new TestData()
                .input("echo 'Hello, \"World!\"'")
                .expectedToken("echo")
                .expectedToken("Hello, \"World!\""));

        list.add(new TestData()
                .input("echo \"Hello, 'World!'\"")
                .expectedToken("echo")
                .expectedToken("Hello, 'World!'"));

        return list.stream();
    }

    /** Class to implement TestData */
    private static class TestData {

        private String input;
        private final List<String> list;

        /**
         * Constructor
         */
        public TestData() {
            list = new ArrayList<>();
        }

        /**
         * Method to set the input
         *
         * @param input the input
         * @return this
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
         * Method to set an expected token
         *
         * @param token the token
         * @return this
         */
        public TestData expectedToken(String token) {
            list.add(token);
            return this;
        }

        /**
         * Method to get the expected tokens
         *
         * @return the expected tokens
         */
        public List<String> expectedTokens() {
            return list;
        }
    }
}
