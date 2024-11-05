package org.verifyica.pipelines.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.UUID;

public class Step {

    private String id;
    private String name;
    private boolean enabled;
    private String workingDirectory;
    private String command;
    private int exitCode;

    public Step() {
        initialize();
    }

    private void initialize() {
        id = UUID.randomUUID().toString();
        name = id;
        enabled = true;
        workingDirectory = ".";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void execute(PrintStream outPrintStream, PrintStream errorPrintStream) {
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Set the command and working directory
        processBuilder.command("bash", "-c", getCommand());
        processBuilder.directory(new File(getWorkingDirectory()));

        try {
            // Start the process
            Process process = processBuilder.start();

            // Capture standard output
            try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String line;

                while ((line = outputReader.readLine()) != null) {
                    outPrintStream.println(line);
                }

                while ((line = errorReader.readLine()) != null) {
                    errorPrintStream.println(line);
                }
            }

            setExitCode(process.waitFor());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(errorPrintStream);
            setExitCode(1);
        }
    }

    @Override
    public String toString() {
        return "Step {" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", workingDirectory='" + workingDirectory + '\'' +
                ", command='" + command + '\'' +
                '}';
    }
}
