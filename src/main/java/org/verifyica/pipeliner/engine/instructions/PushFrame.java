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

package org.verifyica.pipeliner.engine.instructions;

import org.verifyica.pipeliner.engine.Context;
import org.verifyica.pipeliner.engine.Frame;
import org.verifyica.pipeliner.engine.Instruction;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.support.PeekIterator;

/**
 * Instruction to push a new frame onto the context's stack.
 */
public class PushFrame implements Instruction {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PushFrame.class);

    /**
     * The type of the frame to push.
     */
    private final Frame.Type type;

    /**
     * The name of the frame to push.
     */
    private final String name;

    /**
     * The description of the frame to push.
     */
    private final String description;

    /**
     * Constructor
     *
     * @param type the type of the frame
     * @param name the name of the frame
     * @param description the description of the frame
     */
    private PushFrame(Frame.Type type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }

    /**
     * Get the name of the frame.
     *
     * @return the name of the frame
     */
    public Frame.Type getType() {
        return type;
    }

    /**
     * Get the name of the frame.
     *
     * @return the name of the frame
     */
    public String getName() {
        return name;
    }

    /**
     * Get the description of the frame.
     *
     * @return the description of the frame
     */
    public String getDescription() {
        return description;
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("type = [%s]", type);
        LOGGER.trace("name = [%s]", name);
        LOGGER.trace("description = [%s]", description);

        // Create a new frame of the specified type
        Frame frame = new Frame(type);

        // Set the name of the frame
        frame.setName(name);

        // Set the description of the frame
        frame.setDescription(description);

        // Push a new frame onto the context's stack
        context.pushFrame(frame);
    }

    @Override
    public String toString() {
        return getClass().getName() + " { type [" + type + "] name [" + name + "] description [" + description + "] }";
    }

    /**
     * Factory method to create a new instance of PushFrame.
     *
     * @param type the type of the frame
     * @param name the name of the frame
     * @param description the description of the frame
     * @return a new PushFrame instance
     */
    public static PushFrame of(Frame.Type type, String name, String description) {
        return new PushFrame(type, name, description);
    }
}
