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

package org.verifyica.pipeliner.common.io;

import java.io.PrintStream;

/**  Class to implement PrefixedPrintStream */
public class PrefixedPrintStream extends PrintStream {

    private final String prefix;
    private boolean atLineStart = true;

    /**
     * Constructor
     *
     * @param printStream the print stream
     * @param prefix the prefix
     */
    public PrefixedPrintStream(PrintStream printStream, String prefix) {
        super(printStream);

        if (prefix == null) {
            throw new IllegalArgumentException("prefix is null");
        }

        this.prefix = prefix;
    }

    /**
     * Method to get the prefix
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Method to print the prefix if at line start
     */
    private void printPrefix() {
        if (atLineStart) {
            super.print(prefix);
            atLineStart = false;
        }
    }

    @Override
    public void println() {
        super.println();
        atLineStart = true;
    }

    @Override
    public void print(String s) {
        if (s == null) {
            super.print(s);
            return;
        }

        String[] lines = s.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            printPrefix();
            super.print(lines[i]);
            if (i < lines.length - 1) {
                super.println();
                atLineStart = true;
            }
        }
    }

    @Override
    public void println(String s) {
        if (s == null) {
            printPrefix();
            super.println(s);
            atLineStart = true;
            return;
        }

        String[] lines = s.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            printPrefix();
            super.print(lines[i]);
            super.println();
            atLineStart = true;
        }
    }

    @Override
    public void print(boolean b) {
        printPrefix();
        super.print(b);
    }

    @Override
    public void print(char c) {
        printPrefix();
        super.print(c);
    }

    @Override
    public void print(int i) {
        printPrefix();
        super.print(i);
    }

    @Override
    public void print(long l) {
        printPrefix();
        super.print(l);
    }

    @Override
    public void print(float f) {
        printPrefix();
        super.print(f);
    }

    @Override
    public void print(double d) {
        printPrefix();
        super.print(d);
    }

    @Override
    public void print(char[] s) {
        print(new String(s));
    }

    @Override
    public void print(Object o) {
        printPrefix();
        super.print(o);
    }

    @Override
    public void println(boolean b) {
        printPrefix();
        super.println(b);
        atLineStart = true;
    }

    @Override
    public void println(char c) {
        printPrefix();
        super.println(c);
        atLineStart = true;
    }

    @Override
    public void println(int i) {
        printPrefix();
        super.println(i);
        atLineStart = true;
    }

    @Override
    public void println(long l) {
        printPrefix();
        super.println(l);
        atLineStart = true;
    }

    @Override
    public void println(float f) {
        printPrefix();
        super.println(f);
        atLineStart = true;
    }

    @Override
    public void println(double d) {
        printPrefix();
        super.println(d);
        atLineStart = true;
    }

    @Override
    public void println(char[] s) {
        printPrefix();
        super.println(new String(s));
        atLineStart = true;
    }

    @Override
    public void println(Object obj) {
        printPrefix();
        super.println(obj);
        atLineStart = true;
    }

    @Override
    public void flush() {
        super.flush();
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public boolean checkError() {
        return super.checkError();
    }
}
