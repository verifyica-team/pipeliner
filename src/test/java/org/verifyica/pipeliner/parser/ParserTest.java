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

package org.verifyica.pipeliner.parser;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
import org.verifyica.pipeliner.parser.tokens.ParsedEnvironmentVariable;
import org.verifyica.pipeliner.parser.tokens.ParsedText;
import org.verifyica.pipeliner.parser.tokens.ParsedVariable;
import org.verifyica.pipeliner.parser.tokens.Token;

/** Class to implement LexerTest */
public class ParserTest {

    /**
     * Method to test the Parser, validating the token list returned is equal the expected token list
     *
     * @param testData the test data
     * @throws SyntaxException if an error occurs during parsing
     */
    @ParameterizedTest
    @MethodSource("getParserTestData")
    public void testParser(TestData testData) throws SyntaxException {
        List<Token> tokens = Parser.parse(testData.input);

        assertThat(tokens).isNotNull();
        assertThat(tokens.size()).isEqualTo(testData.getExpectedTokens().size());

        // Assert specific token values, since the test/TestData currently doesn't use position or modifiers
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            Token expectedToken = testData.getExpectedTokens().get(i);

            assertThat(token.getType()).isEqualTo(expectedToken.getType());
            assertThat(token.getText()).isEqualTo(expectedToken.getText());

            switch (token.getType()) {
                case VARIABLE: {
                    assertThat(token.cast(ParsedVariable.class).getScopedValue())
                            .isEqualTo(expectedToken.cast(ParsedVariable.class).getScopedValue());
                    break;
                }
                case ENVIRONMENT_VARIABLE: {
                    assertThat(token.cast(ParsedEnvironmentVariable.class).getValue())
                            .isEqualTo(expectedToken
                                    .cast(ParsedEnvironmentVariable.class)
                                    .getValue());
                    break;
                }
                case TEXT: {
                    assertThat(token.cast(ParsedText.class).getValue())
                            .isEqualTo(expectedToken.cast(ParsedText.class).getValue());
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getSyntaxExceptionTestData")
    public void testParserSyntaxError(TestData testData) {
        assertThatExceptionOfType(SyntaxException.class).isThrownBy(() -> Parser.validate(testData.input()));
    }

    /**
     * Method to get the test data
     *
     * @return the test data
     */
    public static Stream<TestData> getParserTestData() throws SyntaxException {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData().input("echo    ").addExpectedToken(new ParsedText(-1, "echo    ")));

        list.add(new TestData().input(" echo    ").addExpectedToken(new ParsedText(-1, " echo    ")));

        list.add(new TestData().input("   echo").addExpectedToken(new ParsedText(-1, "   echo")));

        list.add(new TestData().input("echo \\${{foo}}").addExpectedToken(new ParsedText(-1, "echo \\${{foo}}")));

        list.add(new TestData()
                .input("${{ variable_1 }}")
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1")));

        list.add(new TestData()
                .input("${{   variable_1   }}")
                .addExpectedToken(ParsedVariable.create("${{   variable_1   }}", "variable_1")));

        list.add(new TestData()
                .input("${{\tvariable_1\t}}")
                .addExpectedToken(ParsedVariable.create("${{\tvariable_1\t}}", "variable_1")));

        list.add(new TestData()
                .input("${{\t variable_1\t }}")
                .addExpectedToken(ParsedVariable.create("${{\t variable_1\t }}", "variable_1")));

        list.add(new TestData()
                .input("${{ \t variable_1 \t }}")
                .addExpectedToken(ParsedVariable.create("${{ \t variable_1 \t }}", "variable_1")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{ variable_2 }}")
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedVariable.create("${{ variable_2 }}", "variable_2")));

        list.add(new TestData()
                .input("echo ${{ variable_1 }} ${{ variable_2 }}")
                .addExpectedToken(ParsedText.create("echo ", "echo "))
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedVariable.create("${{ variable_2 }}", "variable_2")));

        list.add(new TestData()
                .input("${{ variable_1 }} echo ${{ variable_2 }}")
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1"))
                .addExpectedToken(ParsedText.create(" echo ", " echo "))
                .addExpectedToken(ParsedVariable.create("${{ variable_2 }}", "variable_2")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{ variable_2 }} echo")
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedVariable.create("${{ variable_2 }}", "variable_2"))
                .addExpectedToken(ParsedText.create(" echo", " echo")));

        list.add(new TestData()
                .input("echo ${{ variable_1 }} echo ${{ variable_2 }} echo")
                .addExpectedToken(ParsedText.create("echo ", "echo "))
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1"))
                .addExpectedToken(ParsedText.create(" echo ", " echo "))
                .addExpectedToken(ParsedVariable.create("${{ variable_2 }}", "variable_2"))
                .addExpectedToken(ParsedText.create(" echo", " echo")));

        list.add(new TestData()
                .input("echo \\${{ variable_1 }} echo ${{ variable_2 }} echo")
                .addExpectedToken(ParsedText.create("echo \\${{ variable_1 }} echo ", "echo \\${{ variable_1 }} echo "))
                .addExpectedToken(ParsedVariable.create("${{ variable_2 }}", "variable_2"))
                .addExpectedToken(ParsedText.create(" echo", " echo")));

        list.add(new TestData()
                .input("\\${{foo}}${{ variable_1 }}")
                .addExpectedToken(ParsedText.create("\\${{foo}}", "\\${{foo}}"))
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1")));

        list.add(new TestData()
                .input("\\${{foo}}${{ variable_1 }}\\${{bar}}")
                .addExpectedToken(ParsedText.create("\\${{foo}}", "\\${{foo}}"))
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1"))
                .addExpectedToken(ParsedText.create("\\${{bar}}", "\\${{bar}}")));

        list.add(new TestData()
                .input(
                        "${{ test_scripts_directory }}/test-arguments-are-equal.sh \"${{ test_scripts_directory }}\" \"${{ test_scripts_directory }}\"")
                .addExpectedToken(ParsedVariable.create("${{ test_scripts_directory }}", "test_scripts_directory"))
                .addExpectedToken(
                        ParsedText.create("/test-arguments-are-equal.sh \"", "/test-arguments-are-equal.sh \""))
                .addExpectedToken(ParsedVariable.create("${{ test_scripts_directory }}", "test_scripts_directory"))
                .addExpectedToken(ParsedText.create("\" \"", "\" \""))
                .addExpectedToken(ParsedVariable.create("${{ test_scripts_directory }}", "test_scripts_directory"))
                .addExpectedToken(ParsedText.create("\"", "\"")));

        list.add(new TestData()
                .input("${{ test_scripts_directory }}_foo")
                .addExpectedToken(ParsedVariable.create("${{ test_scripts_directory }}", "test_scripts_directory"))
                .addExpectedToken(ParsedText.create("_foo", "_foo")));

        list.add(new TestData()
                .input("${{foo}}${{  bar  }}")
                .addExpectedToken(ParsedVariable.create("${{foo}}", "foo"))
                .addExpectedToken(ParsedVariable.create("${{  bar  }}", "bar")));

        list.add(new TestData()
                .input("echo \\\"${{ pipeline_id_test_variable }}\\\"")
                .addExpectedToken(ParsedText.create("echo \\\"", "echo \\\""))
                .addExpectedToken(
                        ParsedVariable.create("${{ pipeline_id_test_variable }}", "pipeline_id_test_variable"))
                .addExpectedToken(ParsedText.create("\\\"", "\\\"")));

        list.add(new TestData()
                .input("_Multiple_\\$_\\${\\${{Combinations}}_")
                .addExpectedToken(ParsedText.create(
                        "_Multiple_\\$_\\${\\${{Combinations}}_", "_Multiple_\\$_\\${\\${{Combinations}}_")));

        list.add(new TestData()
                .input("Mix\\${String\\\"With\\${{Underscores}}_")
                .addExpectedToken(ParsedText.create(
                        "Mix\\${String\\\"With\\${{Underscores}}_", "Mix\\${String\\\"With\\${{Underscores}}_")));

        list.add(new TestData()
                .input("'$FOO'")
                .addExpectedToken(ParsedText.create("'", "'"))
                .addExpectedToken(ParsedEnvironmentVariable.create("$FOO", "FOO"))
                .addExpectedToken(ParsedText.create("'", "'")));

        list.add(new TestData()
                .input("'$FOO")
                .addExpectedToken(ParsedText.create("'", "'"))
                .addExpectedToken(ParsedEnvironmentVariable.create("$FOO", "FOO")));

        list.add(new TestData()
                .input("$FOO'")
                .addExpectedToken(ParsedEnvironmentVariable.create("$FOO", "FOO"))
                .addExpectedToken(ParsedText.create("'", "'")));

        list.add(new TestData().input("\\").addExpectedToken(ParsedText.create("\\", "\\")));

        list.add(new TestData().input("\\\\").addExpectedToken(ParsedText.create("\\\\", "\\\\")));

        list.add(new TestData().input("\\$").addExpectedToken(ParsedText.create("\\$", "\\$")));

        list.add(new TestData().input("\\${{").addExpectedToken(ParsedText.create("\\${{", "\\${{")));

        list.add(new TestData().input("\"").addExpectedToken(ParsedText.create("\"", "\"")));

        list.add(new TestData().input("'").addExpectedToken(ParsedText.create("'", "'")));

        list.add(new TestData().input("''").addExpectedToken(ParsedText.create("''", "''")));

        list.add(new TestData().input("'''").addExpectedToken(ParsedText.create("'''", "'''")));

        list.add(new TestData().input("'$'").addExpectedToken(ParsedText.create("'$'", "'$'")));

        list.add(new TestData().input("'$$'").addExpectedToken(ParsedText.create("'$$'", "'$$'")));

        list.add(new TestData().input("${{ _ }}").addExpectedToken(ParsedVariable.create("${{ _ }}", "_")));

        list.add(new TestData().input("'$$$'").addExpectedToken(ParsedText.create("'$$$'", "'$$$'")));

        list.add(new TestData().input("'$\\").addExpectedToken(ParsedText.create("'$\\", "'$\\")));

        list.add(new TestData().input("'$$'").addExpectedToken(ParsedText.create("'$$'", "'$$'")));

        list.add(new TestData().input("'$$$'").addExpectedToken(ParsedText.create("'$$$'", "'$$$'")));

        list.add(new TestData()
                .input("echo '${{ variable_1 }}'")
                .addExpectedToken(ParsedText.create("echo '", "echo '"))
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1"))
                .addExpectedToken(ParsedText.create("'", "'")));

        list.add(new TestData()
                .input("echo '\\${{ variable_1 }}'")
                .addExpectedToken(ParsedText.create("echo '\\${{ variable_1 }}'", "echo '\\${{ variable_1 }}'")));

        list.add(new TestData()
                .input("echo \\${{ variable_1 }} ${{ variable_2 }}")
                .addExpectedToken(ParsedText.create("echo \\${{ variable_1 }} ", "echo \\${{ variable_1 }} "))
                .addExpectedToken(ParsedVariable.create("${{ variable_2 }}", "variable_2")));

        list.add(new TestData()
                .input("echo '\\$FOO'")
                .addExpectedToken(ParsedText.create("echo '\\$FOO'", "echo '\\$FOO'")));

        list.add(new TestData().input("echo \\$FOO").addExpectedToken(ParsedText.create("echo \\$FOO", "echo \\$FOO")));

        list.add(new TestData()
                .input("echo \\$FOO $BAR")
                .addExpectedToken(ParsedText.create("echo \\$FOO ", "echo \\$FOO "))
                .addExpectedToken(ParsedEnvironmentVariable.create("$BAR", "BAR")));

        list.add(new TestData()
                .input("echo '$ FOO'")
                .addExpectedToken(ParsedText.create("echo '$ FOO'", "echo '$ FOO'")));

        list.add(new TestData()
                .input("echo '$$ FOO'")
                .addExpectedToken(ParsedText.create("echo '$$ FOO'", "echo '$$ FOO'")));

        list.add(new TestData()
                .input("echo '$$$ FOO'")
                .addExpectedToken(ParsedText.create("echo '$$$ FOO'", "echo '$$$ FOO'")));

        list.add(new TestData()
                .input("${{ variable_1 }}\\$")
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1"))
                .addExpectedToken(ParsedText.create("\\$", "\\$")));

        list.add(new TestData()
                .input("${{ variable_1 }}\\")
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1"))
                .addExpectedToken(ParsedText.create("\\", "\\")));

        list.add(new TestData()
                .input("\\${{ variable_1 }}\\${{ variable_2 }}")
                .addExpectedToken(ParsedText.create(
                        "\\${{ variable_1 }}\\${{ variable_2 }}", "\\${{ variable_1 }}\\${{ variable_2 }}")));

        list.add(new TestData()
                .input("ps aux | awk '{print $1, $3}' > output.txt")
                .addExpectedToken(ParsedText.create(
                        "ps aux | awk '{print $1, $3}' > output.txt", "ps aux | awk '{print $1, $3}' > output.txt")));

        list.add(new TestData()
                .input("echo \\\"${{ test_variable }}\\\" \'${{ test_variable }}\'")
                .addExpectedToken(ParsedText.create("echo \\\"", "echo \\\""))
                .addExpectedToken(ParsedVariable.create("${{ test_variable }}", "test_variable"))
                .addExpectedToken(ParsedText.create("\\\" '", "\\\" '"))
                .addExpectedToken(ParsedVariable.create("${{ test_variable }}", "test_variable"))
                .addExpectedToken(ParsedText.create("'", "'")));

        list.add(new TestData()
                .input("cat file.txt | tr '[:lower:]' '[:upper:]'")
                .addExpectedToken(ParsedText.create(
                        "cat file.txt | tr '[:lower:]' '[:upper:]'", "cat file.txt | tr '[:lower:]' '[:upper:]'")));

        list.add(new TestData()
                .input("sed 's/\\${{}}/X/g' file")
                .addExpectedToken(ParsedText.create("sed 's/\\${{}}/X/g' file", "sed 's/\\${{}}/X/g' file")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{variable_2}} $ENV_VAR_1 ${ENV_VAR_2}")
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedVariable.create("${{variable_2}}", "variable_2"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedEnvironmentVariable.create("$ENV_VAR_1", "ENV_VAR_1"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedEnvironmentVariable.create("${ENV_VAR_2}", "ENV_VAR_2")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{variable_2}} $ENV_VAR_1 ${ENV_VAR_2} '$ENV_VAR_3 ${ENV_VAR_4}'")
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedVariable.create("${{variable_2}}", "variable_2"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedEnvironmentVariable.create("$ENV_VAR_1", "ENV_VAR_1"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedEnvironmentVariable.create("${ENV_VAR_2}", "ENV_VAR_2"))
                .addExpectedToken(ParsedText.create(" '", " '"))
                .addExpectedToken(ParsedEnvironmentVariable.create("$ENV_VAR_3", "ENV_VAR_3"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedEnvironmentVariable.create("${ENV_VAR_4}", "ENV_VAR_4"))
                .addExpectedToken(ParsedText.create("'", "'")));

        list.add(new TestData()
                .input(
                        "${{ variable_1 }} ${{variable_2}} $ENV_VAR_1 ${ENV_VAR_2} '$ENV_VAR_3 ${{ variable_3 }} ${ENV_VAR_4}'")
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedVariable.create("${{variable_2}}", "variable_2"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedEnvironmentVariable.create("$ENV_VAR_1", "ENV_VAR_1"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedEnvironmentVariable.create("${ENV_VAR_2}", "ENV_VAR_2"))
                .addExpectedToken(ParsedText.create(" '", " '"))
                .addExpectedToken(ParsedEnvironmentVariable.create("$ENV_VAR_3", "ENV_VAR_3"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedVariable.create("${{ variable_3 }}", "variable_3"))
                .addExpectedToken(ParsedText.create(" ", " "))
                .addExpectedToken(ParsedEnvironmentVariable.create("${ENV_VAR_4}", "ENV_VAR_4"))
                .addExpectedToken(ParsedText.create("'", "'")));

        list.add(new TestData().input("\\ foo \\").addExpectedToken(ParsedText.create("\\ foo \\", "\\ foo \\")));

        list.add(new TestData().input("\\$foo\\").addExpectedToken(ParsedText.create("\\$foo\\", "\\$foo\\")));

        list.add(new TestData()
                .input("\\ $foo\\")
                .addExpectedToken(ParsedText.create("\\ ", "\\ "))
                .addExpectedToken(ParsedEnvironmentVariable.create("$foo", "foo"))
                .addExpectedToken(ParsedText.create("\\", "\\")));

        list.add(new TestData()
                .input("\\ ${{ variable_1 }}\\")
                .addExpectedToken(ParsedText.create("\\ ", "\\ "))
                .addExpectedToken(ParsedVariable.create("${{ variable_1 }}", "variable_1"))
                .addExpectedToken(ParsedText.create("\\", "\\")));

        list.add(new TestData()
                .input("\\${{ variable_1 }}\\")
                .addExpectedToken(ParsedText.create("\\${{ variable_1 }}\\", "\\${{ variable_1 }}\\")));

        list.add(new TestData().input("$-").addExpectedToken(ParsedText.create("$-", "$-")));

        list.add(new TestData().input("$$").addExpectedToken(ParsedText.create("$$", "$$")));

        list.add(new TestData()
                .input("$_$")
                .addExpectedToken(ParsedEnvironmentVariable.create("$_", "_"))
                .addExpectedToken(ParsedText.create("$", "$")));

        list.add(new TestData()
                .input("$_-")
                .addExpectedToken(ParsedEnvironmentVariable.create("$_", "_"))
                .addExpectedToken(ParsedText.create("-", "-")));

        list.add(new TestData()
                .input("{{ foo }} {{bar}}")
                .addExpectedToken(ParsedText.create("{{ foo }} {{bar}}", "{{ foo }} {{bar}}")));

        list.add(new TestData()
                .input("${{ a }}}")
                .addExpectedToken(ParsedVariable.create("${{ a }}", "a"))
                .addExpectedToken(ParsedText.create("}", "}")));

        list.add(new TestData().input("${{ _foo }}").addExpectedToken(ParsedVariable.create("${{ _foo }}", "_foo")));

        list.add(new TestData().input("${{ _A_ }}").addExpectedToken(ParsedVariable.create("${{ _A_ }}", "_A_")));

        list.add(new TestData()
                .input("${{ csharp_extension_sha1_checksum }}")
                .addExpectedToken(ParsedVariable.create(
                        "${{ csharp_extension_sha1_checksum }}", "csharp_extension_sha1_checksum")));

        // Special cases, where the variable name is empty, which could be used in a Bash command

        list.add(new TestData().input("${{}}").addExpectedToken(ParsedText.create("${{}}", "${{}}")));

        list.add(new TestData().input("${{ }}").addExpectedToken(ParsedText.create("${{ }}", "${{ }}")));

        list.add(new TestData().input("${{\t}}").addExpectedToken(ParsedText.create("${{\t}}", "${{\t}}")));

        list.add(new TestData().input("${{\t \t}}").addExpectedToken(ParsedText.create("${{\t \t}}", "${{\t \t}}")));

        list.add(new TestData().input("#{{\t \t}}").addExpectedToken(ParsedText.create("#{{\t \t}}", "#{{\t \t}}")));

        list.add(new TestData()
                .input("${{ _" + ParsedVariable.SCOPE_SEPARATOR + "_ }}")
                .addExpectedToken(ParsedVariable.create(
                        "${{ _" + ParsedVariable.SCOPE_SEPARATOR + "_ }}",
                        "_" + ParsedVariable.SCOPE_SEPARATOR + "_")));

        list.add(new TestData()
                .input("${{ a" + ParsedVariable.SCOPE_SEPARATOR + "_ }}")
                .addExpectedToken(ParsedVariable.create(
                        "${{ a" + ParsedVariable.SCOPE_SEPARATOR + "_ }}",
                        "a" + ParsedVariable.SCOPE_SEPARATOR + "_")));

        return list.stream();
    }

    public static Stream<TestData> getSyntaxExceptionTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData().input("${{ - }}"));

        list.add(new TestData().input("${{ . }}"));

        list.add(new TestData().input("${{ a$ }}"));

        list.add(new TestData().input("${{ #foo }}"));

        list.add(new TestData().input("${{ foo# }}"));

        list.add(new TestData().input("echo ${{ - }}"));

        list.add(new TestData().input("echo ${{ . }}"));

        list.add(new TestData().input("echo ${{ a$ }}"));

        list.add(new TestData().input("echo ${{ #foo }}"));

        list.add(new TestData().input("echo ${{ foo# }}"));

        list.add(new TestData().input("echo ${{ foo..bar }}"));

        for (TestData testData : new ArrayList<>(list)) {
            list.add(new TestData().input(testData.input().replaceAll("\\$\\{\\{ ", "\\${{")));

            list.add(new TestData().input(testData.input().replaceAll(" }} ", "}}")));

            list.add(new TestData()
                    .input(testData.input().replaceAll("\\$\\{\\{ ", "\\${{").replaceAll(" }} ", "}}")));
        }

        return list.stream();
    }

    /**
     * Method to test the Parser, but not validate the token list
     *
     * @throws SyntaxException if an error occurs during parsing
     */
    @Test
    public void testLinuxCommands() throws Throwable {
        String resourceName = "/linux-commands.txt";
        InputStream inputStream = null;

        try {
            // Get the input stream
            inputStream = getClass().getResourceAsStream(resourceName);

            // Throw an exception if the input stream is null
            if (inputStream == null) {
                throw new FileNotFoundException(format("resource [%s] not found", resourceName));
            }

            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                while (true) {
                    // Read a line
                    String line = bufferedReader.readLine();

                    // EOF
                    if (line == null) {
                        break;
                    }

                    // Skip commented out lines
                    if (line.trim().startsWith("#")) {
                        continue;
                    }

                    // Assert no exception is thrown when tokenizing the line
                    assertThatNoException().isThrownBy(() -> Parser.validate(line));
                }
            }
        } finally {
            // Close the input stream
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
        public String input() {
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
