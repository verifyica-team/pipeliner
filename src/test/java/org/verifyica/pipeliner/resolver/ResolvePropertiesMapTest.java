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
import org.verifyica.pipeliner.MapBuilder;
import org.verifyica.pipeliner.execution.support.Resolver;
import org.verifyica.pipeliner.execution.support.UnresolvedException;
import org.verifyica.pipeliner.model.Property;
import org.verifyica.pipeliner.parser.SyntaxException;

/** Class to implement ResolvePropertiesMapTest */
public class ResolvePropertiesMapTest {

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

        assertThat(properties).isEqualTo(testData.expectedProperties());
    }

    public static Stream<TestData> getTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData()
                .properties(MapBuilder.builder().put("foo", "$BAR").build())
                .expectedProperties(MapBuilder.builder().put("foo", "$BAR").build()));

        list.add(new TestData()
                .properties(MapBuilder.builder()
                        .put("foo", "${{ bar }}")
                        .put("bar", "bar")
                        .build())
                .expectedProperties(
                        MapBuilder.builder().put("foo", "bar").put("bar", "bar").build()));

        list.add(new TestData()
                .properties(MapBuilder.builder()
                        .put("foo", "${{ bar }}")
                        .put("bar", "${{ foo.bar }}")
                        .put("foo.bar", "foo_bar")
                        .build())
                .expectedProperties(MapBuilder.builder()
                        .put("foo", "foo_bar")
                        .put("bar", "foo_bar")
                        .put("foo.bar", "foo_bar")
                        .build()));

        list.add(new TestData()
                .properties(MapBuilder.builder()
                        .put("foo", "${{ bar }}")
                        .put("bar", "${{ foo.bar }}")
                        .put("foo.bar", "${{ bar_foo }}")
                        .put("bar_foo", "bar_foo")
                        .build())
                .expectedProperties(MapBuilder.builder()
                        .put("foo", "bar_foo")
                        .put("bar", "bar_foo")
                        .put("foo.bar", "bar_foo")
                        .put("bar_foo", "bar_foo")
                        .build()));

        for (String scopeSeparator : Property.SCOPE_SEPARATORS) {
            list.add(new TestData()
                    .properties(MapBuilder.builder()
                            .put("foo", "bar")
                            .put("foo" + scopeSeparator + "2", "${{ foo }}")
                            .put("foo" + scopeSeparator + "3", "${{ foo" + scopeSeparator + "2 }}")
                            .build())
                    .expectedProperties(MapBuilder.builder()
                            .put("foo", "bar")
                            .put("foo" + scopeSeparator + "2", "bar")
                            .put("foo" + scopeSeparator + "3", "bar")
                            .build()));
        }

        return list.stream();
    }

    public static class TestData {

        private Map<String, String> properties;
        private Map<String, String> expectedProperties;

        public TestData() {
            properties = new TreeMap<>();
            expectedProperties = new TreeMap<>();
        }

        public TestData properties(Map<String, String> properties) {
            this.properties = new TreeMap<>(properties);
            return this;
        }

        public Map<String, String> properties() {
            return properties;
        }

        public TestData expectedProperties(Map<String, String> expectedProperties) {
            this.expectedProperties = new TreeMap<>(expectedProperties);
            return this;
        }

        public Map<String, String> expectedProperties() {
            return expectedProperties;
        }
    }
}
