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

package org.verifyica.pipeliner.engine;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.verifyica.pipeliner.engine.instructions.EvaluateConditional;
import org.verifyica.pipeliner.engine.instructions.EvaluateEnabled;
import org.verifyica.pipeliner.engine.instructions.ExecuteCommand;
import org.verifyica.pipeliner.engine.instructions.Exit;
import org.verifyica.pipeliner.engine.instructions.PopFrame;
import org.verifyica.pipeliner.engine.instructions.PrintFrameStatus;
import org.verifyica.pipeliner.engine.instructions.PushFrame;
import org.verifyica.pipeliner.engine.instructions.SetEnvironmentVariable;
import org.verifyica.pipeliner.engine.instructions.SetShell;
import org.verifyica.pipeliner.engine.instructions.SetVariable;
import org.verifyica.pipeliner.engine.instructions.SetWorkingDirectory;
import org.verifyica.pipeliner.engine.instructions.directives.DirectiveGenerator;
import org.verifyica.pipeliner.model.Job;
import org.verifyica.pipeliner.model.Pipeline;
import org.verifyica.pipeliner.model.Step;
import org.verifyica.pipeliner.support.MultiLineParser;

/**
 * Generator class responsible for generating the Intermediate Representation (IR)
 */
public class Generator {

    /**
     * Generator for directive instructions.
     */
    private final DirectiveGenerator directiveGenerator;

    /**
     * Constructor
     */
    public Generator() {
        this.directiveGenerator = new DirectiveGenerator();
    }

    /**
     * Generates the Intermediate Representation (IR) for the given pipeline.
     *
     * @param pipeline the pipeline to generate IR for
     * @param instructionConsumer the consumer to write the instructions to
     * @throws GeneratorException if an error occurs during generation
     */
    public void generate(Pipeline pipeline, Consumer<Instruction> instructionConsumer) throws GeneratorException {
        Frame.Type type = Frame.Type.PIPELINE;
        String name = pipeline.getName();
        String description = pipeline.getDescription();

        instructionConsumer.accept(PushFrame.of(type, name, description));

        String workingDirectory = pipeline.getWorkingDirectory();
        if (workingDirectory != null) {
            instructionConsumer.accept(SetWorkingDirectory.of(workingDirectory));
        }

        String shell = pipeline.getShell();
        if (shell != null) {
            instructionConsumer.accept(SetShell.of(shell));
        }

        for (Map.Entry<String, String> entry :
                pipeline.getEnvironmentVariables().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            instructionConsumer.accept(SetEnvironmentVariable.of(key, value));
        }

        for (Map.Entry<String, String> entry : pipeline.getVariables().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            instructionConsumer.accept(SetVariable.of(key, value));
        }

        boolean enabled = pipeline.isEnabled();
        instructionConsumer.accept(EvaluateEnabled.of(enabled));

        String conditional = pipeline.getConditional();
        if (conditional != null) {
            instructionConsumer.accept(EvaluateConditional.of(conditional));
        }

        instructionConsumer.accept(PrintFrameStatus.of("running"));

        for (Job job : pipeline.getJobs()) {
            generate(job, instructionConsumer);
        }

        instructionConsumer.accept(PrintFrameStatus.of("success"));
        instructionConsumer.accept(PopFrame.of());
    }

    /**
     * Generates the Intermediate Representation (IR) for the given job.
     *
     * @param job the job to generate IR for
     * @param instructionConsumer the consumer to write the instructions to
     * @throws GeneratorException if an error occurs during generation
     */
    private void generate(Job job, Consumer<Instruction> instructionConsumer) throws GeneratorException {
        Frame.Type type = Frame.Type.JOB;
        String name = job.getName();
        String description = job.getDescription();

        instructionConsumer.accept(PushFrame.of(type, name, description));

        String workingDirectory = job.getWorkingDirectory();
        if (workingDirectory != null) {
            instructionConsumer.accept(SetWorkingDirectory.of(workingDirectory));
        }

        String shell = job.getShell();
        if (shell != null) {
            instructionConsumer.accept(SetShell.of(shell));
        }

        for (Map.Entry<String, String> entry : job.getEnvironmentVariables().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            instructionConsumer.accept(SetEnvironmentVariable.of(key, value));
        }

        for (Map.Entry<String, String> entry : job.getVariables().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            instructionConsumer.accept(SetVariable.of(key, value));
        }

        boolean enabled = job.isEnabled();
        instructionConsumer.accept(EvaluateEnabled.of(enabled));

        String conditional = job.getConditional();
        if (conditional != null) {
            instructionConsumer.accept(EvaluateConditional.of(conditional));
        }

        instructionConsumer.accept(PrintFrameStatus.of("running"));

        for (Step step : job.getSteps()) {
            generate(step, instructionConsumer);
        }

        instructionConsumer.accept(PrintFrameStatus.of("success"));
        instructionConsumer.accept(PopFrame.of());
    }

    /**
     * Generates the Intermediate Representation (IR) for the given step.
     *
     * @param step the step to generate IR for
     * @param instructionConsumer the consumer to write the instructions to
     * @throws GeneratorException if an error occurs during generation
     */
    private void generate(Step step, Consumer<Instruction> instructionConsumer) throws GeneratorException {
        Frame.Type type = Frame.Type.STEP;
        String name = step.getName();
        String description = step.getDescription();

        instructionConsumer.accept(PushFrame.of(type, name, description));

        String workingDirectory = step.getWorkingDirectory();
        if (workingDirectory != null) {
            instructionConsumer.accept(SetWorkingDirectory.of(workingDirectory));
        }

        String shell = step.getShell();
        if (shell != null) {
            instructionConsumer.accept(SetShell.of(shell));
        }

        for (Map.Entry<String, String> entry : step.getEnvironmentVariables().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            instructionConsumer.accept(SetEnvironmentVariable.of(key, value));
        }

        for (Map.Entry<String, String> entry : step.getVariables().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            instructionConsumer.accept(SetVariable.of(key, value));
        }

        boolean enabled = step.isEnabled();
        instructionConsumer.accept(EvaluateEnabled.of(enabled));

        String conditional = step.getConditional();
        if (conditional != null) {
            instructionConsumer.accept(EvaluateConditional.of(conditional));
        }

        instructionConsumer.accept(PrintFrameStatus.of("running"));

        List<String> runCommands = MultiLineParser.parse(step.getRun());

        for (String runCommand : runCommands) {
            generate(runCommand, instructionConsumer);
        }

        instructionConsumer.accept(PrintFrameStatus.of("success"));
        instructionConsumer.accept(PopFrame.of());
    }

    /**
     * Writes the Intermediate Representation (IR) for the given command to the provided BufferedWriter.
     *
     * @param command the command to generate instructions for
     * @param instructionConsumer the consumer to write the instructions to
     * @throws GeneratorException if an error occurs during generation
     */
    private void generate(String command, Consumer<Instruction> instructionConsumer) throws GeneratorException {
        // If the command is null
        if (command == null) {
            // It's null, so ignore it
            return;
        }

        // If the command is empty
        if (command.isBlank()) {
            // It's empty, so ignore it
            return;
        }

        // If the command is a comment, do nothing
        if (command.trim().startsWith("#")) {
            // It's a comment, so ignore it
            return;
        }

        // If the command is a directive, but not a capture directive (special case)
        if (command.startsWith("--") && !command.startsWith("--capture")) {
            // If the command starts with "--", treat it as a directive
            directiveGenerator.generate(command, instructionConsumer);
        } else if (command.trim().startsWith("exit ")) {
            instructionConsumer.accept(Exit.of(command));
        } else {
            // Otherwise, generate a Run instruction for the command
            instructionConsumer.accept(ExecuteCommand.of(command));
        }
    }
}
