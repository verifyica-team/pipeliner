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

package org.verifyica.pipeliner.tokenizer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class TokenizerTest {

    @ParameterizedTest
    @MethodSource("testData")
    public void testTokenizer(TestData testData) throws TokenizerException {
        List<Token> tokens = Tokenizer.tokenize(testData.intputString());

        assertThat(tokens).isEqualTo(testData.expectedTokens());
    }

    public static Stream<TestData> testData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData().inputString("echo    ").addExpectedToken(new Token(Token.Type.TEXT, "echo    ")));

        list.add(new TestData().inputString(" echo    ").addExpectedToken(new Token(Token.Type.TEXT, " echo    ")));

        list.add(new TestData().inputString("   echo").addExpectedToken(new Token(Token.Type.TEXT, "   echo")));

        list.add(new TestData()
                .inputString("echo \\${{foo}}")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo \\${{foo}}")));

        list.add(new TestData()
                .inputString("${{ property.1 }}")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}")));

        list.add(new TestData()
                .inputString("${{ property.1 }} ${{ property.2 }}")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.2 }}")));

        list.add(new TestData()
                .inputString("echo ${{ property.1 }} ${{ property.2 }}")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.2 }}")));

        list.add(new TestData()
                .inputString("${{ property.1 }} echo ${{ property.2 }}")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.2 }}")));

        list.add(new TestData()
                .inputString("${{ property.1 }} ${{ property.2 }} echo")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.2 }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo")));

        list.add(new TestData()
                .inputString("echo ${{ property.1 }} echo ${{ property.2 }} echo")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.2 }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo")));

        list.add(new TestData()
                .inputString("echo \\${{ property.1 }} echo ${{ property.2 }} echo")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo \\${{ property.1 }} echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.2 }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo")));

        list.add(new TestData()
                .inputString("\\${{foo}}${{ property.1 }}")
                .addExpectedToken(new Token(Token.Type.TEXT, "\\${{foo}}"))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}")));

        list.add(new TestData()
                .inputString("\\${{foo}}${{ property.1 }}\\${{bar}}")
                .addExpectedToken(new Token(Token.Type.TEXT, "\\${{foo}}"))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, "\\${{bar}}")));

        list.add(new TestData()
                .inputString(
                        "${{ test.scripts.directory }}/test-arguments-are-equal.sh \"${{ test.scripts.directory }}\" \"${{ test.scripts.directory }}\"")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ test.scripts.directory }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, "/test-arguments-are-equal.sh \""))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ test.scripts.directory }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, "\" \""))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ test.scripts.directory }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, "\"")));

        list.add(new TestData()
                .inputString("${{ test.scripts.directory }}_foo")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ test.scripts.directory }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, "_foo")));

        list.add(new TestData()
                .inputString("${{foo}}${{  bar  }}")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{foo}}"))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{  bar  }}")));

        list.add(new TestData()
                .inputString("echo \\\"${{ pipeline-id.test.property }}\\\"")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo \\\""))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ pipeline-id.test.property }}"))
                .addExpectedToken(new Token(Token.Type.TEXT, "\\\"")));

        return list.stream();
    }

    /** Class to implement TestData */
    public static class TestData {

        private String inputString;
        private final List<Token> expectedTokens;

        /** Constructor */
        public TestData() {
            expectedTokens = new ArrayList<>();
        }

        /**
         * Method to set the input string
         *
         * @param inputString inputString
         * @return the TestData
         */
        public TestData inputString(String inputString) {
            this.inputString = inputString;
            return this;
        }

        public String intputString() {
            return inputString;
        }

        public TestData addExpectedToken(Token token) {
            this.expectedTokens.add(token);
            return this;
        }

        public List<Token> expectedTokens() {
            return expectedTokens;
        }
    }
}
