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

package org.verifyica.pipeliner.execution.support.parser;

import java.util.ArrayList;
import java.util.List;

public class TestData {

    private String string;
    private final List<Token> expectedTokens;

    public TestData() {
        expectedTokens = new ArrayList<>();
    }

    public TestData setString(String string) {
        this.string = string;
        return this;
    }

    public String getString() {
        return string;
    }

    public TestData addExpectedToken(Token token) {
        this.expectedTokens.add(token);
        return this;
    }

    public List<Token> getExpectedTokens() {
        return expectedTokens;
    }
}
