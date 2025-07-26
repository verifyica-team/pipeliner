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

package org.verifyica.pipeliner.core.parser.io;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import org.verifyica.pipeliner.exception.SyntaxException;
import org.verifyica.pipeliner.exception.UncheckedException;

/**
 * Reader that reads lines from a DSL source, stripping comments and empty lines.
 */
public class LineReader implements AutoCloseable {

    private static final int MAXIMUM_LINE_LENGTH = 1024;

    private final LineNumberReader lineNumberReader;

    /**
     * Constructor
     *
     * @param reader the reader to read lines from
     */
    private LineReader(Reader reader) {
        this.lineNumberReader = new LineNumberReader(new BoundedBufferedReader(reader, MAXIMUM_LINE_LENGTH));
    }

    /**
     * Gets the current line number.
     *
     * @return the current line number, starting from 1
     */
    public int getLineNumber() {
        return lineNumberReader.getLineNumber();
    }

    /**
     * Reads a line from the input, stripping comments and empty lines.
     *
     * @return the next line of code, or null if there are no more lines
     */
    public String readLine() {
        try {
            String line;
            while ((line = lineNumberReader.readLine()) != null) {
                if (stripComments(line)) {
                    continue;
                }

                return line;
            }

            return null;
        } catch (IOException e) {
            throw new UncheckedException(e);
        }
    }

    @Override
    public void close() {
        try {
            lineNumberReader.close();
        } catch (IOException e) {
            throw new UncheckedException(e);
        }
    }

    /**
     * Factory method to create a LineReader from a Reader.
     *
     * @param reader the reader to read lines from
     * @return a new LineReader instance
     */
    public static LineReader of(Reader reader) {
        return new LineReader(reader);
    }

    /**
     * Strips comments from a file.
     *
     * @param line the line to check for comments
     * @return true if the line is a comment or empty, false if it contains code
     * @throws IOException if an I/O error occurs while reading from the BufferedReader
     */
    private boolean stripComments(String line) throws IOException {
        String trimmedLine = line.trim();

        if (trimmedLine.isEmpty()) {
            return true;
        }

        if (trimmedLine.startsWith("#") || trimmedLine.startsWith("//")) {
            return true;
        }

        if (trimmedLine.startsWith("/*") && trimmedLine.endsWith("*/")) {
            return true;
        }

        boolean inBlockComment = trimmedLine.startsWith("/*");
        if (!inBlockComment) {
            return false;
        }

        while ((line = lineNumberReader.readLine()) != null) {
            trimmedLine = line.trim();
            if (trimmedLine.endsWith("*/")) {
                return true;
            }
        }

        throw new SyntaxException(null, "unterminated block comment");
    }
}
