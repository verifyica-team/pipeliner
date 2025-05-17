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

package org.verifyica.pipeliner.common.io;

import java.io.PrintStream;

/** Class to implement StringPrintStream */
public class StringPrintStream extends PrintStream {

    private final StringBuilder stringBuilder;

    /**
     * Constructor
     */
    public StringPrintStream() {
        this(new StringBuilder());
    }

    /**
     * Constructor
     *
     * @param stringBuilder the string builder
     */
    public StringPrintStream(StringBuilder stringBuilder) {
        super(new NoOpPrintStream());
        this.stringBuilder = stringBuilder;
    }

    /**
     * Method to get string
     *
     * @return the string
     */
    public String getString() {
        return stringBuilder.toString();
    }

    @Override
    public void print(boolean b) {
        stringBuilder.append(b);
    }

    @Override
    public void print(char c) {
        stringBuilder.append(c);
    }

    @Override
    public void print(int i) {
        stringBuilder.append(i);
    }

    @Override
    public void print(long l) {
        stringBuilder.append(l);
    }

    @Override
    public void print(float f) {
        stringBuilder.append(f);
    }

    @Override
    public void print(double d) {
        stringBuilder.append(d);
    }

    @Override
    public void print(char[] s) {
        stringBuilder.append(s);
    }

    @Override
    public void print(String s) {
        stringBuilder.append(s);
    }

    @Override
    public void print(Object obj) {
        stringBuilder.append(obj);
    }

    @Override
    public void println() {
        stringBuilder.append(System.lineSeparator());
    }

    @Override
    public void println(boolean x) {
        stringBuilder.append(x).append(System.lineSeparator());
    }

    @Override
    public void println(char x) {
        stringBuilder.append(x).append(System.lineSeparator());
    }

    @Override
    public void println(int x) {
        stringBuilder.append(x).append(System.lineSeparator());
    }

    @Override
    public void println(long x) {
        stringBuilder.append(x).append(System.lineSeparator());
    }

    @Override
    public void println(float x) {
        stringBuilder.append(x).append(System.lineSeparator());
    }

    @Override
    public void println(double x) {
        stringBuilder.append(x).append(System.lineSeparator());
    }

    @Override
    public void println(char[] x) {
        stringBuilder.append(x).append(System.lineSeparator());
    }

    @Override
    public void println(String x) {
        stringBuilder.append(x).append(System.lineSeparator());
    }

    @Override
    public void println(Object x) {
        stringBuilder.append(x).append(System.lineSeparator());
    }
}
