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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Class to implement TokenizerTest
 *
 * Then Tokenizer class will tread anything within single quotes as a TEXT token,
 * but the Resolver class will replace any properties withing single quoted tokens
 * regardless of the single quotes.
 *
 * Environment variables withing single quotes are not replaced by the Resolver class.
 */
public class TokenizerTest {

    @ParameterizedTest
    @MethodSource("getTestData")
    public void testTokenizer(TestData testData) throws TokenizerException {
        List<Token> tokens = Tokenizer.tokenize(testData.getInput());

        assertThat(tokens).isEqualTo(testData.getExpectedTokens());
    }

    /**
     * Method to get the test data
     *
     * @return the test data
     */
    public static Stream<TestData> getTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData().input("echo    ").addExpectedToken(new Token(Token.Type.TEXT, "echo    ", "echo    ")));

        list.add(new TestData()
                .input(" echo    ")
                .addExpectedToken(new Token(Token.Type.TEXT, " echo    ", " echo    ")));

        list.add(new TestData().input("   echo").addExpectedToken(new Token(Token.Type.TEXT, "   echo", "   echo")));

        list.add(new TestData()
                .input("echo \\${{foo}}")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo \\${{foo}}", "echo \\${{foo}}")));

        list.add(new TestData()
                .input("${{ property.1 }}")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1")));

        list.add(new TestData()
                .input("${{ property.1 }} ${{ property.2 }}")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1"))
                .addExpectedToken(new Token(Token.Type.TEXT, " ", " "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.2 }}", "property.2")));

        list.add(new TestData()
                .input("echo ${{ property.1 }} ${{ property.2 }}")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo ", "echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1"))
                .addExpectedToken(new Token(Token.Type.TEXT, " ", " "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.2 }}", "property.2")));

        list.add(new TestData()
                .input("${{ property.1 }} echo ${{ property.2 }}")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo ", " echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.2 }}", "property.2")));

        list.add(new TestData()
                .input("${{ property.1 }} ${{ property.2 }} echo")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1"))
                .addExpectedToken(new Token(Token.Type.TEXT, " ", " "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.2 }}", "property.2"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo", " echo")));

        list.add(new TestData()
                .input("echo ${{ property.1 }} echo ${{ property.2 }} echo")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo ", "echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo ", " echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.2 }}", "property.2"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo", " echo")));

        list.add(new TestData()
                .input("echo \\${{ property.1 }} echo ${{ property.2 }} echo")
                .addExpectedToken(
                        new Token(Token.Type.TEXT, "echo \\${{ property.1 }} echo ", "echo \\${{ property.1 }} echo "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.2 }}", "property.2"))
                .addExpectedToken(new Token(Token.Type.TEXT, " echo", " echo")));

        list.add(new TestData()
                .input("\\${{foo}}${{ property.1 }}")
                .addExpectedToken(new Token(Token.Type.TEXT, "\\${{foo}}", "\\${{foo}}"))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1")));

        list.add(new TestData()
                .input("\\${{foo}}${{ property.1 }}\\${{bar}}")
                .addExpectedToken(new Token(Token.Type.TEXT, "\\${{foo}}", "\\${{foo}}"))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1"))
                .addExpectedToken(new Token(Token.Type.TEXT, "\\${{bar}}", "\\${{bar}}")));

        list.add(new TestData()
                .input(
                        "${{ test.scripts.directory }}/test-arguments-are-equal.sh \"${{ test.scripts.directory }}\" \"${{ test.scripts.directory }}\"")
                .addExpectedToken(
                        new Token(Token.Type.PROPERTY, "${{ test.scripts.directory }}", "test.scripts.directory"))
                .addExpectedToken(new Token(
                        Token.Type.TEXT, "/test-arguments-are-equal.sh \"", "/test-arguments-are-equal.sh \""))
                .addExpectedToken(
                        new Token(Token.Type.PROPERTY, "${{ test.scripts.directory }}", "test.scripts.directory"))
                .addExpectedToken(new Token(Token.Type.TEXT, "\" \"", "\" \""))
                .addExpectedToken(
                        new Token(Token.Type.PROPERTY, "${{ test.scripts.directory }}", "test.scripts.directory"))
                .addExpectedToken(new Token(Token.Type.TEXT, "\"", "\"")));

        list.add(new TestData()
                .input("${{ test.scripts.directory }}_foo")
                .addExpectedToken(
                        new Token(Token.Type.PROPERTY, "${{ test.scripts.directory }}", "test.scripts.directory"))
                .addExpectedToken(new Token(Token.Type.TEXT, "_foo", "_foo")));

        list.add(new TestData()
                .input("${{foo}}${{  bar  }}")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{foo}}", "foo"))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{  bar  }}", "bar")));

        list.add(new TestData()
                .input("echo \\\"${{ pipeline-id.test.property }}\\\"")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo \\\"", "echo \\\""))
                .addExpectedToken(
                        new Token(Token.Type.PROPERTY, "${{ pipeline-id.test.property }}", "pipeline-id.test.property"))
                .addExpectedToken(new Token(Token.Type.TEXT, "\\\"", "\\\"")));

        list.add(new TestData()
                .input("_Multiple_\\$_\\${\\${{Combinations}}_")
                .addExpectedToken(new Token(
                        Token.Type.TEXT,
                        "_Multiple_\\$_\\${\\${{Combinations}}_",
                        "_Multiple_\\$_\\${\\${{Combinations}}_")));

        list.add(new TestData()
                .input("Mix\\${String\\\"With\\${{Underscores}}_")
                .addExpectedToken(new Token(
                        Token.Type.TEXT,
                        "Mix\\${String\\\"With\\${{Underscores}}_",
                        "Mix\\${String\\\"With\\${{Underscores}}_")));

        list.add(new TestData().input("'$FOO'").addExpectedToken(new Token(Token.Type.TEXT, "'$FOO'", "'$FOO'")));

        list.add(new TestData().input("'$FOO").addExpectedToken(new Token(Token.Type.TEXT, "'$FOO", "'$FOO")));

        list.add(new TestData()
                .input("$FOO'")
                .addExpectedToken(new Token(Token.Type.ENVIRONMENT_VARIABLE, "$FOO", "FOO"))
                .addExpectedToken(new Token(Token.Type.TEXT, "'", "'")));

        list.add(new TestData().input("\\").addExpectedToken(new Token(Token.Type.TEXT, "\\", "\\")));

        list.add(new TestData().input("\\\\").addExpectedToken(new Token(Token.Type.TEXT, "\\\\", "\\\\")));

        list.add(new TestData().input("\\$").addExpectedToken(new Token(Token.Type.TEXT, "\\$", "\\$")));

        list.add(new TestData().input("\\${{").addExpectedToken(new Token(Token.Type.TEXT, "\\${{", "\\${{")));

        list.add(new TestData().input("\"").addExpectedToken(new Token(Token.Type.TEXT, "\"", "\"")));

        list.add(new TestData().input("'").addExpectedToken(new Token(Token.Type.TEXT, "'", "'")));

        list.add(new TestData().input("''").addExpectedToken(new Token(Token.Type.TEXT, "''", "''")));

        list.add(new TestData().input("'''").addExpectedToken(new Token(Token.Type.TEXT, "'''", "'''")));

        list.add(new TestData().input("'$'").addExpectedToken(new Token(Token.Type.TEXT, "'$'", "'$'")));

        list.add(new TestData().input("'$$'").addExpectedToken(new Token(Token.Type.TEXT, "'$$'", "'$$'")));

        list.add(new TestData().input("'$$$'").addExpectedToken(new Token(Token.Type.TEXT, "'$$$'", "'$$$'")));

        list.add(new TestData().input("'$\\").addExpectedToken(new Token(Token.Type.TEXT, "'$\\", "'$\\")));

        list.add(new TestData().input("'$$'").addExpectedToken(new Token(Token.Type.TEXT, "'$$'", "'$$'")));

        list.add(new TestData().input("'$$$'").addExpectedToken(new Token(Token.Type.TEXT, "'$$$'", "'$$$'")));

        list.add(new TestData()
                .input("echo '${{ property.1 }}'")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo '${{ property.1 }}'", "echo '${{ property.1 }}'")));

        list.add(new TestData()
                .input("echo '\\${{ property.1 }}'")
                .addExpectedToken(
                        new Token(Token.Type.TEXT, "echo '\\${{ property.1 }}'", "echo '\\${{ property.1 }}'")));

        list.add(new TestData()
                .input("echo '\\$FOO'")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo '\\$FOO'", "echo '\\$FOO'")));

        list.add(new TestData()
                .input("echo '$ FOO'")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo '$ FOO'", "echo '$ FOO'")));

        list.add(new TestData()
                .input("echo '$$ FOO'")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo '$$ FOO'", "echo '$$ FOO'")));

        list.add(new TestData()
                .input("echo '$$$ FOO'")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo '$$$ FOO'", "echo '$$$ FOO'")));

        list.add(new TestData()
                .input("${{ property.1 }}\\$")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1"))
                .addExpectedToken(new Token(Token.Type.TEXT, "\\$", "\\$")));

        list.add(new TestData()
                .input("${{ property.1 }}\\")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1"))
                .addExpectedToken(new Token(Token.Type.TEXT, "\\", "\\")));

        list.add(new TestData()
                .input("\\${{ property.1 }}\\${{ property.2 }}")
                .addExpectedToken(new Token(
                        Token.Type.TEXT,
                        "\\${{ property.1 }}\\${{ property.2 }}",
                        "\\${{ property.1 }}\\${{ property.2 }}")));

        list.add(new TestData()
                .input("ps aux | awk '{print $1, $3}' > output.txt")
                .addExpectedToken(new Token(
                        Token.Type.TEXT,
                        "ps aux | awk '{print $1, $3}' > output.txt",
                        "ps aux | awk '{print $1, $3}' > output.txt")));

        list.add(new TestData()
                .input("echo \\\"${{ test.property }}\\\" \'${{ test.property }}\'")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo \\\"", "echo \\\""))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ test.property }}", "test.property"))
                .addExpectedToken(
                        new Token(Token.Type.TEXT, "\\\" '${{ test.property }}'", "\\\" '${{ test.property }}'")));

        return list.stream();
    }

    @Test
    public void testTokenizerException() {
        String input = "echo ${{";

        assertThatExceptionOfType(TokenizerException.class)
                .isThrownBy(() -> Tokenizer.tokenize(input))
                .withMessage("syntax error in string [" + input + "] at position [6]");
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
         * @param input inputString
         * @return the TestData
         */
        public TestData input(String input) {
            this.inputString = input;
            return this;
        }

        public String getInput() {
            return inputString;
        }

        public TestData addExpectedToken(Token token) {
            this.expectedTokens.add(token);
            return this;
        }

        public List<Token> getExpectedTokens() {
            return expectedTokens;
        }
    }
}
