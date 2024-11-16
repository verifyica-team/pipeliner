/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.pipeline.common;

/** Class to implement YamlFormatException */
public class YamlFormatException extends RuntimeException {

    /**
     * Constructor
     *
     * @param message message
     */
    public YamlFormatException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param message message
     * @param throwable throwable
     */
    public YamlFormatException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
