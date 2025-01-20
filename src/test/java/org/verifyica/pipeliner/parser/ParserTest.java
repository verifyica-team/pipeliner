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
        List<ParsedToken> parsedTokens = Parser.parse(testData.input);

        assertThat(parsedTokens).isNotNull();
        assertThat(parsedTokens.size()).isEqualTo(testData.getExpectedTokens().size());

        // Assert specific token variables, since the test/TestData currently doesn't use position or modifiers
        for (int i = 0; i < parsedTokens.size(); i++) {
            ParsedToken parsedToken = parsedTokens.get(i);
            ParsedToken expectedToken = testData.getExpectedTokens().get(i);

            assertThat(parsedToken.getType()).isEqualTo(expectedToken.getType());
            assertThat(parsedToken.getText()).isEqualTo(expectedToken.getText());
            assertThat(parsedToken.getValue()).isEqualTo(expectedToken.getValue());
        }
    }

    @ParameterizedTest
    @MethodSource("getSyntaxExceptionTestData")
    public void testParserSyntaxError(TestData testData) {
        assertThatExceptionOfType(SyntaxException.class).isThrownBy(() -> Parser.validate(testData.getInput()));
    }

    /**
     * Method to get the test data
     *
     * @return the test data
     */
    public static Stream<TestData> getParserTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData()
                .input("echo    ")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "echo    ", "echo    ")));

        list.add(new TestData()
                .input(" echo    ")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " echo    ", " echo    ")));

        list.add(new TestData()
                .input("   echo")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "   echo", "   echo")));

        list.add(new TestData()
                .input("echo \\${{foo}}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "echo \\${{foo}}", "echo \\${{foo}}")));

        list.add(new TestData()
                .input("${{ variable_1 }}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{ variable_2 }}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_2 }}", "variable_2")));

        list.add(new TestData()
                .input("echo ${{ variable_1 }} ${{ variable_2 }}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "echo ", "echo "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_2 }}", "variable_2")));

        list.add(new TestData()
                .input("${{ variable_1 }} echo ${{ variable_2 }}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " echo ", " echo "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_2 }}", "variable_2")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{ variable_2 }} echo")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_2 }}", "variable_2"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " echo", " echo")));

        list.add(new TestData()
                .input("echo ${{ variable_1 }} echo ${{ variable_2 }} echo")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "echo ", "echo "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " echo ", " echo "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_2 }}", "variable_2"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " echo", " echo")));

        list.add(new TestData()
                .input("echo \\${{ variable_1 }} echo ${{ variable_2 }} echo")
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.TEXT, "echo \\${{ variable_1 }} echo ", "echo \\${{ variable_1 }} echo "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_2 }}", "variable_2"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " echo", " echo")));

        list.add(new TestData()
                .input("\\${{foo}}${{ variable_1 }}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\${{foo}}", "\\${{foo}}"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1")));

        list.add(new TestData()
                .input("\\${{foo}}${{ variable_1 }}\\${{bar}}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\${{foo}}", "\\${{foo}}"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\${{bar}}", "\\${{bar}}")));

        list.add(new TestData()
                .input(
                        "${{ test_scripts_directory }}/test-arguments-are-equal.sh \"${{ test_scripts_directory }}\" \"${{ test_scripts_directory }}\"")
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.VARIABLE, "${{ test_scripts_directory }}", "test_scripts_directory"))
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.TEXT, "/test-arguments-are-equal.sh \"", "/test-arguments-are-equal.sh \""))
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.VARIABLE, "${{ test_scripts_directory }}", "test_scripts_directory"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\" \"", "\" \""))
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.VARIABLE, "${{ test_scripts_directory }}", "test_scripts_directory"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\"", "\"")));

        list.add(new TestData()
                .input("${{ test_scripts_directory }}_foo")
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.VARIABLE, "${{ test_scripts_directory }}", "test_scripts_directory"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "_foo", "_foo")));

        list.add(new TestData()
                .input("${{foo}}${{  bar  }}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{foo}}", "foo"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{  bar  }}", "bar")));

        list.add(new TestData()
                .input("echo \\\"${{ pipeline_id_test_variable }}\\\"")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "echo \\\"", "echo \\\""))
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.VARIABLE, "${{ pipeline_id_test_variable }}", "pipeline_id_test_variable"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\\"", "\\\"")));

        list.add(new TestData()
                .input("_Multiple_\\$_\\${\\${{Combinations}}_")
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.TEXT,
                        "_Multiple_\\$_\\${\\${{Combinations}}_",
                        "_Multiple_\\$_\\${\\${{Combinations}}_")));

        list.add(new TestData()
                .input("Mix\\${String\\\"With\\${{Underscores}}_")
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.TEXT,
                        "Mix\\${String\\\"With\\${{Underscores}}_",
                        "Mix\\${String\\\"With\\${{Underscores}}_")));

        list.add(new TestData()
                .input("'$FOO'")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'", "'"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "$FOO", "FOO"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'", "'")));

        list.add(new TestData()
                .input("'$FOO")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'", "'"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "$FOO", "FOO")));

        list.add(new TestData()
                .input("$FOO'")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "$FOO", "FOO"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'", "'")));

        list.add(new TestData().input("\\").addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\", "\\")));

        list.add(new TestData().input("\\\\").addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\\\", "\\\\")));

        list.add(new TestData().input("\\$").addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\$", "\\$")));

        list.add(new TestData()
                .input("\\${{")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\${{", "\\${{")));

        list.add(new TestData().input("\"").addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\"", "\"")));

        list.add(new TestData().input("'").addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'", "'")));

        list.add(new TestData().input("''").addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "''", "''")));

        list.add(new TestData().input("'''").addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'''", "'''")));

        list.add(new TestData().input("'$'").addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'$'", "'$'")));

        list.add(new TestData().input("'$$'").addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'$$'", "'$$'")));

        list.add(new TestData()
                .input("'$$$'")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'$$$'", "'$$$'")));

        list.add(new TestData().input("'$\\").addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'$\\", "'$\\")));

        list.add(new TestData().input("'$$'").addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'$$'", "'$$'")));

        list.add(new TestData()
                .input("'$$$'")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'$$$'", "'$$$'")));

        list.add(new TestData()
                .input("echo '${{ variable_1 }}'")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "echo '", "echo '"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'", "'")));

        list.add(new TestData()
                .input("echo '\\${{ variable_1 }}'")
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.TEXT, "echo '\\${{ variable_1 }}'", "echo '\\${{ variable_1 }}'")));

        list.add(new TestData()
                .input("echo \\${{ variable_1 }} ${{ variable_2 }}")
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.TEXT, "echo \\${{ variable_1 }} ", "echo \\${{ variable_1 }} "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_2 }}", "variable_2")));

        list.add(new TestData()
                .input("echo '\\$FOO'")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "echo '\\$FOO'", "echo '\\$FOO'")));

        list.add(new TestData()
                .input("echo \\$FOO")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "echo \\$FOO", "echo \\$FOO")));

        list.add(new TestData()
                .input("echo \\$FOO $BAR")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "echo \\$FOO ", "echo \\$FOO "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "$BAR", "BAR")));

        list.add(new TestData()
                .input("echo '$ FOO'")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "echo '$ FOO'", "echo '$ FOO'")));

        list.add(new TestData()
                .input("echo '$$ FOO'")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "echo '$$ FOO'", "echo '$$ FOO'")));

        list.add(new TestData()
                .input("echo '$$$ FOO'")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "echo '$$$ FOO'", "echo '$$$ FOO'")));

        list.add(new TestData()
                .input("${{ variable_1 }}\\$")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\$", "\\$")));

        list.add(new TestData()
                .input("${{ variable_1 }}\\")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\", "\\")));

        list.add(new TestData()
                .input("\\${{ variable_1 }}\\${{ variable_2 }}")
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.TEXT,
                        "\\${{ variable_1 }}\\${{ variable_2 }}",
                        "\\${{ variable_1 }}\\${{ variable_2 }}")));

        list.add(new TestData()
                .input("ps aux | awk '{print $1, $3}' > output.txt")
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.TEXT,
                        "ps aux | awk '{print $1, $3}' > output.txt",
                        "ps aux | awk '{print $1, $3}' > output.txt")));

        list.add(new TestData()
                .input("echo \\\"${{ test_variable }}\\\" \'${{ test_variable }}\'")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "echo \\\"", "echo \\\""))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ test_variable }}", "test_variable"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\\" '", "\\\" '"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ test_variable }}", "test_variable"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'", "'")));

        list.add(new TestData()
                .input("cat file.txt | tr '[:lower:]' '[:upper:]'")
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.TEXT,
                        "cat file.txt | tr '[:lower:]' '[:upper:]'",
                        "cat file.txt | tr '[:lower:]' '[:upper:]'")));

        list.add(new TestData()
                .input("sed 's/\\${{}}/X/g' file")
                .addExpectedToken(new ParsedToken(
                        ParsedToken.Type.TEXT, "sed 's/\\${{}}/X/g' file", "sed 's/\\${{}}/X/g' file")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{variable_2}} $ENV_VAR_1 ${ENV_VAR_2}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{variable_2}}", "variable_2"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "$ENV_VAR_1", "ENV_VAR_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "${ENV_VAR_2}", "ENV_VAR_2")));

        list.add(new TestData()
                .input("${{ variable_1 }} ${{variable_2}} $ENV_VAR_1 ${ENV_VAR_2} '$ENV_VAR_3 ${ENV_VAR_4}'")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{variable_2}}", "variable_2"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "$ENV_VAR_1", "ENV_VAR_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "${ENV_VAR_2}", "ENV_VAR_2"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " '", " '"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "$ENV_VAR_3", "ENV_VAR_3"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "${ENV_VAR_4}", "ENV_VAR_4"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'", "'")));

        list.add(new TestData()
                .input(
                        "${{ variable_1 }} ${{variable_2}} $ENV_VAR_1 ${ENV_VAR_2} '$ENV_VAR_3 ${{ variable_3 }} ${ENV_VAR_4}'")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{variable_2}}", "variable_2"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "$ENV_VAR_1", "ENV_VAR_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "${ENV_VAR_2}", "ENV_VAR_2"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " '", " '"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "$ENV_VAR_3", "ENV_VAR_3"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_3 }}", "variable_3"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, " ", " "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "${ENV_VAR_4}", "ENV_VAR_4"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "'", "'")));

        list.add(new TestData()
                .input("\\ foo \\")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\ foo \\", "\\ foo \\")));

        list.add(new TestData()
                .input("\\$foo\\")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\$foo\\", "\\$foo\\")));

        list.add(new TestData()
                .input("\\ $foo\\")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\ ", "\\ "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "$foo", "foo"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\", "\\")));

        list.add(new TestData()
                .input("\\ ${{ variable_1 }}\\")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\ ", "\\ "))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ variable_1 }}", "variable_1"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "\\", "\\")));

        list.add(new TestData()
                .input("\\${{ variable_1 }}\\")
                .addExpectedToken(
                        new ParsedToken(ParsedToken.Type.TEXT, "\\${{ variable_1 }}\\", "\\${{ variable_1 }}\\")));

        list.add(new TestData().input("$-").addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "$-", "$-")));

        list.add(new TestData().input("$$").addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "$$", "$$")));

        list.add(new TestData()
                .input("${{ _ }}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ _ }}", "_")));

        list.add(new TestData()
                .input("$_$")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "$_", "_"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "$", "$")));

        list.add(new TestData()
                .input("$_-")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.ENVIRONMENT_VARIABLE, "$_", "_"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "-", "-")));

        list.add(new TestData()
                .input("{{ foo }} {{bar}}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "{{ foo }} {{bar}}", "{{ foo }} {{bar}}")));

        list.add(new TestData()
                .input("${{ a }}}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ a }}", "a"))
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "}", "}")));

        list.add(new TestData()
                .input("${{ _foo }}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.VARIABLE, "${{ _foo }}", "_foo")));

        // Special cases, where the variable name is empty, which could be used in a Bash command

        list.add(new TestData()
                .input("${{}}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "${{}}", "${{}}")));

        list.add(new TestData()
                .input("${{ }}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "${{ }}", "${{ }}")));

        list.add(new TestData()
                .input("${{\t}}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "${{\t}}", "${{\t}}")));

        list.add(new TestData()
                .input("${{\t \t}}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "${{\t \t}}", "${{\t \t}}")));

        list.add(new TestData()
                .input("#{{\t \t}}")
                .addExpectedToken(new ParsedToken(ParsedToken.Type.TEXT, "#{{\t \t}}", "#{{\t \t}}")));

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

        list.add(new TestData().input("${{ _#_ }}"));

        list.add(new TestData().input("${{ _#A_ }}"));

        list.add(new TestData().input("${{ c#.extension.sha1.checksum }}"));

        list.add(new TestData().input("${{ _._ }}"));

        list.add(new TestData().input("${{ _.A_ }}"));

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
        public String getInput() {
            return input;
        }

        /**
         * Method to add an expected token
         *
         * @param token token
         * @return the TestData
         */
        public TestData addExpectedToken(ParsedToken token) {
            this.expectedTokens.add(token);
            return this;
        }

        /**
         * Method to get the expected tokens
         *
         * @return the expected tokens
         */
        public List<ParsedToken> getExpectedTokens() {
            return expectedTokens;
        }

        @Override
        public String toString() {
            return input;
        }
    }
}
