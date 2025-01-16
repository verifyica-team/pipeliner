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
import org.verifyica.pipeliner.execution.support.ResolverException;
import org.verifyica.pipeliner.parser.ParserException;

/** Class to implement ResolveEnvironmentVariablesMapTest */
public class ResolveEnvironmentVariablesMapTest {

    @ParameterizedTest
    @MethodSource("getTestData")
    public void testResolver(TestData testData) throws ParserException, ResolverException {
        Map<String, String> environmentVariables =
                Resolver.resolveEnvironmentVariables(testData.environmentVariables(), testData.properties());

        assertThat(environmentVariables).isEqualTo(testData.expectedEnvironmentVariables());
    }

    /**
     * Method to get test data
     *
     * @return test data
     */
    public static Stream<TestData> getTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData()
                .environmentVariable("FOO", "${{ foo }}")
                .environmentVariable("BAR", "bar")
                .property("foo", "$BAR")
                .expectedEnvironmentVariable("FOO", "bar")
                .expectedEnvironmentVariable("BAR", "bar"));

        list.add(new TestData()
                .environmentVariable("FOO", "${{ foo }}")
                .environmentVariable("BAR", "bar")
                .property("foo", "${BAR}")
                .expectedEnvironmentVariable("FOO", "bar")
                .expectedEnvironmentVariable("BAR", "bar"));

        list.add(new TestData()
                .environmentVariable("FOO", "${{ foo }}")
                .environmentVariable("BAR", "bar")
                .property("foo", "${{ foo.2 }}")
                .property("foo.2", "bar")
                .expectedEnvironmentVariable("FOO", "bar")
                .expectedEnvironmentVariable("BAR", "bar"));

        list.add(new TestData()
                .environmentVariable("FOO", "${{ foo }}")
                .environmentVariable("BAR", "${FOO_BAR}")
                .environmentVariable("FOO_BAR", "${{foo}}")
                .property("foo", "${{ foo.2 }}")
                .property("foo.2", "bar")
                .expectedEnvironmentVariable("FOO", "bar")
                .expectedEnvironmentVariable("BAR", "bar")
                .expectedEnvironmentVariable("FOO_BAR", "bar"));

        return list.stream();
    }

    /** Class to implement TestData */
    public static class TestData {

        private final Map<String, String> environmentVariables;
        private final Map<String, String> properties;
        private final Map<String, String> expectedEnvironmentVariables;

        /** Constructor */
        public TestData() {
            environmentVariables = new TreeMap<>();
            properties = new TreeMap<>();
            expectedEnvironmentVariables = new TreeMap<>();
        }

        /**
         * Method to set an environment variable
         *
         * @param name the environment variable name
         * @param value the environment variable value
         * @return TestData
         */
        public TestData environmentVariable(String name, String value) {
            environmentVariables.put(name, value);
            return this;
        }

        /**
         * Method to get environment variables
         *
         * @return environment variables
         */
        public Map<String, String> environmentVariables() {
            return environmentVariables;
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
         * Method to set an expected environment variable
         *
         * @param name the expected environment variable name
         * @param value the expected environment variable value
         * @return TestData
         */
        public TestData expectedEnvironmentVariable(String name, String value) {
            expectedEnvironmentVariables.put(name, value);
            return this;
        }

        /**
         * Method to get expected environment variables
         *
         * @return expected environment variables
         */
        public Map<String, String> expectedEnvironmentVariables() {
            return expectedEnvironmentVariables;
        }
    }
}
