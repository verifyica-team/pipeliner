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

package org.verifyica.pipeliner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.verifyica.pipeliner.support.CommandLineParser;

public class CommandLineParserTest {

    @Test
    public void testFlag() {
        String[] arguments = new String[] {"--version"};
        System.out.println("command line [" + String.join(" ", arguments) + "]");

        CommandLineParser commandLineParser = createCommandLineParser(arguments);

        Set<String> flags = commandLineParser.getFlags();

        assertThat(flags).hasSize(1);
        assertThat(commandLineParser.hasFlag("--version")).isTrue();
    }

    @Test
    public void testOptionsWithArguments() {
        String[] arguments = new String[] {"--env", "foo=bar", "--env", "bar=bar2"};
        System.out.println("command line [" + String.join(" ", arguments) + "]");

        CommandLineParser commandLineParser = createCommandLineParser(arguments);

        Map<String, List<String>> options = commandLineParser.getOptions();

        assertThat(options).hasSize(1);
        assertThat(options).containsKey("--env");
        assertThat(options.get("--env")).hasSize(2);
        assertThat(options.get("--env").get(0)).isEqualTo("foo=bar");
        assertThat(options.get("--env").get(1)).isEqualTo("bar=bar2");
    }

    @Test
    public void testFlagsAndOptionsWithArguments() {
        String[] arguments = new String[] {"-q", "--env", "foo=bar", "--env", "bar=bar2"};
        System.out.println("command line [" + String.join(" ", arguments) + "]");

        CommandLineParser commandLineParser = createCommandLineParser(arguments);

        assertThat(commandLineParser.hasFlag("-q")).isTrue();

        Map<String, List<String>> options = commandLineParser.getOptions();

        assertThat(options).hasSize(1);
        assertThat(options).containsKey("--env");
        assertThat(options.get("--env")).hasSize(2);
        assertThat(options.get("--env").get(0)).isEqualTo("foo=bar");
        assertThat(options.get("--env").get(1)).isEqualTo("bar=bar2");
    }

    @Test
    public void testFlagsAndOptionsWithArguments2() {
        String[] arguments = new String[] {"-q", "--env", "foo=bar", "--env", "bar=bar2"};
        System.out.println("command line [" + String.join(" ", arguments) + "]");

        CommandLineParser commandLineParser = createCommandLineParser(arguments);

        assertThat(commandLineParser.hasFlag("-q")).isTrue();

        Map<String, List<String>> options = commandLineParser.getOptions();

        assertThat(options).hasSize(1);
        assertThat(options).containsKey("--env");
        assertThat(options.get("--env")).hasSize(2);
        assertThat(options.get("--env").get(0)).isEqualTo("foo=bar");
        assertThat(options.get("--env").get(1)).isEqualTo("bar=bar2");
    }

    @Test
    public void testOptionsWithArguments2AndFlag() {
        String[] arguments = new String[] {"--env", "foo=bar", "--env", "bar=bar2", "-q"};
        System.out.println("command line [" + String.join(" ", arguments) + "]");

        CommandLineParser commandLineParser = createCommandLineParser(arguments);

        assertThat(commandLineParser.hasFlag("-q")).isTrue();

        Map<String, List<String>> options = commandLineParser.getOptions();

        assertThat(options).hasSize(1);
        assertThat(options).containsKey("--env");
        assertThat(options.get("--env")).hasSize(2);
        assertThat(options.get("--env").get(0)).isEqualTo("foo=bar");
        assertThat(options.get("--env").get(1)).isEqualTo("bar=bar2");
    }

    @Test
    public void testOptionsWithArguments2AndFlag2() {
        String[] arguments = new String[] {"--env", "foo=bar", "--env", "bar=bar2", "-q"};
        System.out.println("command line [" + String.join(" ", arguments) + "]");

        CommandLineParser commandLineParser = createCommandLineParser(arguments);

        assertThat(commandLineParser.hasFlag("-q")).isTrue();

        Map<String, List<String>> options = commandLineParser.getOptions();

        assertThat(options).hasSize(1);
        assertThat(options).containsKey("--env");
        assertThat(options.get("--env")).hasSize(2);
        assertThat(options.get("--env").get(0)).isEqualTo("foo=bar");
        assertThat(options.get("--env").get(1)).isEqualTo("bar=bar2");
    }

    @Test
    public void testComprehensive() {
        String[] arguments = new String[] {"--env", "foo=bar", "--env", "bar=bar2", "-q", "testfile.yaml"};
        System.out.println("command line [" + String.join(" ", arguments) + "]");

        CommandLineParser commandLineParser = createCommandLineParser(arguments);

        assertThat(commandLineParser.hasFlag("-q")).isTrue();

        Map<String, List<String>> options = commandLineParser.getOptions();

        assertThat(options).hasSize(1);
        assertThat(options).containsKey("--env");
        assertThat(options.get("--env")).hasSize(2);
        assertThat(options.get("--env").get(0)).isEqualTo("foo=bar");
        assertThat(options.get("--env").get(1)).isEqualTo("bar=bar2");

        assertThat(commandLineParser.getArguments()).isNotNull();
        // assertThat(commandLineParser.getArguments()).containsExactly("testfile.yaml");
    }

    private static CommandLineParser createCommandLineParser(String[] args) {
        // Set of known flags
        Set<String> knownFlags = Set.of(
                "-v",
                "--version",
                "-i",
                "--info",
                "-h",
                "--help",
                "-t",
                "--trace",
                "-T",
                "--timestamps",
                "-q",
                "--quiet",
                "-qq",
                "--quieter");

        // Set of known options
        Set<String> knownOptions = Set.of("-E", "--env", "-P", "--with");

        // Create the command line parser with the known flags and options
        CommandLineParser commandLineParser = new CommandLineParser(knownFlags, knownOptions);

        // Parse the command line arguments
        commandLineParser.parse(args);

        return commandLineParser;
    }
}
