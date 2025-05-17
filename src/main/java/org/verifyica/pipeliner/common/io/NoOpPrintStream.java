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

/** Class to implement NoOpPrintStream */
public class NoOpPrintStream extends PrintStream {

    /**
     * Constructor
     */
    public NoOpPrintStream() {
        super(new NoOpOutputStream());
    }

    @Override
    public void print(boolean b) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void print(char c) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void print(int i) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void print(long l) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void print(float f) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void print(double d) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void print(char[] s) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void print(String s) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void print(Object obj) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void println() {
        // INTENTIONALLY BLANK
    }

    @Override
    public void println(boolean x) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void println(char x) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void println(int x) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void println(long x) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void println(float x) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void println(double x) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void println(char[] x) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void println(String x) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void println(Object x) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void write(int b) {
        // INTENTIONALLY BLANK
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        // INTENTIONALLY BLANK
    }
}
