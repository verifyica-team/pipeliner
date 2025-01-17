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
import org.verifyica.pipeliner.execution.support.Resolver;
import org.verifyica.pipeliner.execution.support.UnresolvedException;
import org.verifyica.pipeliner.lexer.SyntaxException;

/** Class to implement ResolvePropertiesTest */
public class ResolvePropertiesTest {

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
        Map<String, String> properties = Resolver.resolveProperties(testData.properties());

        String string = Resolver.replaceProperties(properties, testData.inputString());

        assertThat(string).isEqualTo(testData.expectedString());
    }

    public static Stream<TestData> getTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData()
                .property("property.1", "${{ property.3 }}")
                .property("property.2", "foo")
                .property("property.3", "$PROPERTY_2")
                .inputString("echo $PROPERTY_1 ${{ property.2 }}")
                .expectedString("echo $PROPERTY_1 foo"));

        list.add(new TestData()
                .property("property.1", "${{ property.3 }}")
                .property("property.2", "foo")
                .property("property.3", "$PROPERTY_2")
                .inputString("echo $PROPERTY_1 ${{property.2}}")
                .expectedString("echo $PROPERTY_1 foo"));

        list.add(new TestData()
                .property("test.scripts.directory", "$PIPELINER_HOME/tests/scripts")
                .inputString("${{ test.scripts.directory }}/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\"")
                .expectedString(
                        "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\""));

        list.add(
                new TestData()
                        .property("test.scripts.directory", "$PIPELINER_HOME/tests/scripts")
                        .inputString(
                                "${{ test.scripts.directory }}/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\" \\${{ should.not.be.replaced }}")
                        .expectedString(
                                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\" \\${{ should.not.be.replaced }}"));

        list.add(new TestData()
                .property("extension.property.1", "extension.bar")
                .inputString(
                        "echo captured extension property \\${{ extension.property.1 }} = \"${{ extension.property.1 }}\"")
                .expectedString("echo captured extension property \\${{ extension.property.1 }} = \"extension.bar\""));

        list.add(
                new TestData()
                        .property("test.scripts.directory", "$PIPELINER_HOME/tests/scripts")
                        .inputString(
                                "${{ test.scripts.directory }}/test-arguments-are-equal.sh \"$PWD\" \"tests\" \"\\${{ should.not.be.replaced }}\"")
                        .expectedString(
                                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$PWD\" \"tests\" \"\\${{ should.not.be.replaced }}\""));

        list.add(new TestData()
                .property("test.scripts.directory", "$PIPELINER_HOME/tests/scripts")
                .property("a", "$B")
                .property("b", "$C")
                .inputString("${{ test.scripts.directory }}/test-arguments-are-equal.sh \"$PWD\" ${{ a }} ${{ b }}")
                .expectedString("$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$PWD\" $B $C"));

        list.add(
                new TestData()
                        .property("test.scripts.directory", "$PIPELINER_HOME/tests/scripts")
                        .inputString(
                                "${{ test.scripts.directory }}/test-arguments-are-equal.sh \"${{ test.scripts.directory }}\" \"${{ test.scripts.directory }}\"")
                        .expectedString(
                                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$PIPELINER_HOME/tests/scripts\" \"$PIPELINER_HOME/tests/scripts\""));

        list.add(
                new TestData()
                        .property(
                                "hello-world-job.hello-world-step.property.1",
                                "${{ hello-world-job.property.1 }}_step.foo")
                        .property(
                                "hello-world-job.hello-world-step.property.2",
                                "${{ hello-world-job.property.2 }}_step.bar")
                        .property("hello-world-job.property.1", "${{ hello-world-pipeline.property.1 }}_job.foo")
                        .property("hello-world-job.property.2", "${{ hello-world-pipeline.property.2 }}_job.bar")
                        .property(
                                "hello-world-pipeline.hello-world-job.hello-world-step.property.1",
                                "${{ hello-world-job.property.1 }}_step.foo")
                        .property(
                                "hello-world-pipeline.hello-world-job.hello-world-step.property.2",
                                "${{ hello-world-job.property.2 }}_step.bar")
                        .property(
                                "hello-world-pipeline.hello-world-job.property.1",
                                "${{ hello-world-pipeline.property.1 }}_job.foo")
                        .property(
                                "hello-world-pipeline.hello-world-job.property.2",
                                "${{ hello-world-pipeline.property.2 }}_job.bar")
                        .property("hello-world-pipeline.property.1", "pipeline.foo")
                        .property("hello-world-pipeline.property.2", "pipeline.bar")
                        .property("hello-world-step.property.1", "${{ hello-world-job.property.1 }}_step.foo")
                        .property("hello-world-step.property.2", "${{ hello-world-job.property.2 }}_step.bar")
                        .property("property.1", "${{ hello-world-job.property.1 }}_step.foo")
                        .property("property.2", "${{ hello-world-job.property.2 }}_step.bar")
                        .inputString(
                                "echo pipeline scoped properties - ${{ hello-world-pipeline.hello-world-job.hello-world-step.property.1 }} ${{ hello-world-pipeline.hello-world-job.hello-world-step.property.2 }}")
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
         * @param name the property name
         * @param value the property value
         * @return TestData
         */
        public TestData property(String name, String value) {
            properties.put(name, value);
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
