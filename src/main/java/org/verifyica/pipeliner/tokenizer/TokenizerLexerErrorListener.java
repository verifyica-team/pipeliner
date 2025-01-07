/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.tokenizer;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Class to implement ErrorListener
 */
class TokenizerLexerErrorListener extends ConsoleErrorListener {

    private final List<String> errors;

    /**
     * Constructor
     */
    public TokenizerLexerErrorListener() {
        errors = new ArrayList<>();
    }

    /**
     * Method to get the errors
     *
     * @return the errors
     */
    public List<String> getErrors() {
        return errors;
    }

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int characterPositionInLine,
            String message,
            RecognitionException recognitionException) {
        errors.add(
                format("syntax error at line [%d] position [%d] error [%s]", line, characterPositionInLine, message));
    }
}
