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

/** Class to implement Crlf */
public class Crlf {

    /** Constructor */
    private Crlf() {
        // INTENTIONALLY BLANK
    }

    /**
     * Escapes any CRLF (\r\n) in a string by replacing it with \\r\\n.
     *
     * @param string the string to escape
     * @return the escaped string
     */
    public static String escapeCRLF(String string) {
        if (string == null) {
            return null;
        }

        return string.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n");
    }

    /**
     * Unescapes \\r\\n in a string to actual CRLF (\r\n).
     *
     * @param value the string to unescape
     * @return the unescaped string
     */
    public static String unescapeCRLF(String value) {
        if (value == null) {
            return null;
        }

        return value.replace("\\n", "\n").replace("\\r", "\r").replace("\\\\", "\\");
    }
}
