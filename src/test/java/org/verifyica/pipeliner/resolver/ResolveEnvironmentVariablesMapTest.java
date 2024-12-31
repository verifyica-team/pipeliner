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
import org.verifyica.pipeliner.tokenizer.TokenizerException;

public class ResolveEnvironmentVariablesMapTest {

    @ParameterizedTest
    @MethodSource("testData")
    public void testResolver(TestData testData) throws TokenizerException, ResolverException {
        Map<String, String> environmentVariables =
                Resolver.resolveEnvironmentVariables(testData.environmentVariables(), testData.properties());

        assertThat(environmentVariables).isEqualTo(testData.expectedEnvironmentVariables());
    }

    public static Stream<TestData> testData() {
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

    public static class TestData {

        private final Map<String, String> environmentVariables;
        private final Map<String, String> properties;
        private final Map<String, String> expectedEnvironmentVariables;

        public TestData() {
            environmentVariables = new TreeMap<>();
            properties = new TreeMap<>();
            expectedEnvironmentVariables = new TreeMap<>();
        }

        public TestData environmentVariable(String name, String value) {
            environmentVariables.put(name, value);
            return this;
        }

        public Map<String, String> environmentVariables() {
            return environmentVariables;
        }

        public TestData property(String name, String value) {
            properties.put(name, value);
            return this;
        }

        public Map<String, String> properties() {
            return properties;
        }

        public TestData expectedEnvironmentVariable(String name, String value) {
            expectedEnvironmentVariables.put(name, value);
            return this;
        }

        public Map<String, String> expectedEnvironmentVariables() {
            return expectedEnvironmentVariables;
        }
    }
}
