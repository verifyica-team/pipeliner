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
import org.verifyica.pipeliner.support.ConditionalEvaluator;
import org.verifyica.pipeliner.support.PeekIterator;

/**
 * Instruction to set the type of the current frame
 */
public class EvaluateConditional implements Instruction {

    /**
     * The logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluateConditional.class);

    /**
     * The expression to evaluate
     */
    private final String expression;

    /**
     * Constructor
     *
     * @param expression the conditional
     */
    private EvaluateConditional(String expression) {
        this.expression = expression.trim();
    }

    @Override
    public void execute(Context context, PeekIterator<Instruction> peekIterator) throws Throwable {
        LOGGER.trace("execute()");
        LOGGER.trace("expression = [%s]", expression);

        // Resolve all variables in the expression
        String resolvedExpression = context.resolveAllVariables(expression);

        LOGGER.trace("resolvedExpression = [%s]", resolvedExpression);

        // Evaluate the resolved expression
        boolean enabled = ConditionalEvaluator.getInstance().evaluate(resolvedExpression);

        LOGGER.trace("enabled = [%s]", enabled);

        if (!enabled) {
            Frame currentFrame = context.getFrame();

            context.getConsole().println("%s status=[%s]", currentFrame.toConsoleString(), "disabled");

            int depth = 0;

            while (peekIterator.hasNext()) {
                Instruction instruction = peekIterator.peek();

                if (instruction instanceof PushFrame) {
                    PushFrame nestedFrame = (PushFrame) instruction;

                    // Manually print the disabled status of the nested frame
                    Frame.Type type = nestedFrame.getType();
                    String name = nestedFrame.getName();
                    String description = nestedFrame.getDescription();

                    Frame frame = new Frame(type);
                    frame.setName(name);
                    frame.setDescription(description);

                    context.getConsole().println("%s status=[%s]", frame.toConsoleString(), "disabled");

                    depth++;
                } else if (instruction instanceof PopFrame) {
                    if (depth == 0) {
                        break;
                    }
                    depth--;
                }

                peekIterator.next();
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + " { expression [" + expression + "] }";
    }

    /**
     * Factory method to create a new instance of EvaluateConditional.
     *
     * @param expression the expression
     * @return a new EvaluateConditional instance
     */
    public static EvaluateConditional of(String expression) {
        return new EvaluateConditional(expression);
    }
}
