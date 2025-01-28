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

package org.verifyica.pipeliner.parser.tokens;

/** Class to implement TextToken */
public class ParsedText extends Token {

    /**
     * Constructor
     *
     * @param position the position
     * @param text the text
     */
    public ParsedText(int position, String text) {
        super(Type.TEXT, position, text);
    }

    /**
     * Method to get the value
     *
     * @return the value
     */
    public String getValue() {
        return getText();
    }

    /**
     * Method to create a new text token
     *
     * @param text the text
     * @return a new text token
     */
    public static ParsedText create(String text) {
        return create(-1, text);
    }

    /**
     * Method to create a new text token
     *
     * @param text the text
     * @param value the value
     * @return a new text token
     */
    public static ParsedText create(String text, String value) {
        if (!text.equals(value)) {
            throw new IllegalArgumentException("Text and value must be the same");
        }

        return create(-1, text);
    }

    /**
     * Method to create a new text token
     *
     * @param position the position
     * @param text the text
     * @return a new text token
     */
    public static ParsedText create(int position, String text) {
        return new ParsedText(position, text);
    }
}
