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

package org.verifyica.pipeliner.execution.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

public class ResolverTest {

    @Test
    public void testResolve1() throws ResolverException {
        Map<String, String> environmentVariables = new TreeMap<>();
        environmentVariables.put("PROPERTY_1", "${{ property.1 }}");
        environmentVariables.put("PROPERTY_2", "${{ property.2 }}");

        Map<String, String> properties = new TreeMap<>();
        properties.put("property.1", "${{ property.3 }}");
        properties.put("property.2", "foo");
        properties.put("property.3", "$PROPERTY_2");

        String command = "echo $PROPERTY_1 ${{ property.2 }}";
        String expectedCommand = "echo $PROPERTY_1 foo";
        String actualCommand = Resolver.resolveProperties(properties, command);

        System.out.printf("command         [%s]%n", command);
        System.out.printf("expectedCommand [%s]%n", expectedCommand);
        System.out.printf("actualCommand   [%s]%n", actualCommand);

        assertThat(actualCommand).isEqualTo(expectedCommand);
    }

    @Test
    public void testResolve2() throws ResolverException {
        Map<String, String> environmentVariables = new TreeMap<>();
        environmentVariables.put("PROPERTY_1", "${{ property.1 }}");
        environmentVariables.put("PROPERTY_2", "${{ property.2 }}");

        Map<String, String> properties = new TreeMap<>();
        properties.put("property.1", "${{ property.3 }}");
        properties.put("property.2", "foo");
        properties.put("property.3", "$PROPERTY_2");

        String command = "echo $PROPERTY_1 ${{property.2}}";
        String expectedCommand = "echo $PROPERTY_1 foo";
        String actualCommand = Resolver.resolveProperties(properties, command);

        System.out.printf("command         [%s]%n", command);
        System.out.printf("expectedCommand [%s]%n", expectedCommand);
        System.out.printf("actualCommand   [%s]%n", actualCommand);

        assertThat(actualCommand).isEqualTo(expectedCommand);
    }

    @Test
    public void testResolve3() throws ResolverException {
        Map<String, String> environmentVariables = new TreeMap<>();
        environmentVariables.put("PWD", "/home/user/Development/github/verifyica-team/pipeliner/tests");
        environmentVariables.put("PIPELINER_HOME", "/home/user/Development/github/verifyica-team/pipeliner");
        environmentVariables.put("PIPELINER", "$PIPELINER_HOME/pipeliner");

        Map<String, String> properties = new TreeMap<>();
        properties.put("test.scripts.directory", "$PIPELINER_HOME/tests/scripts");

        String command = "${{ test.scripts.directory }}/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\"";
        String expectedCommand =
                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\"";

        String actualCommand = Resolver.resolveProperties(properties, command);

        System.out.printf("command         [%s]%n", command);
        System.out.printf("expectedCommand [%s]%n", expectedCommand);
        System.out.printf("actualCommand   [%s]%n", actualCommand);

        assertThat(actualCommand).isEqualTo(expectedCommand);
    }
}
