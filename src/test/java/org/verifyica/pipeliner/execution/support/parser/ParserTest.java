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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ParserTest {

    @ParameterizedTest
    @MethodSource("getTestData")
    public void testPropertyParser(TestData testData) throws ParserException {
        List<Token> tokens = Parser.parse(testData.getString());

        assertThat(tokens).isEqualTo(testData.getExpectedTokens());
    }

    public static Stream<TestData> getTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData().setString("echo    ").addExpectedToken(new Token(Token.Type.TEXT, "echo    ")));

        list.add(new TestData().setString(" echo    ").addExpectedToken(new Token(Token.Type.TEXT, " echo    ")));

        list.add(new TestData().setString("   echo").addExpectedToken(new Token(Token.Type.TEXT, "   echo")));

        list.add(new TestData()
                .setString("echo \\${{foo}}")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo \\${{foo}}")));

        list.add(new TestData()
                .setString("${{ property.1 }}")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.1}}")));

        list.add(new TestData()
                .setString("${{ property.1 }} ${{ property.2 }}")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.1}}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.2}}")));

        list.add(new TestData()
                .setString("echo ${{ property.1 }} ${{ property.2 }}")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.1}}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.2}}")));

        list.add(new TestData()
                .setString("${{ property.1 }} echo ${{ property.2 }}")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.1}}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.2}}")));

        list.add(new TestData()
                .setString("${{ property.1 }} ${{ property.2 }} echo")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.1}}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.2}}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo")));

        list.add(new TestData()
                .setString("echo ${{ property.1 }} echo ${{ property.2 }} echo")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.1}}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.2}}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo")));

        list.add(new TestData()
                .setString("echo \\${{ property.1 }} echo ${{ property.2 }} echo")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo \\${{ property.1 }} echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.2}}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo")));

        list.add(new TestData()
                .setString("\\${{foo}}${{ property.1 }}")
                .addExpectedToken(new Token(Token.Type.TEXT, "\\${{foo}}"))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.1}}")));

        list.add(new TestData()
                .setString("\\${{foo}}${{ property.1 }}\\${{bar}}")
                .addExpectedToken(new Token(Token.Type.TEXT, "\\${{foo}}"))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.1}}"))
                .addExpectedToken(new Token(Token.Type.TEXT, "\\${{bar}}")));

        list.add(new TestData()
                .setString(
                        "${{ test.scripts.directory }}/test-arguments-are-equal.sh \"${{ test.scripts.directory }}\" \"${{ test.scripts.directory }}\"")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{test.scripts.directory}}"))
                .addExpectedToken(new Token(Token.Type.TEXT, "/test-arguments-are-equal.sh \""))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{test.scripts.directory}}"))
                .addExpectedToken(new Token(Token.Type.TEXT, "\" \""))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{test.scripts.directory}}"))
                .addExpectedToken(new Token(Token.Type.TEXT, "\"")));

        list.add(new TestData()
                .setString("${{ test.scripts.directory }}_foo")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{test.scripts.directory}}"))
                .addExpectedToken(new Token(Token.Type.TEXT, "_foo")));

        return list.stream();
    }
}
