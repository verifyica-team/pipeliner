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
import org.verifyica.pipeliner.execution.support.parser.PropertyParserException;

public class PropertiesResolverTest {

    @Test
    public void testResolve1() throws ResolverException {
        Map<String, String> properties = new TreeMap<>();
        properties.put("property.1", "${{ property.3 }}");
        properties.put("property.2", "foo");
        properties.put("property.3", "$PROPERTY_2");

        PropertiesResolver.resolveProperties(properties);

        String string = "echo $PROPERTY_1 ${{ property.2 }}";
        String expectedString = "echo $PROPERTY_1 foo";
        String actualString = PropertiesResolver.resolveProperties(properties, string);

        assertThat(actualString).isEqualTo(expectedString);
    }

    @Test
    public void testResolve2() throws ResolverException {
        Map<String, String> properties = new TreeMap<>();
        properties.put("property.1", "${{ property.3 }}");
        properties.put("property.2", "foo");
        properties.put("property.3", "$PROPERTY_2");

        PropertiesResolver.resolveProperties(properties);

        String string = "echo $PROPERTY_1 ${{property.2}}";
        String expectedString = "echo $PROPERTY_1 foo";
        String actualString = PropertiesResolver.resolveProperties(properties, string);

        assertThat(actualString).isEqualTo(expectedString);
    }

    @Test
    public void testResolve3() throws ResolverException {
        Map<String, String> properties = new TreeMap<>();
        properties.put("test.scripts.directory", "$PIPELINER_HOME/tests/scripts");

        PropertiesResolver.resolveProperties(properties);

        String string = "${{ test.scripts.directory }}/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\"";
        String expectedString =
                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\"";
        String actualString = PropertiesResolver.resolveProperties(properties, string);

        assertThat(actualString).isEqualTo(expectedString);
    }

    @Test
    public void testResolve4() throws ResolverException {
        Map<String, String> properties = new TreeMap<>();
        properties.put("test.scripts.directory", "$PIPELINER_HOME/tests/scripts");

        PropertiesResolver.resolveProperties(properties);

        String string =
                "${{ test.scripts.directory }}/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\" \\${{ should.not.be.replaced }}";
        String expectedString =
                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$(basename $PWD)\" \"tests\" \\${{ should.not.be.replaced }}";
        String actualString = PropertiesResolver.resolveProperties(properties, string);

        assertThat(actualString).isEqualTo(expectedString);
    }

    @Test
    public void testResolve5() throws ResolverException {
        Map<String, String> properties = new TreeMap<>();
        properties.put("extension.property.1", "extension.bar");

        PropertiesResolver.resolveProperties(properties);

        String string =
                "echo captured extension property \\${{ extension.property.1 }} = \"${{ extension.property.1 }}\"";
        String expectedString = "echo captured extension property \\${{ extension.property.1 }} = \"extension.bar\"";
        String actualString = PropertiesResolver.resolveProperties(properties, string);

        assertThat(actualString).isEqualTo(expectedString);
    }

    @Test
    public void testResolve6() throws ResolverException {
        Map<String, String> properties = new TreeMap<>();
        properties.put("test.scripts.directory", "$PIPELINER_HOME/tests/scripts");

        PropertiesResolver.resolveProperties(properties);

        String string =
                "${{ test.scripts.directory }}/test-arguments-are-equal.sh \"$PWD\" \"tests\" \"\\${{ should.not.be.replaced }}\"";
        String expectedString =
                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$PWD\" \"tests\" \"\\${{ should.not.be.replaced }}\"";
        String actualString = PropertiesResolver.resolveProperties(properties, string);

        assertThat(actualString).isEqualTo(expectedString);
    }

    @Test
    public void testResolve7() throws ResolverException {
        Map<String, String> properties = new TreeMap<>();
        properties.put("test.scripts.directory", "$PIPELINER_HOME/tests/scripts");
        properties.put("a", "$B");
        properties.put("b", "$C");

        PropertiesResolver.resolveProperties(properties);

        String string = "${{ test.scripts.directory }}/test-arguments-are-equal.sh \"$PWD\" ${{ a }} ${{ b }}";
        String expectedString = "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$PWD\" $B $C";
        String actualString = PropertiesResolver.resolveProperties(properties, string);

        assertThat(actualString).isEqualTo(expectedString);
    }

    @Test
    public void testResolve8() throws ResolverException {
        Map<String, String> properties = new TreeMap<>();
        properties.put("test.scripts.directory", "$PIPELINER_HOME/tests/scripts");

        PropertiesResolver.resolveProperties(properties);

        String string =
                "${{ test.scripts.directory }}/test-arguments-are-equal.sh \"${{ test.scripts.directory }}\" \"${{ test.scripts.directory }}\"";
        String expectedString =
                "$PIPELINER_HOME/tests/scripts/test-arguments-are-equal.sh \"$PIPELINER_HOME/tests/scripts\" \"$PIPELINER_HOME/tests/scripts\"";
        String actualString = PropertiesResolver.resolveProperties(properties, string);

        assertThat(actualString).isEqualTo(expectedString);
    }

    @Test
    public void testResolve9() throws ResolverException, PropertyParserException {
        Map<String, String> properties = new TreeMap<>();
        properties.put("hello-world-job.hello-world-step.property.1", "${{ hello-world-job.property.1 }}_step.foo");
        properties.put("hello-world-job.hello-world-step.property.2", "${{ hello-world-job.property.2 }}_step.bar");
        properties.put("hello-world-job.property.1", "${{ hello-world-pipeline.property.1 }}_job.foo");
        properties.put("hello-world-job.property.2", "${{ hello-world-pipeline.property.2 }}_job.bar");
        properties.put(
                "hello-world-pipeline.hello-world-job.hello-world-step.property.1",
                "${{ hello-world-job.property.1 }}_step.foo");
        properties.put(
                "hello-world-pipeline.hello-world-job.hello-world-step.property.2",
                "${{ hello-world-job.property.2 }}_step.bar");
        properties.put(
                "hello-world-pipeline.hello-world-job.property.1", "${{ hello-world-pipeline.property.1 }}_job.foo");
        properties.put(
                "hello-world-pipeline.hello-world-job.property.2", "${{ hello-world-pipeline.property.2 }}_job.bar");
        properties.put("hello-world-pipeline.property.1", "pipeline.foo");
        properties.put("hello-world-pipeline.property.2", "pipeline.bar");
        properties.put("hello-world-step.property.1", "${{ hello-world-job.property.1 }}_step.foo");
        properties.put("hello-world-step.property.2", "${{ hello-world-job.property.2 }}_step.bar");
        properties.put("property.1", "${{ hello-world-job.property.1 }}_step.foo");
        properties.put("property.2", "${{ hello-world-job.property.2 }}_step.bar");

        PropertiesResolver.resolveProperties(properties);

        String string =
                "echo pipeline scoped properties - ${{ hello-world-pipeline.hello-world-job.hello-world-step.property.1 }} ${{ hello-world-pipeline.hello-world-job.hello-world-step.property.2 }}";
        String expectedString =
                "echo pipeline scoped properties - pipeline.foo_job.foo_step.foo pipeline.bar_job.bar_step.bar";
        String actualString = PropertiesResolver.resolveProperties(properties, string);

        assertThat(actualString).isEqualTo(expectedString);
    }
}
