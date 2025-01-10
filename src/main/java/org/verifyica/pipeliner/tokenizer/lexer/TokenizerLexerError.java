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

package org.verifyica.pipeliner.tokenizer.lexer;

/** Class to implement TokenizerLexerError */
public class TokenizerLexerError {

    private final int position;
    private final String message;

    /**
     * Constructor
     *
     * @param position position
     * @param message message
     */
    public TokenizerLexerError(int position, String message) {
        this.position = position;
        this.message = message;
    }

    /**
     * Method to get the position
     *
     * @return position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Method to get the message
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }
}
