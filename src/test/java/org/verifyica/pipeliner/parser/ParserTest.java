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
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.verifyica.pipeliner.model.SyntaxException;

/** Class to implement ParserTest */
@Disabled
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
        List<Parser.Token> parsedTokens = new Parser(testData.input()).parseAll();

        System.out.printf("input %s%n", testData.input());
        for (Parser.Token token : parsedTokens) {
            System.out.printf("  %s%n", token);
        }

        assertThat(parsedTokens).isNotNull();
        assertThat(parsedTokens.size()).isEqualTo(testData.expectedTokens().size());

        // Assert specific parsed token values, since the test/TestData currently doesn't use position
        for (int i = 0; i < parsedTokens.size(); i++) {
            Parser.Token actualParsedToken = parsedTokens.get(i);
            Parser.Token expectedParserToken = testData.expectedTokens().get(i);

            assertThat(actualParsedToken.getType()).isEqualTo(expectedParserToken.getType());
            assertThat(actualParsedToken.getText()).isEqualTo(expectedParserToken.getText());
            // assertThat(actualParsedToken.getName()).isEqualTo(expectedParserToken.getName());

            if (actualParsedToken.getType() == Parser.Token.Type.VARIABLE) {
                // assertThat(actualParsedToken.getScope()).isEqualTo(expectedParserToken.getScope());
                // assertThat(actualParsedToken.getQualifiedName()).isEqualTo(expectedParserToken.getQualifiedName());
                // assertThat(actualParsedToken.getModifiers()).isEqualTo(expectedParserToken.getModifiers());
            } else {
                assertThat(actualParsedToken.getText()).isEqualTo(expectedParserToken.getText());
            }
        }

        assertThat(toInput(testData.expectedTokens())).isEqualTo(testData.input());
    }

    /**
     * Method to get the test data
     *
     * @return the test data
     */
    public static Stream<TestData> getParserTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData().input("echo    ").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo    ")));

        list.add(
                new TestData().input(" echo    ").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " echo    ")));

        list.add(new TestData().input("   echo").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "   echo")));

        list.add(new TestData()
                .input("echo \\${{foo}}")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo \\${{foo}}")));

        list.add(new TestData()
                .input("${{ variable_1 }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}")));

        list.add(new TestData()
                .input("${{   variable_1   }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{   variable_1   }}")));

        list.add(new TestData()
                .input("${{\tvariable_1\t}}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{\tvariable_1\t}}")));

        list.add(new TestData()
                .input("${{\t variable_1\t }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{\t variable_1\t }}")));

        list.add(new TestData()
                .input("${{ \t variable_1 \t }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ \t variable_1 \t }}")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{ variable_2 }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_2 }}")));

        list.add(new TestData()
                .input("echo ${{ variable_1 }} ${{ variable_2 }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_2 }}")));

        list.add(new TestData()
                .input("${{ variable_1 }} echo ${{ variable_2 }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " echo "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_2 }}")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{ variable_2 }} echo")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_2 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " echo")));

        list.add(new TestData()
                .input("echo ${{ variable_1 }} echo ${{ variable_2 }} echo")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " echo "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_2 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " echo")));

        list.add(new TestData()
                .input("echo \\${{ variable_1 }} echo ${{ variable_2 }} echo")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo \\${{ variable_1 }} echo "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_2 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " echo")));

        list.add(new TestData()
                .input("\\${{foo}}${{ variable_1 }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\${{foo}}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}")));

        list.add(new TestData()
                .input("\\${{foo}}${{ variable_1 }}\\${{bar}}")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\${{foo}}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\${{bar}}")));

        list.add(new TestData()
                .input(
                        "${{ test_scripts_directory }}/test-arguments-are-equal.sh \"${{ test_scripts_directory }}\" \"${{ test_scripts_directory }}\"")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ test_scripts_directory }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "/test-arguments-are-equal.sh \""))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ test_scripts_directory }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\" \""))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ test_scripts_directory }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\"")));

        list.add(new TestData()
                .input("${{ test_scripts_directory }}_foo")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ test_scripts_directory }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "_foo")));

        list.add(new TestData()
                .input("${{foo}}${{  bar  }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{foo}}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{  bar  }}")));

        list.add(new TestData()
                .input("echo \\\"${{ pipeline_id_test_variable }}\\\"")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo \\\""))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ pipeline_id_test_variable }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\\"")));

        list.add(new TestData()
                .input("_Multiple_\\$_\\${\\${{Combinations}}_")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "_Multiple_\\$_\\${\\${{Combinations}}_")));

        list.add(new TestData()
                .input("Mix\\${String\\\"With\\${{Underscores}}_")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "Mix\\${String\\\"With\\${{Underscores}}_")));

        list.add(new TestData()
                .input("'$FOO'")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'"))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "$FOO"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'")));

        list.add(new TestData()
                .input("'$FOO")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'"))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "$FOO")));

        list.add(new TestData()
                .input("$FOO'")
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "$FOO"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'")));

        list.add(new TestData().input("\\").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\")));

        list.add(new TestData().input("\\\\").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\\\")));

        list.add(new TestData().input("\\$").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\$")));

        list.add(new TestData().input("\\${{").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\${{")));

        list.add(new TestData().input("\"").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\"")));

        list.add(new TestData().input("'").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'")));

        list.add(new TestData().input("''").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "''")));

        list.add(new TestData().input("'''").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'''")));

        list.add(new TestData().input("'$'").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'$'")));

        list.add(new TestData().input("'$$'").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'$$'")));

        list.add(new TestData()
                .input("${{ _ }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ _ }}")));

        list.add(new TestData().input("'$$$'").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'$$$'")));

        list.add(new TestData().input("'$\\").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'$\\")));

        list.add(new TestData().input("'$$'").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'$$'")));

        list.add(new TestData().input("'$$$'").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'$$$'")));

        list.add(new TestData()
                .input("echo '${{ variable_1 }}'")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo '"))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'")));

        list.add(new TestData()
                .input("echo '\\${{ variable_1 }}'")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo '\\${{ variable_1 }}'")));

        list.add(new TestData()
                .input("echo \\${{ variable_1 }} ${{ variable_2 }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo \\${{ variable_1 }} "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_2 }}")));

        list.add(new TestData()
                .input("echo '\\$FOO'")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo '\\$FOO'")));

        list.add(new TestData()
                .input("echo \\$FOO")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo \\$FOO")));

        list.add(new TestData()
                .input("echo \\$FOO $BAR")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo \\$FOO "))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "$BAR")));

        list.add(new TestData()
                .input("echo '$ FOO'")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo '$ FOO'")));

        list.add(new TestData()
                .input("echo '$$ FOO'")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo '$$ FOO'")));

        list.add(new TestData()
                .input("echo '$$$ FOO'")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo '$$$ FOO'")));

        list.add(new TestData()
                .input("${{ variable_1 }}\\$")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\$")));

        list.add(new TestData()
                .input("${{ variable_1 }}\\")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\")));

        list.add(new TestData()
                .input("\\${{ variable_1 }}\\${{ variable_2 }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\${{ variable_1 }}\\${{ variable_2 }}")));

        list.add(new TestData()
                .input("ps aux | awk '{print $1, $3}' > output.txt")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "ps aux | awk '{print $1, $3}' > output.txt")));

        list.add(new TestData()
                .input("echo \\\"${{ test_variable }}\\\" '${{ test_variable }}'")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "echo \\\""))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ test_variable }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\\" '"))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ test_variable }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'")));

        list.add(new TestData()
                .input("cat file.txt | tr '[:lower:]' '[:upper:]'")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "cat file.txt | tr '[:lower:]' '[:upper:]'")));

        list.add(new TestData()
                .input("sed 's/\\${{}}/X/g' file")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "sed 's/\\${{}}/X/g' file")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{variable_2}} $ENV_VAR_1 ${ENV_VAR_2}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{variable_2}}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "$ENV_VAR_1"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "${ENV_VAR_2}")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{variable_2}} $ENV_VAR_1 ${ENV_VAR_2} '$ENV_VAR_3 ${ENV_VAR_4}'")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{variable_2}}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "$ENV_VAR_1"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "${ENV_VAR_2}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " '"))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "$ENV_VAR_3"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "${ENV_VAR_4}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'")));

        list.add(new TestData()
                .input(
                        "${{ variable_1 }} ${{variable_2}} $ENV_VAR_1 ${ENV_VAR_2} '$ENV_VAR_3 ${{ variable_3 }} ${ENV_VAR_4}'")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{variable_2}}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "$ENV_VAR_1"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "${ENV_VAR_2}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " '"))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "$ENV_VAR_3"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_3 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, " "))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "${ENV_VAR_4}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "'")));

        list.add(
                new TestData().input("\\ foo \\").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\ foo \\")));

        list.add(new TestData().input("\\$foo\\").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\$foo\\")));

        list.add(new TestData()
                .input("\\ $foo\\")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\ "))
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "$foo"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\")));

        list.add(new TestData()
                .input("\\ ${{ variable_1 }}\\")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\ "))
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ variable_1 }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\")));

        list.add(new TestData()
                .input("\\${{ variable_1 }}\\")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "\\${{ variable_1 }}\\")));

        list.add(new TestData().input("$-").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "$-")));

        list.add(new TestData().input("$$").expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "$$")));

        list.add(new TestData()
                .input("$_$")
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "$_"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "$")));

        list.add(new TestData()
                .input("$_-")
                .expectedToken(new Parser.Token(Parser.Token.Type.ENVIRONMENT_VARIABLE, "$_"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "-")));

        list.add(new TestData()
                .input("{{ foo }} {{bar}}")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "{{ foo }} {{bar}}")));

        list.add(new TestData()
                .input("${{ a }}}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ a }}"))
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "}")));

        list.add(new TestData()
                .input("${{ _foo }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ _foo }}")));

        list.add(new TestData()
                .input("${{ _A_ }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ _A_ }}")));

        list.add(new TestData()
                .input("${{ csharp_extension_sha1_checksum }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ csharp_extension_sha1_checksum }}")));

        list.add(new TestData()
                .input("${{ required:variable_1 }}")
                .expectedToken(new Parser.Token(Parser.Token.Type.VARIABLE, "${{ required:variable_1 }}")));

        list.add(new TestData()
                .input("#{{\t \t}}")
                .expectedToken(new Parser.Token(Parser.Token.Type.TEXT, "#{{\t \t}}")));

        /*
        list.add(new TestData()
                .input("${{ _" + Parser.Token.SCOPE_SEPARATOR + "_ }}")
                .expectedToken(
                        new Parser.Token(Parser.Token.Type.VARIABLE, "${{ _" + Parser.Token.SCOPE_SEPARATOR + "_ }}")));

        list.add(new TestData()
                .input("${{ a" + Parser.Token.SCOPE_SEPARATOR + "_ }}")
                .expectedToken(
                        new Parser.Token(Parser.Token.Type.VARIABLE, "${{ a" + Parser.Token.SCOPE_SEPARATOR + "_ }}")));
        */

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

        try (InputStream inputStream = getClass().getResourceAsStream(resourceName)) {
            // Get the input stream

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
                    assertThatNoException().isThrownBy(() -> new Parser(line).parseAll());
                }
            }
        }
    }

    public static String toInput(List<Parser.Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Parser.Token token : tokens) {
            sb.append(token.getText());
        }
        return sb.toString();
    }

    /** Class to implement TestData */
    public static class TestData {

        private String input;
        private final List<Parser.Token> expectedTokens;

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
        public TestData expectedToken(Parser.Token token) {
            this.expectedTokens.add(token);
            return this;
        }

        /**
         * Method to get the expected tokens
         *
         * @return the expected tokens
         */
        public List<Parser.Token> expectedTokens() {
            return expectedTokens;
        }

        @Override
        public String toString() {
            return input;
        }
    }
}
