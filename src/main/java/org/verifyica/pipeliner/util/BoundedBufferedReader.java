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

package org.verifyica.pipeliner.util;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * A BufferedReader that enforces a maximum line length.
 * Ignores carriage return characters.
 * If any line exceeds the limit, a {@link LineTooLongException} is thrown.
 */
public final class BoundedBufferedReader extends BufferedReader {

    private final int maxLineLength;

    /**
     * Constructs a BoundedBufferedReader with the default buffer size.
     *
     * @param in the underlying reader
     * @param maxLineLength maximum allowed line length (not including line terminator)
     */
    public BoundedBufferedReader(Reader in, int maxLineLength) {
        super(in);
        this.maxLineLength = maxLineLength;
    }

    /**
     * Constructs a BoundedBufferedReader with the specified buffer size.
     *
     * @param in the underlying reader
     * @param bufferSize internal buffer size
     * @param maxLineLength maximum allowed line length (not including line terminator)
     */
    public BoundedBufferedReader(Reader in, int bufferSize, int maxLineLength) {
        super(in, bufferSize);
        this.maxLineLength = maxLineLength;
    }

    /**
     * Reads a line and enforces the maximum length constraint.
     *
     * @return the line read, or {@code null} if end of stream
     * @throws IOException if an I/O error occurs
     * @throws LineTooLongException if line exceeds {@code maxLineLength}
     */
    @Override
    public String readLine() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        int c;
        while ((c = read()) != -1) {
            if (c == '\r') {
                // Ignore carriage return
                continue;
            }

            if (c == '\n') {
                break;
            }

            if (stringBuilder.length() >= maxLineLength) {
                throw new LineTooLongException("Line exceeds maximum length of " + maxLineLength);
            }

            stringBuilder.append((char) c);
        }

        if (c == -1 && stringBuilder.length() == 0) {
            return null;
        }

        return stringBuilder.toString();
    }

    /**
     * Represents an exception that is thrown when a line exceeds the maximum allowed length.
     */
    public static final class LineTooLongException extends IOException {

        /**
         * Constructor
         *
         * @param message the detail message
         */
        public LineTooLongException(String message) {
            super(message);
        }
    }
}
