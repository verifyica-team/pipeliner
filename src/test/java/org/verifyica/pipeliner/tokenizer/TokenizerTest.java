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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    /**
     * Method to test the tokenizer, validating the token list returned is equal the expected token list
     *
     * @param testData the test data
     * @throws TokenizerException If an error occurs during tokenization
     */
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
                .addExpectedToken(new Token(Token.Type.TEXT, "echo '", "echo '"))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1"))
                .addExpectedToken(new Token(Token.Type.TEXT, "'", "'")));

        list.add(new TestData()
                .input("echo '\\${{ property.1 }}'")
                .addExpectedToken(
                        new Token(Token.Type.TEXT, "echo '\\${{ property.1 }}'", "echo '\\${{ property.1 }}'")));

        list.add(new TestData()
                .input("echo \\${{ property.1 }} ${{ property.2 }}")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo \\${{ property.1 }} ", "echo \\${{ property.1 }} "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.2 }}", "property.2")));

        list.add(new TestData()
                .input("echo '\\$FOO'")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo '\\$FOO'", "echo '\\$FOO'")));

        list.add(new TestData()
                .input("echo \\$FOO")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo \\$FOO", "echo \\$FOO")));

        list.add(new TestData()
                .input("echo \\$FOO $BAR")
                .addExpectedToken(new Token(Token.Type.TEXT, "echo \\$FOO ", "echo \\$FOO "))
                .addExpectedToken(new Token(Token.Type.ENVIRONMENT_VARIABLE, "$BAR", "BAR")));

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
                .addExpectedToken(new Token(Token.Type.TEXT, "\\\" '", "\\\" '"))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ test.property }}", "test.property"))
                .addExpectedToken(new Token(Token.Type.TEXT, "'", "'")));

        list.add(new TestData()
                .input("cat file.txt | tr '[:lower:]' '[:upper:]'")
                .addExpectedToken(new Token(
                        Token.Type.TEXT,
                        "cat file.txt | tr '[:lower:]' '[:upper:]'",
                        "cat file.txt | tr '[:lower:]' '[:upper:]'")));

        list.add(new TestData()
                .input("sed 's/\\${{}}/X/g' file")
                .addExpectedToken(new Token(Token.Type.TEXT, "sed 's/\\${{}}/X/g' file", "sed 's/\\${{}}/X/g' file")));

        list.add(new TestData()
                .input("${{ property.1 }} ${{property.2}} $ENV_VAR_1 ${ENV_VAR_2}")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1"))
                .addExpectedToken(new Token(Token.Type.TEXT, " ", " "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.2}}", "property.2"))
                .addExpectedToken(new Token(Token.Type.TEXT, " ", " "))
                .addExpectedToken(new Token(Token.Type.ENVIRONMENT_VARIABLE, "$ENV_VAR_1", "ENV_VAR_1"))
                .addExpectedToken(new Token(Token.Type.TEXT, " ", " "))
                .addExpectedToken(new Token(Token.Type.ENVIRONMENT_VARIABLE, "${ENV_VAR_2}", "ENV_VAR_2")));

        list.add(new TestData()
                .input("${{ property.1 }} ${{property.2}} $ENV_VAR_1 ${ENV_VAR_2} '$ENV_VAR_3 ${ENV_VAR_4}'")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1"))
                .addExpectedToken(new Token(Token.Type.TEXT, " ", " "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.2}}", "property.2"))
                .addExpectedToken(new Token(Token.Type.TEXT, " ", " "))
                .addExpectedToken(new Token(Token.Type.ENVIRONMENT_VARIABLE, "$ENV_VAR_1", "ENV_VAR_1"))
                .addExpectedToken(new Token(Token.Type.TEXT, " ", " "))
                .addExpectedToken(new Token(Token.Type.ENVIRONMENT_VARIABLE, "${ENV_VAR_2}", "ENV_VAR_2"))
                .addExpectedToken(
                        new Token(Token.Type.TEXT, " '$ENV_VAR_3 ${ENV_VAR_4}'", " '$ENV_VAR_3 ${ENV_VAR_4}'")));

        list.add(new TestData()
                .input(
                        "${{ property.1 }} ${{property.2}} $ENV_VAR_1 ${ENV_VAR_2} '$ENV_VAR_3 ${{ property.3 }} ${ENV_VAR_4}'")
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.1 }}", "property.1"))
                .addExpectedToken(new Token(Token.Type.TEXT, " ", " "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{property.2}}", "property.2"))
                .addExpectedToken(new Token(Token.Type.TEXT, " ", " "))
                .addExpectedToken(new Token(Token.Type.ENVIRONMENT_VARIABLE, "$ENV_VAR_1", "ENV_VAR_1"))
                .addExpectedToken(new Token(Token.Type.TEXT, " ", " "))
                .addExpectedToken(new Token(Token.Type.ENVIRONMENT_VARIABLE, "${ENV_VAR_2}", "ENV_VAR_2"))
                .addExpectedToken(new Token(Token.Type.TEXT, " '$ENV_VAR_3 ", " '$ENV_VAR_3 "))
                .addExpectedToken(new Token(Token.Type.PROPERTY, "${{ property.3 }}", "property.3"))
                .addExpectedToken(new Token(Token.Type.TEXT, " ${ENV_VAR_4}'", " ${ENV_VAR_4}'")));

        return list.stream();
    }

    /**
     * Method to test the tokenizer, but not validate the token list
     *
     * @throws TokenizerException If an error occurs during tokenization
     */
    @Test
    public void testLinuxCommands() throws Throwable {
        String resourceName = "/linux-commands.txt";
        InputStream inputStream = null;

        try {
            inputStream = getClass().getResourceAsStream(resourceName);

            if (inputStream == null) {
                throw new FileNotFoundException(format("resource [%s] not found", resourceName));
            }

            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                while (true) {
                    String line = bufferedReader.readLine();

                    if (line == null) {
                        break;
                    }

                    if (line.startsWith("#")) {
                        continue;
                    }

                    assertThatNoException().isThrownBy(() -> Tokenizer.validate(line));
                }
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /** Class to implement TestData */
    public static class TestData {

        private String input;
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
            this.input = input;
            return this;
        }

        /**
         * Method to get the input
         *
         * @return the input
         */
        public String getInput() {
            return input;
        }

        /**
         * Method to add an expected token
         *
         * @param token token
         * @return the TestData
         */
        public TestData addExpectedToken(Token token) {
            this.expectedTokens.add(token);
            return this;
        }

        /**
         * Method to get the expected tokens
         *
         * @return the expected tokens
         */
        public List<Token> getExpectedTokens() {
            return expectedTokens;
        }

        @Override
        public String toString() {
            return input;
        }
    }
}
