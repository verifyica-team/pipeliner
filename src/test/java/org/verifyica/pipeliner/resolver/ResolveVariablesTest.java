/*
 * Copyright (C) 2024-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.verifyica.pipeliner.core.support.Resolver;
import org.verifyica.pipeliner.core.support.UnresolvedException;
import org.verifyica.pipeliner.parser.SyntaxException;

/** Class to implement ResolveVariablesTest */
public class ResolveVariablesTest {

    /**
     * Method to test the Resolver
     *
     * @param testData testData
     * @throws SyntaxException if an error occurs during parsing
     * @throws UnresolvedException if an error occurs during resolving
     */
    @ParameterizedTest
    @MethodSource("getTestData")
    public void testResolver(TestData testData) throws SyntaxException, UnresolvedException {
        Map<String, String> properties = Resolver.resolveVariables(testData.variables());

        String string = Resolver.resolveVariables(properties, testData.input());

        assertThat(string).isEqualTo(testData.expected());
    }

    public static Stream<TestData> getTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData()
                .variable("property_1", "${{ property_3 }}")
                .variable("property_2", "foo")
                .variable("property_3", "$PROPERTY_2")
                .input("echo $PROPERTY_1 ${{ property_2 }}")
                .expected("echo $PROPERTY_1 foo"));

        list.add(new TestData()
                .variable("property_1", "${{ property_3 }}")
                .variable("property_2", "foo")
                .variable("property_3", "$PROPERTY_2")
                .input("echo $PROPERTY_1 ${{property_2}}")
                .expected("echo $PROPERTY_1 foo"));

        list.add(new TestData()
                .variable("test_scripts_directory", "$PIPELINER_HOME/tests/scripts")
                .input("${{ test_scripts_directory }}/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\"")
                .expected("$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\""));

        list.add(
                new TestData()
                        .variable("test_scripts_directory", "$PIPELINER_HOME/tests/scripts")
                        .input(
                                "${{ test_scripts_directory }}/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\" \\${{ should.not.be.replaced }}")
                        .expected(
                                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\" \\${{ should.not.be.replaced }}"));

        list.add(new TestData()
                .variable("extension_property_1", "extension.bar")
                .input(
                        "echo captured extension property \\${{ extension_property_1 }} = \"${{ extension_property_1 }}\"")
                .expected("echo captured extension property \\${{ extension_property_1 }} = \"extension.bar\""));

        list.add(
                new TestData()
                        .variable("test_scripts_directory", "$PIPELINER_HOME/tests/scripts")
                        .input(
                                "${{ test_scripts_directory }}/test-arguments-are-equal.sh \"$PWD\" \"tests\" \"\\${{ should.not.be.replaced }}\"")
                        .expected(
                                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$PWD\" \"tests\" \"\\${{ should.not.be.replaced }}\""));

        list.add(new TestData()
                .variable("test_scripts_directory", "$PIPELINER_HOME/tests/scripts")
                .variable("a", "$B")
                .variable("b", "$C")
                .input("${{ test_scripts_directory }}/test-arguments-are-equal.sh \"$PWD\" ${{ a }} ${{ b }}")
                .expected("$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$PWD\" $B $C"));

        list.add(
                new TestData()
                        .variable("test_scripts_directory", "$PIPELINER_HOME/tests/scripts")
                        .input(
                                "${{ test_scripts_directory }}/test-arguments-are-equal.sh \"${{ test_scripts_directory }}\" \"${{ test_scripts_directory }}\"")
                        .expected(
                                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$PIPELINER_HOME/tests/scripts\" \"$PIPELINER_HOME/tests/scripts\""));

        return list.stream();
    }

    /** Class to implement TestData */
    public static class TestData {

        private final Map<String, String> variables;
        private String input;
        private String expected;

        /**
         * Constructor
         */
        public TestData() {
            variables = new TreeMap<>();
        }

        /**
         * Method to set a variable
         *
         * @param name the name
         * @param value the value
         * @return this
         */
        public TestData variable(String name, String value) {
            variables.put(name, value);
            return this;
        }

        /**
         * Method to get the variables
         *
         * @return the variables
         */
        public Map<String, String> variables() {
            return variables;
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
         * Method to set an expected string
         *
         * @param expected the expected
         * @return this
         */
        public TestData expected(String expected) {
            this.expected = expected;
            return this;
        }

        /**
         * Method to get the expected
         *
         * @return the expected
         */
        public String expected() {
            return expected;
        }
    }
}
