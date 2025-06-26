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

package org.verifyica.pipeliner.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.verifyica.pipeliner.Console;
import org.verifyica.pipeliner.Constants;

public class ContextTest {

    @Test
    public void testResolveVariables1() {
        Context context = createContext();

        context.getVariables().put("user", "test-user");

        String input = "$PIPELINER_HOME/src ${{ user }}";
        String expected = "$PIPELINER_HOME/src test-user";
        String actual = context.resolveVariables(input);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testResolveVariables2() {
        Context context = createContext();

        context.getVariables().put("user.name", "test-user");

        String input = "$PIPELINER_HOME/src ${{ user.name }}";
        String expected = "$PIPELINER_HOME/src test-user";
        String actual = context.resolveVariables(input);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testResolveAllVariables() {
        Context context = createContext();

        context.getVariables().put("user", "test-user");

        String input = "$PIPELINER_HOME/src ${{ user }}";
        String expected = "/home/verifyica/pipeliner/src test-user";
        String actual = context.resolveAllVariables(input);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testUnresolvedVariables() {
        Context context = createContext();

        String input = "${{ undefined }}";
        String expected = "";
        String actual = context.resolveVariables(input);

        assertThat(actual).isEqualTo(expected);
    }

    private static Context createContext() {
        Console console = new Console();
        Context context = new Context(console);

        context.getEnvironmentVariables().put(Constants.PIPELINER_HOME, "/home/verifyica/pipeliner");
        context.getEnvironmentVariables().put(Constants.PIPELINER_TMP, "/tmp");
        context.getEnvironmentVariables().put(Constants.PIPELINER, "/home/verifyica/pipeliner/pipeliner");

        return context;
    }
}
