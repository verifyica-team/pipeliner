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

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.verifyica.pipeliner.exception.HaltException;
import org.verifyica.pipeliner.exception.SyntaxException;
import org.verifyica.pipeliner.util.BoundedBufferedReader;
import org.verifyica.pipeliner.util.EnvironmentVariableName;
import org.verifyica.pipeliner.util.Stopwatch;
import org.verifyica.pipeliner.util.VariableName;

/**
 * Command Line Interface (CLI) for the Pipeliner engine.
 */
public class CLI {

    /**
     * Console for output
     */
    private final Console console;

    /**
     * The command line after parsing the arguments
     */
    private CommandLine commandLine;

    /**
     * Environment variables provided via the command line.
     */
    private final Map<String, String> commandLineEnvironmentVariables;

    /**
     * Variables provided via the command line.
     */
    private final Map<String, String> commandLineVariables;

    /**
     * Main method to run the Pipeline engine
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Create a new CLI instance
        CLI cli = new CLI();

        // Execute the CLI with the provided arguments
        int exitCode = cli.execute(args);

        // Exit the program with the exit code
        System.exit(exitCode);
    }

    /**
     * Constructor
     */
    public CLI() {
        this.console = new Console();
        this.commandLineEnvironmentVariables = new LinkedHashMap<>();
        this.commandLineVariables = new LinkedHashMap<>();
    }

    /**
     * Executes the CLI with the provided arguments.
     *
     * @param arguments the command line arguments
     * @return the exit code
     */
    public int execute(String[] arguments) {
        // Create a stopwatch to measure execution time
        Stopwatch stopwatch = new Stopwatch();

        // Check if the arguments are null or empty
        if (arguments == null || arguments.length == 0) {
            // Show usage information
            showUsage();

            // Return error exit code
            return 1;
        }

        // Initialize the command line parser
        initializeCommandLine(arguments);

        // Process help flags
        processHelpFlags();

        // Process version flags
        processVersionFlags();

        // Process information flags
        processInformationFlags();

        // Process help flags
        processHelpFlags();

        // Process verbosity flags
        processTimestampFlags();

        // Process environment variables
        processEnvironmentVariables();

        // Process variables
        processVariables();

        // Get the list of filenames from the command line arguments
        List<String> filenames = getFilenames();

        // If no filenames are provided
        if (filenames.isEmpty()) {
            // Show usage information
            showUsage();

            // Return error exit code
            return 1;
        }

        // Get the first pipeline filename
        File file = new File(filenames.get(0));

        // Print the banner
        console.println("******************************************");
        console.println("* Pipeliner " + Version.getVersion() + "                        *");
        console.println("* " + Constants.PIPELINER_PROJECT_URL + " *");
        console.println("******************************************");
        console.println("# file [%s]", getRelativeFilename(file));

        // Validate the file
        validateFile(file);

        // Create the context for execution
        Context context = new Context(console);

        // Copy the command line environment variables to the context
        context.currentScope().putAllEnvironmentVariables(commandLineEnvironmentVariables);

        // Set environment variables from the environment
        context.currentScope().putAllEnvironmentVariables(System.getenv());

        // Copy the command line variables to the context
        context.currentScope().putAllVariables(commandLineVariables);

        int exitCode = 0;

        try (Reader reader = new BoundedBufferedReader(new FileReader(arguments[0]), 1024)) {
            Script.create(reader).execute(context);
        } catch (SyntaxException e) {
            exitCode = 1;
            context.println("Syntax error: %s", e.getMessage());
        } catch (HaltException e) {
            exitCode = e.getExitCode();
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            exitCode = 1;
        }

        // Return the exit code
        return exitCode;
    }

    /**
     * Initialize the command line parser with the known flags and options.
     *
     * @param args the command line arguments
     */
    private void initializeCommandLine(String[] args) {
        // Create Options for the command line parser
        Options options = new Options();

        // Add an option for information
        options.addOption(
                Option.builder("i").longOpt("info").desc("show information").build());

        // Add an option for version
        options.addOption(
                Option.builder("v").longOpt("version").desc("show version").build());

        // Add an option for timestamps
        options.addOption(Option.builder("ts")
                .longOpt("timestamps")
                .desc("enable output timestamps")
                .build());

        // Add an option for environment variables
        options.addOption(Option.builder("E")
                .longOpt("env")
                .desc("command line environment variable")
                .hasArg(true)
                .valueSeparator('=')
                .numberOfArgs(1)
                .build());

        // Add an option for variables
        options.addOption(Option.builder("V")
                .longOpt("with")
                .desc("command line variable")
                .hasArg(true)
                .valueSeparator('=')
                .numberOfArgs(1)
                .build());

        // Add an option for help
        options.addOption(Option.builder("h").longOpt("help").desc("show usage").build());

        // Create a command line parser
        CommandLineParser commandLineParser = new DefaultParser();

        try {
            // Parse the command line arguments
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace(System.out);

            // Show usage information
            showUsage();

            // Exit the program with an error code
            System.exit(1);
        }
    }

    /**
     * Process the version flags and print the version if requested.
     */
    private void processVersionFlags() {
        // Check if the -v or --version flag is present
        if (commandLine.hasOption("v") || commandLine.hasOption("version")) {
            // Print the version
            console.print(Version.getVersion());

            // Exit the program successfully
            System.exit(0);
        }
    }

    /**
     * Process the information flags if requested.
     */
    private void processInformationFlags() {
        // Check if the -i or --info flag is present
        if (commandLine.hasOption("i") || commandLine.hasOption("info")) {
            // Print the banner
            // Print the banner
            console.println("******************************************");
            console.println("* Pipeliner " + Version.getVersion() + "                        *");
            console.println("* " + Constants.PIPELINER_PROJECT_URL + " *");
            console.println("******************************************");

            // Exit the program successfully
            System.exit(0);
        }
    }

    /**
     * Process the help flags and show usage information if requested.
     */
    private void processHelpFlags() {
        // Check if the -h or --help flag is present
        if (commandLine.hasOption("h") || commandLine.hasOption("help")) {
            // Show usage information
            showUsage();

            // Exit the program successfully
            System.exit(0);
        }
    }

    /**
     * Process the timestamp flags
     */
    private void processTimestampFlags() {
        // If the -ts or --timestamps flag is present
        if (commandLine.hasOption("-ts") || commandLine.hasOption("--timestamps")) {
            // Enable timestamps in the console output
            console.setEnableTimestamps(true);
        }
    }

    /**
     * Process the command line options for environment variables.
     */
    private void processEnvironmentVariables() {
        // Process the command line options for environment variables
        commandLine.getOptionProperties("E").forEach((k, v) -> {
            String name = (String) k;

            if (!name.contains("=")) {
                // Print the banner
                console.println("info: Pipeliner %s (%s)", Version.getVersion(), Constants.PIPELINER_PROJECT_URL);

                // Print an error message for invalid command line environment variable
                console.println("error: command line environment variable [%s] is invalid", name);

                // Print the exit code
                console.println("error: Pipeliner %s exit-code %d", Version.getVersion(), 1);

                // Exit the program with an error code
                System.exit(1);
            }

            String[] tokens = name.split("=", 2);
            name = tokens[0];
            String value = "";
            if (tokens.length == 2) {
                value = tokens[1];
            }

            if (EnvironmentVariableName.isInvalid(name)) {
                // Print the banner
                console.println("info: Pipeliner %s (%s)", Version.getVersion(), Constants.PIPELINER_PROJECT_URL);

                // Print an error message for invalid command line environment variable
                console.println("error: command line environment variable [%s] is invalid", name);

                // Print the exit code
                console.println("error: Pipeliner %s exit-code %d", Version.getVersion(), 1);

                // Exit the program with an error code
                System.exit(1);
            }

            commandLineEnvironmentVariables.put(name, value);
        });
    }

    /**
     * Process the command line options for variables.
     */
    private void processVariables() {
        // Process the command line options for variables
        commandLine.getOptionProperties("V").forEach((k, v) -> {
            String name = (String) k;

            if (!name.contains("=")) {
                // Print the banner
                console.println("info: Pipeliner %s (%s)", Version.getVersion(), Constants.PIPELINER_PROJECT_URL);

                // Print an error message for invalid command line variable
                console.println("error: command line variable [%s] is invalid", name);

                // Print the exit code
                console.println("error: Pipeliner %s exit-code %d", Version.getVersion(), 1);

                // Exit the program with an error code
                System.exit(1);
            }

            String[] tokens = name.split("=", 2);
            name = tokens[0];
            String value = "";
            if (tokens.length == 2) {
                value = tokens[1];
            }

            if (VariableName.isInvalid(name)) {
                // Print the banner
                console.println("info: Pipeliner %s (%s)", Version.getVersion(), Constants.PIPELINER_PROJECT_URL);

                // Print an error message for invalid command line variable
                console.println("error: command line variable [%s] is invalid", name);

                // Print the exit code
                console.println("error: Pipeliner %s exit-code %d", Version.getVersion(), 1);

                // Exit the program with an error code
                System.exit(1);
            }

            commandLineVariables.put(name, value);
        });
    }

    /**
     * Get the filenames from the command line arguments.
     *
     * @return a list of filenames
     */
    private List<String> getFilenames() {
        // Get the filenames from the command line arguments
        return commandLine.getArgList();
    }

    /**
     * Get the relative filename of the specified file based on the current working directory.
     *
     * @param file the file
     * @return the relative filename or absolute path if the file is not in the current working directory
     */
    public static String getRelativeFilename(File file) {
        // Get the current working directory as an absolute path
        Path currentWorkingDirectory = Paths.get(".").toAbsolutePath().normalize();

        // Get the target file's absolute path
        Path target = file.getAbsoluteFile().toPath().normalize();

        // If the current working directory's root is the same as the target's root,
        if (currentWorkingDirectory.getRoot() != null
                && currentWorkingDirectory.getRoot().equals(target.getRoot())) {
            // Return the relative path from the current working directory to the target file
            return currentWorkingDirectory.relativize(target).toString();
        }

        // Return the absolute path of the target file
        return target.toString();
    }

    /**
     * Validate the provided file.
     *
     * @param file the file to validate
     */
    private void validateFile(File file) {
        if (!file.exists()) {
            // If the file does not exist
            console.println("error: file [%s] does not exist", file.getName());

            // Print the exit code and duration
            console.println("error: Pipeliner %s exit-code %d", Version.getVersion(), 1);

            // Exit the program with an error
            System.exit(1);
        }

        // Check if the file is readable
        if (!file.canRead()) {
            // If the file does not exist
            console.println("error: file [%s] is not readable", file.getName());

            // Print the exit code and duration
            console.println("error: Pipeliner %s exit-code %d", Version.getVersion(), 1);

            // Exit the program with an error
            System.exit(1);
        }

        // Check if the file is a regular file
        if (!file.isFile()) {
            // If the file does not exist
            console.println("error: file [%s] is not a file", file.getName());

            // Print the exit code and duration
            console.println("error: Pipeliner %s exit-code %d", Version.getVersion(), 1);

            // Exit the program with an error
            System.exit(1);
        }
    }

    /**
     * Show usage information for the CLI.
     */
    private void showUsage() {
        console.setEnableTimestamps(false);
        console.println("Usage:");
        console.println();
        console.println("  pipeliner [options] <pipeline-file>");
        console.println();
        console.println("Options:");
        console.println();
        console.println("  -i, --info                 print information");
        console.println("  -v, --version              print version number (no newline)");
        // console.println("  -ts, --timestamps          enable output timestamps");
        console.println("  -h, --help                 print usage");
        console.println("  -E, --env <name>=<value>   set an environment variable");
        console.println("  -V, --var <name>=<value>   set a variable");
        console.println();
    }
}
