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
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.verifyica.pipeliner.execution.support.Resolver;
import org.verifyica.pipeliner.execution.support.UnresolvedException;
import org.verifyica.pipeliner.parser.SyntaxException;

/** Class to implement ResolvePropertiesTest */
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
        Map<String, String> properties = Resolver.resolveVariables(testData.properties());

        String string = Resolver.replaceVariables(properties, testData.inputString());

        assertThat(string).isEqualTo(testData.expectedString());
    }

    public static Stream<TestData> getTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData()
                .variable("variable_1", "${{ variable_3 }}")
                .variable("variable_2", "foo")
                .variable("variable_3", "$VARIABLE_2")
                .inputString("echo $VARIABLE_1 ${{ variable_2 }}")
                .expectedString("echo $VARIABLE_1 foo"));

        list.add(new TestData()
                .variable("variable_1", "${{ variable_3 }}")
                .variable("variable_2", "foo")
                .variable("variable_3", "$VARIABLE_2")
                .inputString("echo $VARIABLE_1 ${{variable_2}}")
                .expectedString("echo $VARIABLE_1 foo"));

        list.add(new TestData()
                .variable("test_scripts_directory", "$PIPELINER_HOME/tests/scripts")
                .inputString("${{ test_scripts_directory }}/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\"")
                .expectedString(
                        "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\""));

        list.add(
                new TestData()
                        .variable("test_scripts_directory", "$PIPELINER_HOME/tests/scripts")
                        .inputString(
                                "${{ test_scripts_directory }}/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\" \\${{ should.not.be.replaced }}")
                        .expectedString(
                                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\" \\${{ should.not.be.replaced }}"));

        list.add(new TestData()
                .variable("extension_variable_1", "extension.bar")
                .inputString(
                        "echo captured extension variable \\${{ extension_variable_1 }} = \"${{ extension_variable_1 }}\"")
                .expectedString("echo captured extension variable \\${{ extension_variable_1 }} = \"extension.bar\""));

        list.add(
                new TestData()
                        .variable("test_scripts_directory", "$PIPELINER_HOME/tests/scripts")
                        .inputString(
                                "${{ test_scripts_directory }}/test-arguments-are-equal.sh \"$PWD\" \"tests\" \"\\${{ should.not.be.replaced }}\"")
                        .expectedString(
                                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$PWD\" \"tests\" \"\\${{ should.not.be.replaced }}\""));

        list.add(new TestData()
                .variable("test_scripts_directory", "$PIPELINER_HOME/tests/scripts")
                .variable("a", "$B")
                .variable("b", "$C")
                .inputString("${{ test_scripts_directory }}/test-arguments-are-equal.sh \"$PWD\" ${{ a }} ${{ b }}")
                .expectedString("$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$PWD\" $B $C"));

        list.add(
                new TestData()
                        .variable("test_scripts_directory", "$PIPELINER_HOME/tests/scripts")
                        .inputString(
                                "${{ test_scripts_directory }}/test-arguments-are-equal.sh \"${{ test_scripts_directory }}\" \"${{ test_scripts_directory }}\"")
                        .expectedString(
                                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$PIPELINER_HOME/tests/scripts\" \"$PIPELINER_HOME/tests/scripts\""));
        list.add(
                new TestData()
                        .variable(
                                "hello_world_job__hello_world_step__variable_1",
                                "${{ hello_world_job__variable_1 }}_step.foo")
                        .variable(
                                "hello_world_job__hello_world_step__variable_2",
                                "${{ hello_world_job__variable_2 }}_step.bar")
                        .variable("hello_world_job__variable_1", "${{ hello_world_pipeline__variable_1 }}_job.foo")
                        .variable("hello_world_job__variable_2", "${{ hello_world_pipeline__variable_2 }}_job.bar")
                        .variable(
                                "hello_world_pipeline__hello_world_job__hello_world_step__variable_1",
                                "${{ hello_world_job__variable_1 }}_step.foo")
                        .variable(
                                "hello_world_pipeline__hello_world_job__hello_world_step__variable_2",
                                "${{ hello_world_job__variable_2 }}_step.bar")
                        .variable(
                                "hello_world_pipeline__hello_world_job__variable_1",
                                "${{ hello_world_pipeline_variable_1  }}_job.foo")
                        .variable(
                                "hello_world_pipeline__hello_world_job__variable_2",
                                "${{ hello_world_pipeline__variable_2 }}_job.bar")
                        .variable("hello_world_pipeline__variable_1", "pipeline.foo")
                        .variable("hello_world_pipeline__variable_2", "pipeline.bar")
                        .variable("hello_world_step__variable_1", "${{ hello_world_job__variable_1 }}_step.foo")
                        .variable("hello_world_step__variable_2", "${{ hello_world_job__variable_2 }}_step.bar")
                        .variable("variable_1", "${{ hello_world_job__variable_1 }}_step.foo")
                        .variable("variable_2", "${{ hello_world_job__variable_2 }}_step.bar")
                        .inputString(
                                "echo pipeline scoped properties - ${{ hello_world_pipeline__hello_world_job__hello_world_step__variable_1 }} ${{ hello_world_pipeline__hello_world_job__hello_world_step__variable_2 }}")
                        .expectedString(
                                "echo pipeline scoped properties - pipeline.foo_job.foo_step.foo pipeline.bar_job.bar_step.bar"));

        return list.stream();
    }

    /** Class to implement TestData */
    public static class TestData {

        private final Map<String, String> properties;
        private String inputString;
        private String expectedString;

        /** Constructor */
        public TestData() {
            properties = new TreeMap<>();
        }

        /**
         * Method to set a property
         *
         * @param name the variable name
         * @param value the variable value
         * @return TestData
         */
        public TestData variable(String name, String value) {
            properties.put(name.toUpperCase(Locale.ROOT), value);
            return this;
        }

        /**
         * Method to get properties
         *
         * @return properties
         */
        public Map<String, String> properties() {
            return properties;
        }

        /**
         * Method to set the input string
         *
         * @param inputString inputString
         * @return TestData
         */
        public TestData inputString(String inputString) {
            this.inputString = inputString;
            return this;
        }

        /**
         * Method to get the input string
         *
         * @return string
         */
        public String inputString() {
            return inputString;
        }

        /**
         * Method to set an expected string
         *
         * @param expectedString expectedString
         * @return TestData
         */
        public TestData expectedString(String expectedString) {
            this.expectedString = expectedString;
            return this;
        }

        /**
         * Method to get an expected string
         *
         * @return expectedString
         */
        public String expectedString() {
            return expectedString;
        }
    }
}
