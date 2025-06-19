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

package org.verifyica.pipeliner.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.verifyica.pipeliner.support.MultiLineParser;

/** Class to implement MultiLineParserTest */
public class MultiLineParserTest {

    private static final String LINE_CONTINUATION_SEQUENCE = " \\\r\n";

    /**
     * Method to test command line parsing, validating the lines list returned is equal the expected lines list
     *
     * @param testData the test data
     */
    @ParameterizedTest
    @MethodSource("getTestData")
    public void testLineParser(TestData testData) {
        List<String> lines = MultiLineParser.parse(testData.input());

        assertThat(lines).isEqualTo(testData.expectedLines());
    }

    /**
     * Method to get the test data
     *
     * @return the test data
     */
    public static Stream<TestData> getTestData() {
        List<TestData> list = new ArrayList<>();

        list.add(new TestData().input("ls").expectedLine("ls"));

        list.add(new TestData().input("ls \r\n -l").expectedLine("ls ").expectedLine(" -l"));

        list.add(new TestData().input("ls \r -l").expectedLine("ls ").expectedLine(" -l"));

        list.add(new TestData().input("ls \n -l").expectedLine("ls ").expectedLine(" -l"));

        list.add(new TestData().input("ls \\r\\n -l").expectedLine("ls \\r\\n -l"));

        list.add(new TestData().input("ls \\r -l").expectedLine("ls \\r -l"));

        list.add(new TestData().input("ls \\n -l").expectedLine("ls \\n -l"));

        list.add(new TestData()
                .input("ls" + LINE_CONTINUATION_SEQUENCE + " -l\r\n | sort")
                .expectedLine("ls -l")
                .expectedLine(" | sort"));

        list.add(new TestData()
                .input("ls" + LINE_CONTINUATION_SEQUENCE + " -l\r | sort")
                .expectedLine("ls -l")
                .expectedLine(" | sort"));

        list.add(new TestData()
                .input("ls" + LINE_CONTINUATION_SEQUENCE + " -l\n | sort")
                .expectedLine("ls -l")
                .expectedLine(" | sort"));

        list.add(new TestData()
                .input("ls" + LINE_CONTINUATION_SEQUENCE + " -l\r\n | sort\r\n|wc -l")
                .expectedLine("ls -l")
                .expectedLine(" | sort")
                .expectedLine("|wc -l"));

        list.add(new TestData()
                .input("ls" + LINE_CONTINUATION_SEQUENCE + " -l\r | sort\r|wc -l")
                .expectedLine("ls -l")
                .expectedLine(" | sort")
                .expectedLine("|wc -l"));

        list.add(new TestData()
                .input("ls" + LINE_CONTINUATION_SEQUENCE + " -l\n | sort\n|wc -l")
                .expectedLine("ls -l")
                .expectedLine(" | sort")
                .expectedLine("|wc -l"));

        list.add(new TestData()
                .input("ls" + LINE_CONTINUATION_SEQUENCE + " -l\n | sort\n|wc" + LINE_CONTINUATION_SEQUENCE + " -l")
                .expectedLine("ls -l")
                .expectedLine(" | sort")
                .expectedLine("|wc -l"));

        list.add(new TestData()
                .input("ls" + LINE_CONTINUATION_SEQUENCE + " -l\n# \n | sort\n|wc" + LINE_CONTINUATION_SEQUENCE + " -l")
                .expectedLine("ls -l")
                .expectedLine(" | sort")
                .expectedLine("|wc -l"));

        list.add(new TestData()
                .input("ls" + LINE_CONTINUATION_SEQUENCE + " -l\n# \\\n | sort\n|wc" + LINE_CONTINUATION_SEQUENCE
                        + " -l")
                .expectedLine("ls -l")
                .expectedLine(" | sort")
                .expectedLine("|wc -l"));

        list.add(new TestData()
                .input("ls" + LINE_CONTINUATION_SEQUENCE + " -l\n# \\\n | sort\n|wc" + LINE_CONTINUATION_SEQUENCE
                        + " -l\n#\n#")
                .expectedLine("ls -l")
                .expectedLine(" | sort")
                .expectedLine("|wc -l"));

        list.add(new TestData()
                .input(" ls" + LINE_CONTINUATION_SEQUENCE + " -l\n# \\\n | sort\n|wc" + LINE_CONTINUATION_SEQUENCE
                        + " -l\n#\n#")
                .expectedLine(" ls -l")
                .expectedLine(" | sort")
                .expectedLine("|wc -l"));

        list.add(new TestData()
                .input(" ls" + LINE_CONTINUATION_SEQUENCE + " -l\t\n# \\\n | sort\n|wc" + LINE_CONTINUATION_SEQUENCE
                        + " -l\n#\n#")
                .expectedLine(" ls -l\t")
                .expectedLine(" | sort")
                .expectedLine("|wc -l"));

        list.add(new TestData()
                .input("\tls" + LINE_CONTINUATION_SEQUENCE + " -l\t\n# \\\n | sort\n|wc" + LINE_CONTINUATION_SEQUENCE
                        + " -l\n#\n#")
                .expectedLine("\tls -l\t")
                .expectedLine(" | sort")
                .expectedLine("|wc -l"));

        list.add(new TestData()
                .input("\tls" + LINE_CONTINUATION_SEQUENCE + " -l\t\n# \\\n | sort\n|wc" + LINE_CONTINUATION_SEQUENCE
                        + " -l\n#\n#")
                .expectedLine("\tls -l\t")
                .expectedLine(" | sort")
                .expectedLine("|wc -l"));

        List<TestData> copy = new ArrayList<>(list);

        for (TestData testData : copy) {
            TestData newTestData = new TestData();
            newTestData.input(testData.input().replaceAll("\r", "\n"));

            for (String expectedLine : testData.expectedLines()) {
                newTestData.expectedLine(expectedLine.replaceAll("\r", "\n"));
            }

            list.add(newTestData);
        }

        for (TestData testData : copy) {
            TestData newTestData = new TestData();
            newTestData.input(testData.input().replaceAll("\n", "\r"));

            for (String expectedLine : testData.expectedLines()) {
                newTestData.expectedLine(expectedLine.replaceAll("\n", "\r"));
            }

            list.add(newTestData);
        }

        return list.stream();
    }

    /** Class to implement TestData */
    public static class TestData {

        private String input;
        private final List<String> expectedLines;

        /**
         * Constructor
         */
        public TestData() {
            expectedLines = new ArrayList<>();
        }

        /**
         * Method to set the input string
         *
         * @param input inputString
         * @return the TestData
         */
        public TestData input(String input) {
            this.input = input;
            return this;
        }

        /**
         * Method to get the input
         *
         * @return the input
         */
        public String input() {
            return input;
        }

        /**
         * Method to add an expected line
         *
         * @param line the expected line
         * @return the TestData
         */
        public TestData expectedLine(String line) {
            expectedLines.add(line);
            return this;
        }

        /**
         * Method to get the expected lines
         *
         * @return the expected lines
         */
        public List<String> expectedLines() {
            return expectedLines;
        }
    }
}
