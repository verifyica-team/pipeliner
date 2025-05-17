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
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.verifyica.pipeliner.common.SetOf;
import org.verifyica.pipeliner.parser.tokens.Modifier;
import org.verifyica.pipeliner.parser.tokens.ParsedEnvironmentVariable;
import org.verifyica.pipeliner.parser.tokens.ParsedText;
import org.verifyica.pipeliner.parser.tokens.ParsedToken;
import org.verifyica.pipeliner.parser.tokens.ParsedVariable;

/** Class to implement ParserTest */
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
        List<ParsedToken> parsedTokens = Parser.parse(testData.input());

        assertThat(parsedTokens).isNotNull();
        assertThat(parsedTokens.size()).isEqualTo(testData.expectedTokens().size());

        // Assert specific parsed token values, since the test/TestData currently doesn't use position
        for (int i = 0; i < parsedTokens.size(); i++) {
            ParsedToken parsedToken = parsedTokens.get(i);
            ParsedToken expectedParsedToken = testData.expectedTokens().get(i);

            assertThat(parsedToken.getType()).isEqualTo(expectedParsedToken.getType());
            assertThat(parsedToken.getText()).isEqualTo(expectedParsedToken.getText());

            switch (parsedToken.getType()) {
                case VARIABLE: {
                    ParsedVariable expectedParsedVariable = expectedParsedToken.cast(ParsedVariable.class);
                    ParsedVariable actualParsedVariable = parsedToken.cast(ParsedVariable.class);

                    assertThat(actualParsedVariable.getScope()).isEqualTo(expectedParsedVariable.getScope());
                    assertThat(actualParsedVariable.getValue()).isEqualTo(expectedParsedVariable.getValue());
                    assertThat(actualParsedVariable.getScopedValue())
                            .isEqualTo(expectedParsedVariable.getScopedValue());
                    assertThat(actualParsedVariable.getModifiers()).isEqualTo(expectedParsedVariable.getModifiers());

                    break;
                }
                case ENVIRONMENT_VARIABLE: {
                    ParsedEnvironmentVariable expectedParsedEnvironmentVariable =
                            expectedParsedToken.cast(ParsedEnvironmentVariable.class);
                    ParsedEnvironmentVariable actualParsedEnvironmentVariable =
                            parsedToken.cast(ParsedEnvironmentVariable.class);

                    assertThat(actualParsedEnvironmentVariable.getValue())
                            .isEqualTo(expectedParsedEnvironmentVariable.getValue());

                    break;
                }
                default: {
                    assertThat(parsedToken.getText()).isEqualTo(expectedParsedToken.getText());

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
    public static Stream<TestData> getParserTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData().input("echo    ").expectedToken(createParsedText("echo    ")));

        list.add(new TestData().input(" echo    ").expectedToken(createParsedText(" echo    ")));

        list.add(new TestData().input("   echo").expectedToken(createParsedText("   echo")));

        list.add(new TestData().input("echo \\${{foo}}").expectedToken(createParsedText("echo \\${{foo}}")));

        list.add(new TestData()
                .input("${{ variable_1 }}")
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1")));

        list.add(new TestData()
                .input("${{   variable_1   }}")
                .expectedToken(createParsedVariable("${{   variable_1   }}", "variable_1")));

        list.add(new TestData()
                .input("${{\tvariable_1\t}}")
                .expectedToken(createParsedVariable("${{\tvariable_1\t}}", "variable_1")));

        list.add(new TestData()
                .input("${{\t variable_1\t }}")
                .expectedToken(createParsedVariable("${{\t variable_1\t }}", "variable_1")));

        list.add(new TestData()
                .input("${{ \t variable_1 \t }}")
                .expectedToken(createParsedVariable("${{ \t variable_1 \t }}", "variable_1")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{ variable_2 }}")
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedVariable("${{ variable_2 }}", "variable_2")));

        list.add(new TestData()
                .input("echo ${{ variable_1 }} ${{ variable_2 }}")
                .expectedToken(createParsedText("echo "))
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedVariable("${{ variable_2 }}", "variable_2")));

        list.add(new TestData()
                .input("${{ variable_1 }} echo ${{ variable_2 }}")
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1"))
                .expectedToken(createParsedText(" echo "))
                .expectedToken(createParsedVariable("${{ variable_2 }}", "variable_2")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{ variable_2 }} echo")
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedVariable("${{ variable_2 }}", "variable_2"))
                .expectedToken(createParsedText(" echo")));

        list.add(new TestData()
                .input("echo ${{ variable_1 }} echo ${{ variable_2 }} echo")
                .expectedToken(createParsedText("echo "))
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1"))
                .expectedToken(createParsedText(" echo "))
                .expectedToken(createParsedVariable("${{ variable_2 }}", "variable_2"))
                .expectedToken(createParsedText(" echo")));

        list.add(new TestData()
                .input("echo \\${{ variable_1 }} echo ${{ variable_2 }} echo")
                .expectedToken(createParsedText("echo \\${{ variable_1 }} echo "))
                .expectedToken(createParsedVariable("${{ variable_2 }}", "variable_2"))
                .expectedToken(createParsedText(" echo")));

        list.add(new TestData()
                .input("\\${{foo}}${{ variable_1 }}")
                .expectedToken(createParsedText("\\${{foo}}"))
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1")));

        list.add(new TestData()
                .input("\\${{foo}}${{ variable_1 }}\\${{bar}}")
                .expectedToken(createParsedText("\\${{foo}}"))
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1"))
                .expectedToken(createParsedText("\\${{bar}}")));

        list.add(new TestData()
                .input(
                        "${{ test_scripts_directory }}/test-arguments-are-equal.sh \"${{ test_scripts_directory }}\" \"${{ test_scripts_directory }}\"")
                .expectedToken(createParsedVariable("${{ test_scripts_directory }}", "test_scripts_directory"))
                .expectedToken(createParsedText("/test-arguments-are-equal.sh \""))
                .expectedToken(createParsedVariable("${{ test_scripts_directory }}", "test_scripts_directory"))
                .expectedToken(createParsedText("\" \""))
                .expectedToken(createParsedVariable("${{ test_scripts_directory }}", "test_scripts_directory"))
                .expectedToken(createParsedText("\"")));

        list.add(new TestData()
                .input("${{ test_scripts_directory }}_foo")
                .expectedToken(createParsedVariable("${{ test_scripts_directory }}", "test_scripts_directory"))
                .expectedToken(createParsedText("_foo")));

        list.add(new TestData()
                .input("${{foo}}${{  bar  }}")
                .expectedToken(createParsedVariable("${{foo}}", "foo"))
                .expectedToken(createParsedVariable("${{  bar  }}", "bar")));

        list.add(new TestData()
                .input("echo \\\"${{ pipeline_id_test_variable }}\\\"")
                .expectedToken(createParsedText("echo \\\""))
                .expectedToken(createParsedVariable("${{ pipeline_id_test_variable }}", "pipeline_id_test_variable"))
                .expectedToken(createParsedText("\\\"")));

        list.add(new TestData()
                .input("_Multiple_\\$_\\${\\${{Combinations}}_")
                .expectedToken(createParsedText("_Multiple_\\$_\\${\\${{Combinations}}_")));

        list.add(new TestData()
                .input("Mix\\${String\\\"With\\${{Underscores}}_")
                .expectedToken(createParsedText("Mix\\${String\\\"With\\${{Underscores}}_")));

        list.add(new TestData()
                .input("'$FOO'")
                .expectedToken(createParsedText("'"))
                .expectedToken(createParsedEnvironmentVariable("$FOO", "FOO"))
                .expectedToken(createParsedText("'")));

        list.add(new TestData()
                .input("'$FOO")
                .expectedToken(createParsedText("'"))
                .expectedToken(createParsedEnvironmentVariable("$FOO", "FOO")));

        list.add(new TestData()
                .input("$FOO'")
                .expectedToken(createParsedEnvironmentVariable("$FOO", "FOO"))
                .expectedToken(createParsedText("'")));

        list.add(new TestData().input("\\").expectedToken(createParsedText("\\")));

        list.add(new TestData().input("\\\\").expectedToken(createParsedText("\\\\")));

        list.add(new TestData().input("\\$").expectedToken(createParsedText("\\$")));

        list.add(new TestData().input("\\${{").expectedToken(createParsedText("\\${{")));

        list.add(new TestData().input("\"").expectedToken(createParsedText("\"")));

        list.add(new TestData().input("'").expectedToken(createParsedText("'")));

        list.add(new TestData().input("''").expectedToken(createParsedText("''")));

        list.add(new TestData().input("'''").expectedToken(createParsedText("'''")));

        list.add(new TestData().input("'$'").expectedToken(createParsedText("'$'")));

        list.add(new TestData().input("'$$'").expectedToken(createParsedText("'$$'")));

        list.add(new TestData().input("${{ _ }}").expectedToken(createParsedVariable("${{ _ }}", "_")));

        list.add(new TestData().input("'$$$'").expectedToken(createParsedText("'$$$'")));

        list.add(new TestData().input("'$\\").expectedToken(createParsedText("'$\\")));

        list.add(new TestData().input("'$$'").expectedToken(createParsedText("'$$'")));

        list.add(new TestData().input("'$$$'").expectedToken(createParsedText("'$$$'")));

        list.add(new TestData()
                .input("echo '${{ variable_1 }}'")
                .expectedToken(createParsedText("echo '"))
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1"))
                .expectedToken(createParsedText("'")));

        list.add(new TestData()
                .input("echo '\\${{ variable_1 }}'")
                .expectedToken(createParsedText("echo '\\${{ variable_1 }}'")));

        list.add(new TestData()
                .input("echo \\${{ variable_1 }} ${{ variable_2 }}")
                .expectedToken(createParsedText("echo \\${{ variable_1 }} "))
                .expectedToken(createParsedVariable("${{ variable_2 }}", "variable_2")));

        list.add(new TestData().input("echo '\\$FOO'").expectedToken(createParsedText("echo '\\$FOO'")));

        list.add(new TestData().input("echo \\$FOO").expectedToken(createParsedText("echo \\$FOO")));

        list.add(new TestData()
                .input("echo \\$FOO $BAR")
                .expectedToken(createParsedText("echo \\$FOO "))
                .expectedToken(createParsedEnvironmentVariable("$BAR", "BAR")));

        list.add(new TestData().input("echo '$ FOO'").expectedToken(createParsedText("echo '$ FOO'")));

        list.add(new TestData().input("echo '$$ FOO'").expectedToken(createParsedText("echo '$$ FOO'")));

        list.add(new TestData().input("echo '$$$ FOO'").expectedToken(createParsedText("echo '$$$ FOO'")));

        list.add(new TestData()
                .input("${{ variable_1 }}\\$")
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1"))
                .expectedToken(createParsedText("\\$")));

        list.add(new TestData()
                .input("${{ variable_1 }}\\")
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1"))
                .expectedToken(createParsedText("\\")));

        list.add(new TestData()
                .input("\\${{ variable_1 }}\\${{ variable_2 }}")
                .expectedToken(createParsedText("\\${{ variable_1 }}\\${{ variable_2 }}")));

        list.add(new TestData()
                .input("ps aux | awk '{print $1, $3}' > output.txt")
                .expectedToken(createParsedText("ps aux | awk '{print $1, $3}' > output.txt")));

        list.add(new TestData()
                .input("echo \\\"${{ test_variable }}\\\" \'${{ test_variable }}\'")
                .expectedToken(createParsedText("echo \\\""))
                .expectedToken(createParsedVariable("${{ test_variable }}", "test_variable"))
                .expectedToken(createParsedText("\\\" '"))
                .expectedToken(createParsedVariable("${{ test_variable }}", "test_variable"))
                .expectedToken(createParsedText("'")));

        list.add(new TestData()
                .input("cat file.txt | tr '[:lower:]' '[:upper:]'")
                .expectedToken(createParsedText("cat file.txt | tr '[:lower:]' '[:upper:]'")));

        list.add(new TestData()
                .input("sed 's/\\${{}}/X/g' file")
                .expectedToken(createParsedText("sed 's/\\${{}}/X/g' file")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{variable_2}} $ENV_VAR_1 ${ENV_VAR_2}")
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedVariable("${{variable_2}}", "variable_2"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedEnvironmentVariable("$ENV_VAR_1", "ENV_VAR_1"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedEnvironmentVariable("${ENV_VAR_2}", "ENV_VAR_2")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{variable_2}} $ENV_VAR_1 ${ENV_VAR_2} '$ENV_VAR_3 ${ENV_VAR_4}'")
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedVariable("${{variable_2}}", "variable_2"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedEnvironmentVariable("$ENV_VAR_1", "ENV_VAR_1"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedEnvironmentVariable("${ENV_VAR_2}", "ENV_VAR_2"))
                .expectedToken(createParsedText(" '"))
                .expectedToken(createParsedEnvironmentVariable("$ENV_VAR_3", "ENV_VAR_3"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedEnvironmentVariable("${ENV_VAR_4}", "ENV_VAR_4"))
                .expectedToken(createParsedText("'")));

        list.add(new TestData()
                .input(
                        "${{ variable_1 }} ${{variable_2}} $ENV_VAR_1 ${ENV_VAR_2} '$ENV_VAR_3 ${{ variable_3 }} ${ENV_VAR_4}'")
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedVariable("${{variable_2}}", "variable_2"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedEnvironmentVariable("$ENV_VAR_1", "ENV_VAR_1"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedEnvironmentVariable("${ENV_VAR_2}", "ENV_VAR_2"))
                .expectedToken(createParsedText(" '"))
                .expectedToken(createParsedEnvironmentVariable("$ENV_VAR_3", "ENV_VAR_3"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedVariable("${{ variable_3 }}", "variable_3"))
                .expectedToken(createParsedText(" "))
                .expectedToken(createParsedEnvironmentVariable("${ENV_VAR_4}", "ENV_VAR_4"))
                .expectedToken(createParsedText("'")));

        list.add(new TestData().input("\\ foo \\").expectedToken(createParsedText("\\ foo \\")));

        list.add(new TestData().input("\\$foo\\").expectedToken(createParsedText("\\$foo\\")));

        list.add(new TestData()
                .input("\\ $foo\\")
                .expectedToken(createParsedText("\\ "))
                .expectedToken(createParsedEnvironmentVariable("$foo", "foo"))
                .expectedToken(createParsedText("\\")));

        list.add(new TestData()
                .input("\\ ${{ variable_1 }}\\")
                .expectedToken(createParsedText("\\ "))
                .expectedToken(createParsedVariable("${{ variable_1 }}", "variable_1"))
                .expectedToken(createParsedText("\\")));

        list.add(
                new TestData().input("\\${{ variable_1 }}\\").expectedToken(createParsedText("\\${{ variable_1 }}\\")));

        list.add(new TestData().input("$-").expectedToken(createParsedText("$-")));

        list.add(new TestData().input("$$").expectedToken(createParsedText("$$")));

        list.add(new TestData()
                .input("$_$")
                .expectedToken(createParsedEnvironmentVariable("$_", "_"))
                .expectedToken(createParsedText("$")));

        list.add(new TestData()
                .input("$_-")
                .expectedToken(createParsedEnvironmentVariable("$_", "_"))
                .expectedToken(createParsedText("-")));

        list.add(new TestData().input("{{ foo }} {{bar}}").expectedToken(createParsedText("{{ foo }} {{bar}}")));

        list.add(new TestData()
                .input("${{ a }}}")
                .expectedToken(createParsedVariable("${{ a }}", "a"))
                .expectedToken(createParsedText("}")));

        list.add(new TestData().input("${{ _foo }}").expectedToken(createParsedVariable("${{ _foo }}", "_foo")));

        list.add(new TestData().input("${{ _A_ }}").expectedToken(createParsedVariable("${{ _A_ }}", "_A_")));

        list.add(new TestData()
                .input("${{ csharp_extension_sha1_checksum }}")
                .expectedToken(createParsedVariable(
                        "${{ csharp_extension_sha1_checksum }}", "csharp_extension_sha1_checksum")));

        list.add(new TestData()
                .input("${{ required:variable_1 }}")
                .expectedToken(createParsedVariable(
                        "${{ required:variable_1 }}", null, "variable_1", SetOf.of(Modifier.REQUIRED))));

        // Special cases, where the variable name is empty, which could be used in a Bash command

        list.add(new TestData().input("${{}}").expectedToken(createParsedText("${{}}")));

        list.add(new TestData().input("${{ }}").expectedToken(createParsedText("${{ }}")));

        list.add(new TestData().input("${{\t}}").expectedToken(createParsedText("${{\t}}")));

        list.add(new TestData().input("${{\t \t}}").expectedToken(createParsedText("${{\t \t}}")));

        list.add(new TestData().input("#{{\t \t}}").expectedToken(createParsedText("#{{\t \t}}")));

        list.add(new TestData()
                .input("${{ _" + ParsedVariable.SCOPE_SEPARATOR + "_ }}")
                .expectedToken(
                        createParsedVariable("${{ _" + ParsedVariable.SCOPE_SEPARATOR + "_ }}", "_", "_", null)));

        list.add(new TestData()
                .input("${{ a" + ParsedVariable.SCOPE_SEPARATOR + "_ }}")
                .expectedToken(
                        createParsedVariable("${{ a" + ParsedVariable.SCOPE_SEPARATOR + "_ }}", "a", "_", null)));

        return list.stream();
    }

    public static Stream<TestData> getSyntaxExceptionTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData().input("${{ - }}"));

        list.add(new TestData().input("${{ . }}"));

        list.add(new TestData().input("${{ .. }}"));

        list.add(new TestData().input("${{ a$ }}"));

        list.add(new TestData().input("${{ #foo }}"));

        list.add(new TestData().input("${{ foo# }}"));

        list.add(new TestData().input("echo ${{ - }}"));

        list.add(new TestData().input("echo ${{ . }}"));

        list.add(new TestData().input("echo ${{ a$ }}"));

        list.add(new TestData().input("echo ${{ #foo }}"));

        list.add(new TestData().input("echo ${{ foo# }}"));

        list.add(new TestData().input("echo ${{ foo..bar }}"));

        list.add(new TestData().input("echo ${{ .foo }}"));

        list.add(new TestData().input("echo ${{ foo. }}"));

        list.add(new TestData().input("echo ${{ ,foo.bar }}"));

        list.add(new TestData().input("echo ${{ foo.bar. }}"));

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

    private static ParsedEnvironmentVariable createParsedEnvironmentVariable(String text, String value) {
        return ParsedEnvironmentVariable.builder().text(text).value(value).build();
    }

    private static ParsedVariable createParsedVariable(String text, String value) {
        return ParsedVariable.builder().text(text).value(value).build();
    }

    private static ParsedVariable createParsedVariable(
            String text, String scope, String value, Set<Modifier> modifiers) {
        return ParsedVariable.builder()
                .text(text)
                .addScope(scope)
                .value(value)
                .addModifiers(modifiers)
                .build();
    }

    private static ParsedText createParsedText(String text) {
        return ParsedText.builder().text(text).build();
    }

    /** Class to implement TestData */
    public static class TestData {

        private String input;
        private final List<ParsedToken> expectedTokens;

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
         * @param token the token
         * @return the TestData
         */
        public TestData expectedToken(ParsedToken token) {
            this.expectedTokens.add(token);
            return this;
        }

        /**
         * Method to get the expected tokens
         *
         * @return the expected tokens
         */
        public List<ParsedToken> expectedTokens() {
            return expectedTokens;
        }

        @Override
        public String toString() {
            return input;
        }
    }
}
