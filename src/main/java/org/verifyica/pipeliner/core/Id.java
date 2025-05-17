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

package org.verifyica.pipeliner.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class to implement Id */
public class Id {

    private static final String REGEX = "^[a-zA-Z_]([a-zA-Z0-9-_]*[a-zA-Z0-9_])?$";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private static final Matcher MATCHER = PATTERN.matcher("");

    /**
     * Constructor
     */
    private Id() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to return if an id name is valid
     *
     * @param name the name
     * @return true of the id name is valid, else false
     */
    public static boolean isValid(String name) {
        return MATCHER.reset(name).matches();
    }

    /**
     * Method to return an id name is invalid
     *
     * @param name the name
     * @return true if the id name is invalid, else false
     */
    public static boolean isInvalid(String name) {
        return !isValid(name);
    }
}
