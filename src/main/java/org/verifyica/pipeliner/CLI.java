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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.verifyica.pipeliner.engine.Context;
import org.verifyica.pipeliner.engine.Engine;
import org.verifyica.pipeliner.support.CommandLineParser;
import org.verifyica.pipeliner.support.HumanDuration;
import org.verifyica.pipeliner.support.SetOf;
import org.verifyica.pipeliner.support.Stopwatch;

/**
 * Command Line Interface (CLI) for the Pipeliner engine.
 */
public class CLI {

    /**
     * Set of known flags
     */
    Set<String> knownFlags = SetOf.of("-h", "--help", "-i", "--info", "-ts", "--timestamps", "-v", "--version");

    /**
     * Set of known options
     */
    Set<String> knownOptions = SetOf.of("-E", "--env", "-P", "--with");

    /**
     * Console for output
     */
    private final Console console;

    /**
     * Command line parser for parsing command line arguments
     */
    private CommandLineParser commandLineParser;

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
    }

    /**
     * Executes the CLI with the provided arguments.
     *
     * @param args the command line arguments
     * @return the exit code
     */
    public int execute(String[] args) {
        // Create a stopwatch to measure execution time
        Stopwatch stopwatch = new Stopwatch();

        // Check if the arguments are null or empty
        if (args == null || args.length == 0) {
            // Show usage information
            showUsage();

            // Return error exit code
            return 1;
        }

        // Initialize the command line parser
        initializeCommandLineParser(args);

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

        // Get the list of filenames from the command line arguments
        List<String> filenames = getFilenames();

        // If no filenames are provided
        if (filenames.isEmpty()) {
            // Show usage information
            showUsage();

            // Return error exit code
            return 1;
        }

        // TODO handle multiple filenames

        // Get the first pipeline filename
        File file = new File(filenames.get(0));

        // Print the version and project URL
        console.info("Pipeliner %s (%s)", Version.getVersion(), Constants.PIPELINER_PROJECT_URL);

        // Print the file being processed
        console.info("file [%s]", getRelativeFilename(file));

        // Validate the file
        validateFile(file);

        // Create the context for execution
        Context context = new Context(console);

        // Set environment variables from the environment
        context.getEnvironmentVariables().putAll(System.getenv());

        // TODO check/read IPC variables

        // TODO set environment variables from command line options

        // TODO set variables from command line options

        // Create the engine for execution
        Engine engine = new Engine(context);

        // Execute the engine and return the exit code
        int exitCode = engine.execute(file);

        // Get the elapsed time  as human-readable duration
        String humanDuration = HumanDuration.humanDuration(stopwatch.elapsedTime());

        // Create the message to print
        String message = String.format(
                "Pipeliner %s exit-code=[%d] duration=[%s]", Version.getVersion(), exitCode, humanDuration);

        if (exitCode == 0) {
            // Print the exit code and duration
            console.info(message);
        } else {
            // Print the exit code and duration
            console.error(message);
        }

        // Return the exit code
        return exitCode;
    }

    /**
     * Initialize the command line parser with the known flags and options.
     *
     * @param args the command line arguments
     */
    private void initializeCommandLineParser(String[] args) {
        // Create the command line parser with the known flags and options
        commandLineParser = new CommandLineParser(knownFlags, knownOptions);

        try {
            // Parse the command line arguments
            commandLineParser.parse(args);
        } catch (IllegalArgumentException e) {
            console.println("Error:");
            console.println();
            console.println("  %s", e.getMessage());
            console.println();

            // Show usage information
            showUsage();

            // Return error exit code
            System.exit(1);
        }
    }

    /**
     * Process the version flags and print the version if requested.
     */
    private void processVersionFlags() {
        // Check if the -v or --version flag is present
        if (commandLineParser.hasFlag("-v") || commandLineParser.hasFlag("--version")) {
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
        if (commandLineParser.hasFlag("-i") || commandLineParser.hasFlag("--info")) {
            // Print the verbose information
            console.println("Pipeliner %s (%s)", Version.getVersion(), Constants.PIPELINER_PROJECT_URL);

            // Exit the program successfully
            System.exit(0);
        }
    }

    /**
     * Process the help flags and show usage information if requested.
     */
    private void processHelpFlags() {
        // Check if the -h or --help flag is present
        if (commandLineParser.hasFlag("-h") || commandLineParser.hasFlag("--help")) {
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
        // If the -T or --timestamps flag is present
        if (commandLineParser.hasFlag("-ts") || commandLineParser.hasFlag("--timestamps")) {
            // Enable timestamps in the console output
            console.setEnableTimestamps(true);
        }
    }

    /**
     * Get the filenames from the command line arguments.
     *
     * @return a list of filenames
     */
    private List<String> getFilenames() {
        // Get the filenames from the command line arguments
        return commandLineParser.getArguments();
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
            console.error("file [%s] does not exist", file.getName());

            // Print the exit code and duration
            console.error("Pipeliner %s exit-code=[%d]", Version.getVersion(), 1);

            // Exit the program with an error
            System.exit(1);
        }

        // Check if the file is readable
        if (!file.canRead()) {
            // If the file does not exist
            console.error("file [%s] is not readable", file.getName());

            // Print the exit code and duration
            console.error("Pipeliner %s exit-code=[%d]", Version.getVersion(), 1);

            // Exit the program with an error
            System.exit(1);
        }

        // Check if the file is a regular file
        if (!file.isFile()) {
            // If the file does not exist
            console.error("file [%s] is not a file", file.getName());

            // Print the exit code and duration
            console.error("Pipeliner %s exit-code=[%d]", Version.getVersion(), 1);

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
        console.println("  -i, --info        print information");
        console.println("  -v, --version     print version number (no newline)");
        console.println("  -ts, --timestamps  enable output timestamps");
        console.println("  -h, --help        print usage");
        // console.println("  -q, --quiet       set verbosity to quiet");
        // console.println("  -qq, --quieter    set verbosity to quieter");
        console.println();
    }
}
