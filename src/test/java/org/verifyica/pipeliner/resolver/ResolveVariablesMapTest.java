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
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.MapBuilder;
import org.verifyica.pipeliner.execution.support.Resolver;
import org.verifyica.pipeliner.execution.support.UnresolvedException;
import org.verifyica.pipeliner.parser.SyntaxException;

/** Class to implement ResolveVariablesMapTest */
public class ResolveVariablesMapTest {

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

        assertThat(properties).isEqualTo(testData.expectedVariables());
    }

    public static Stream<TestData> getTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData()
                .variables(MapBuilder.builder().put("foo", "$BAR").build())
                .expectedVariables(MapBuilder.builder().put("foo", "$BAR").build()));

        list.add(new TestData()
                .variables(MapBuilder.builder()
                        .put("foo", "${{ bar }}")
                        .put("bar", "bar")
                        .build())
                .expectedVariables(
                        MapBuilder.builder().put("foo", "bar").put("bar", "bar").build()));

        list.add(new TestData()
                .variables(MapBuilder.builder()
                        .put("foo", "${{ bar }}")
                        .put("bar", "${{ foo_bar }}")
                        .put("foo_bar", "foo_bar")
                        .build())
                .expectedVariables(MapBuilder.builder()
                        .put("foo", "foo_bar")
                        .put("bar", "foo_bar")
                        .put("foo_bar", "foo_bar")
                        .build()));

        list.add(new TestData()
                .variables(MapBuilder.builder()
                        .put("foo", "${{ bar }}")
                        .put("bar", "${{ foo_bar }}")
                        .put("foo_bar", "${{ bar_foo }}")
                        .put("bar_foo", "bar_foo")
                        .build())
                .expectedVariables(MapBuilder.builder()
                        .put("foo", "bar_foo")
                        .put("bar", "bar_foo")
                        .put("foo_bar", "bar_foo")
                        .put("bar_foo", "bar_foo")
                        .build()));

        list.add(new TestData()
                .variables(MapBuilder.builder()
                        .put("foo", "bar")
                        .put("foo" + Constants.SCOPE_SEPARATOR + "_2", "${{ foo" + Constants.SCOPE_SEPARATOR + "_3 }}")
                        .put("foo" + Constants.SCOPE_SEPARATOR + "_3", "${{ foo }}")
                        .build())
                .expectedVariables(MapBuilder.builder()
                        .put("foo", "bar")
                        .put("foo" + Constants.SCOPE_SEPARATOR + "_2", "bar")
                        .put("foo" + Constants.SCOPE_SEPARATOR + "_3", "bar")
                        .build()));

        return list.stream();
    }

    public static class TestData {

        private Map<String, String> variables;
        private Map<String, String> expectedVariables;

        public TestData() {
            variables = new TreeMap<>();
            expectedVariables = new TreeMap<>();
        }

        public TestData variables(Map<String, String> properties) {
            this.variables = new TreeMap<>(properties);
            return this;
        }

        public Map<String, String> variables() {
            return variables;
        }

        public TestData expectedVariables(Map<String, String> variables) {
            this.expectedVariables = new TreeMap<>(variables);
            return this;
        }

        public Map<String, String> expectedVariables() {
            return expectedVariables;
        }
    }
}
