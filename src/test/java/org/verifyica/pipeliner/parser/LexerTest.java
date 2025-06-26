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

package org.verifyica.pipeliner.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LexerTest {

    @Test
    public void testLexer() {
        String input = "Hello World";
        List<Lexer.Token> expectedTokens = of(new Lexer.Token(Lexer.Token.Type.TEXT, "Hello World"));

        validate(input, expectedTokens);

        input = "${{ Hello World }}";
        expectedTokens = of(new Lexer.Token(Lexer.Token.Type.VARIABLE, "${{ Hello World }}"));

        validate(input, expectedTokens);

        input = "${{ Hello World";
        expectedTokens = of(new Lexer.Token(Lexer.Token.Type.TEXT, "${{ Hello World"));

        validate(input, expectedTokens);

        input = "$VAR";
        expectedTokens = of(new Lexer.Token(Lexer.Token.Type.ENVIRONMENT_VARIABLE, "$VAR"));

        validate(input, expectedTokens);

        input = "${VAR}";
        expectedTokens = of(new Lexer.Token(Lexer.Token.Type.ENVIRONMENT_VARIABLE, "${VAR}"));

        validate(input, expectedTokens);

        input = "${ VAR";
        expectedTokens = of(new Lexer.Token(Lexer.Token.Type.TEXT, "${ VAR"));

        validate(input, expectedTokens);

        input = "${{}}";
        expectedTokens = of(new Lexer.Token(Lexer.Token.Type.VARIABLE, "${{}}"));

        validate(input, expectedTokens);

        input = "${}";
        expectedTokens = of(new Lexer.Token(Lexer.Token.Type.ENVIRONMENT_VARIABLE, "${}"));

        validate(input, expectedTokens);

        input = "$$";
        expectedTokens = of(
                new Lexer.Token(Lexer.Token.Type.ENVIRONMENT_VARIABLE, "$"),
                new Lexer.Token(Lexer.Token.Type.ENVIRONMENT_VARIABLE, "$"));

        validate(input, expectedTokens);

        input = "$ $";
        expectedTokens = of(
                new Lexer.Token(Lexer.Token.Type.ENVIRONMENT_VARIABLE, "$"),
                new Lexer.Token(Lexer.Token.Type.TEXT, " "),
                new Lexer.Token(Lexer.Token.Type.ENVIRONMENT_VARIABLE, "$"));

        validate(input, expectedTokens);

        input = "echo \\${{Hello World}}";
        expectedTokens = of(
                new Lexer.Token(Lexer.Token.Type.TEXT, "echo "),
                new Lexer.Token(Lexer.Token.Type.TEXT, "\\$"),
                new Lexer.Token(Lexer.Token.Type.TEXT, "{{Hello World}}"));

        validate(input, expectedTokens);
    }

    private static void validate(String string, List<Lexer.Token> expectedTokens) {
        List<Lexer.Token> actualTokens = new ArrayList<>();

        // System.out.printf("input %s%n", string);

        Lexer lexer = new Lexer(string);
        Lexer.Token token;
        while ((token = lexer.next()) != null) {
            // System.out.printf("  %s%n", token);
            actualTokens.add(token);
        }

        assertThat(actualTokens).hasSize(expectedTokens.size());

        for (int i = 0; i < expectedTokens.size(); i++) {
            Lexer.Token actualToken = actualTokens.get(i);
            Lexer.Token expectedToken = expectedTokens.get(i);

            assertThat(actualToken.getType()).isEqualTo(expectedToken.getType());
            assertThat(actualToken.getText()).isEqualTo(expectedToken.getText());
        }
    }

    private static List<Lexer.Token> of(Lexer.Token... tokens) {
        List<Lexer.Token> list = new ArrayList<>();
        Collections.addAll(list, tokens);
        return list;
    }
}
