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

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Pattern;

/** Class to implement PathResolver */
public class PathResolver {

    /** Constructor */
    private PathResolver() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to resolve a path
     *
     * @param absolutePath
     * @return the resolved path
     */
    public static String resolvePath(String absolutePath) {
        String[] tokens = absolutePath.split(Pattern.quote(File.separator));
        Deque<String> stack = new ArrayDeque<>();

        for (String token : tokens) {
            if (token.equals("..")) {
                if (!stack.isEmpty()) {
                    stack.pop();
                }
            } else if (!token.equals(".") && !token.isEmpty()) {
                stack.push(token);
            }
        }

        StringBuilder resolvedPath = new StringBuilder();
        while (!stack.isEmpty()) {
            resolvedPath.insert(0, stack.pop()).insert(0, File.separator);
        }

        return absolutePath.startsWith(File.separator) ? resolvedPath.toString() : resolvedPath.substring(1);
    }
}
