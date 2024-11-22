/*
 * Copyright (C) 2024-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.common;

import static java.lang.String.format;

import java.util.function.Supplier;

/** Class to implement MessageSupplier */
public class MessageSupplier implements Supplier<String> {

    private final String string;

    /**
     * Constructor
     *
     * @param string string
     */
    public MessageSupplier(String string) {
        this.string = string;
    }

    /**
     * Constructor
     *
     * @param format format
     * @param objects objects
     */
    public MessageSupplier(String format, Object... objects) {
        string = format(format, objects);
    }

    @Override
    public String get() {
        return string;
    }

    /**
     * Method to create a MessageSupplier
     *
     * @param string string
     * @return a MessageSupplier
     */
    public static MessageSupplier of(String string) {
        return new MessageSupplier(string);
    }

    /**
     * Method to create a MessageSupplier
     *
     * @param format format
     * @param objects objects
     * @return a MessageSupplier
     */
    public static MessageSupplier of(String format, Object... objects) {
        return new MessageSupplier(format, objects);
    }
}
