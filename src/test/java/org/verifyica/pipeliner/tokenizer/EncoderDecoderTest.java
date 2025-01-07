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

package org.verifyica.pipeliner.tokenizer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class EncoderDecoderTest {

    @ParameterizedTest
    @MethodSource("getTestData")
    void testEncodeDecode(TestData testData) {
        String input = testData.getInput();
        String encodedInput = EncoderDecoder.encode(input);
        String decodedInput = EncoderDecoder.decode(encodedInput);

        assertThat(decodedInput).isEqualTo(input);
    }

    private static Stream<TestData> getTestData() {
        List<TestData> list = new ArrayList<>();

        // Simple cases
        list.add(new TestData("\\${"));
        list.add(new TestData("\\$"));
        list.add(new TestData("\\\""));
        list.add(new TestData("_"));

        // Mixed cases
        list.add(new TestData("\\${{Hello\\${World_}}"));
        list.add(new TestData("NormalText\\$Special"));
        list.add(new TestData("Edge_Case\\\"_Test"));

        // Complex cases
        list.add(new TestData("Complex\\${{\\${_\\$_\"_"));
        list.add(new TestData("Complex\\${{\\${_\\$_\"_"));
        list.add(new TestData("\\${{Escaped\\$String_Inside}}"));
        list.add(new TestData("Nested_\\${Structure_\\${{Here_}}"));
        list.add(new TestData("EdgeCase\\${{\\$_\\\"_\\${}}"));
        list.add(new TestData("_Multiple_\\$_\\${\\${{Combinations}}_"));
        list.add(new TestData("Special_Case\\${{\\${_End"));
        list.add(new TestData("\\${{Middle\\${Here\\$_\\\"}}"));
        list.add(new TestData("Pre\\${{fix\\${_Suffix}}_"));
        list.add(new TestData("Start\\$End_\\\"_\\${{_"));
        list.add(new TestData("Mix\\${String\\\"With\\${{Underscores}}_"));
        list.add(new TestData("'${{ foo }}'"));
        list.add(new TestData("echo '$ FOO'"));
        list.add(new TestData("echo '$$ FOO'"));
        list.add(new TestData("echo '$$$ FOO'"));
        list.add(new TestData("echo 'FOO$'"));
        list.add(new TestData("echo 'FOO$$'"));
        list.add(new TestData("echo 'FOO$$$'"));
        list.add(new TestData("echo '! FOO'"));
        list.add(new TestData("echo '!! FOO'"));
        list.add(new TestData("echo '!!! FOO'"));
        list.add(new TestData("echo '{ FOO'"));
        list.add(new TestData("echo '{{ FOO'"));
        list.add(new TestData("echo '{{{ FOO'"));
        list.add(new TestData("echo '\\{ FOO'"));
        list.add(new TestData("echo '\\{{ FOO'"));
        list.add(new TestData("echo '\\{{{ FOO'"));
        list.add(new TestData("echo '\\{FOO'"));
        list.add(new TestData("echo '\\{\\{FOO'"));
        list.add(new TestData("echo '\\{\\{\\{FOO'"));

        // Edge cases
        list.add(new TestData(null));
        list.add(new TestData(""));
        list.add(new TestData(EncoderDecoder.ENCODING_PREFIX));
        list.add(new TestData(EncoderDecoder.ENCODING_SUFFIX));
        list.add(new TestData(EncoderDecoder.ENCODING_PREFIX + EncoderDecoder.ENCODING_SUFFIX));

        for (int i = 0; i < 100; i++) {
            list.add(new TestData(EncoderDecoder.ENCODING_PREFIX + i + EncoderDecoder.ENCODING_SUFFIX));
        }

        return list.stream();
    }

    /** Class to implement TestData */
    private static class TestData {

        private final String input;

        /**
         * Constructor
         *
         * @param input input
         */
        public TestData(String input) {
            this.input = input;
        }

        /**
         * Method to get the input
         *
         * @return input
         */
        public String getInput() {
            return input;
        }
    }
}
