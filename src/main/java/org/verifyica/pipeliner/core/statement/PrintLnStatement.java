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

package org.verifyica.pipeliner.core.statement;

import java.util.List;
import org.verifyica.pipeliner.core.Context;
import org.verifyica.pipeliner.core.expression.Expression;
import org.verifyica.pipeliner.core.expression.ExpressionParser;
import org.verifyica.pipeliner.core.expression.LiteralExpression;

/**
 * A statement that prints a line.
 */
public final class PrintLnStatement implements Statement {

    private final String qualifier;
    private final Expression expression;

    /**
     * Constructor
     *
     * @param qualifier the qualifier for the print statement, e.g., "info", "warning", or "error"
     * @param expression the expression to evaluate and print
     */
    public PrintLnStatement(String qualifier, Expression expression) {
        this.qualifier = qualifier;
        this.expression = expression;
    }

    @Override
    public void execute(Context context) {
        String message = expression.evaluate(context).asString();
        if (qualifier != null) {
            context.println("%s %s", qualifier, message);
        } else {
            context.println(message);
        }
    }

    @Override
    public String toString() {
        return "PrintlnInstruction{" + "qualifier='" + qualifier + '\'' + ", expression=" + expression + '}';
    }

    /**
     * Parses a print statement from the given statementParser.
     *
     * @param statementParser the statement parser to read from
     * @return a new PrintInstruction instance
     */
    public static Statement parse(StatementParser statementParser) {
        Line line = statementParser.nextLine();

        line.expect(Token.Type.LITERAL, "println");

        List<Token> qualifierTokens = KeywordQualifierParser.parse(line);
        String qualifier = !qualifierTokens.isEmpty() ? qualifierTokens.get(0).lexeme : null;

        Expression expression;

        Token nextToken = line.peek();
        if (nextToken != null) {
            line.expectWhitespace();
            expression = ExpressionParser.parseOptionalExpression(line);
        } else {
            expression = new LiteralExpression("");
        }

        return new PrintLnStatement(qualifier, expression);
    }

    /**
     * Resolves all placeholders in {@code text} using the supplied {@link Context}.
     * <p>Supports:
     * <ul>
     *   <li>{@code {: var :}} – recursively resolved from {@code Context#getVariable}</li>
     *   <li>{@code $VAR} and {@code ${VAR}} – resolved via {@code Context#getEnvironmentVariable}</li>
     *   <li>Escaped sequences: <code>\{:</code>, <code>\$VAR</code>, <code>\${VAR}</code>, <code>\"</code></li>
     * </ul>
     * Infinite recursion is prevented with a depth guard (32) and a cycle‐detection set.
     */
    /*
    public static String resolvePlaceholders(String text, Context ctx) {
        return resolvePlaceholders(text, ctx, new java.util.HashSet<>(), 0);
    }
    */

    /** Recursive helper. */
    /*
    private static String resolvePlaceholders(String input, Context ctx, java.util.Set<String> chain, int depth) {
        if (depth > 32) throw new IllegalStateException("Placeholder recursion too deep");

        StringBuilder out = new StringBuilder();
        int len = input.length();

        for (int i = 0; i < len; ) {
            char ch = input.charAt(i);

            /* ── Escaped sequences ─────────────────────────────────────────── */
    /*
    if (ch == '\\') {
        if (i + 1 < len) {
            char next = input.charAt(i + 1);
            if (next == '{' && i + 2 < len && input.charAt(i + 2) == ':') {
                out.append("{:");
                i += 3;
                continue;
            } else if (next == '$') {
                out.append('$');
                i += 2;
                continue;
            } else if (next == '"') {
                out.append('"');
                i += 2;
                continue;
            }
        }
        out.append('\\');
        i++;
        continue;
    }

    /* ── Variable placeholder  {: ... :} ───────────────────────────── */
    /*
    if (ch == '{' && i + 1 < len && input.charAt(i + 1) == ':') {
        int end = input.indexOf(":}", i + 2);
        if (end == -1) { // unterminated ⇒ literal
            out.append(input.substring(i));
            break;
        }
        String inner = input.substring(i + 2, end);
        String trimmed = inner.trim();

        if (!trimmed.isEmpty()) {
            if (!chain.add(trimmed)) throw new IllegalStateException("Recursive reference: " + trimmed);
            String val = ctx.currentFrame().getVariable(trimmed);
            if (val != null) out.append(resolvePlaceholders(val, ctx, chain, depth + 1));
            chain.remove(trimmed);
        } else {
            out.append("{:").append(inner).append(":}"); // preserve spacing for {::}
        }
        i = end + 2;
        continue;
    }

    /* ── ${ENV_VAR} ───────────────────────────────────────────────── */
    /*
    if (ch == '$' && i + 1 < len && input.charAt(i + 1) == '{') {
        int end = input.indexOf('}', i + 2);
        if (end == -1) { // unterminated ⇒ literal
            out.append(input.substring(i));
            break;
        }
        String raw = input.substring(i + 2, end);
        String name = raw.trim();
        if (!name.isEmpty() && EnvironmentVariableName.isValid(name)) {
            String val = ctx.currentFrame().resolveEnvironmentVariable(name);
            if (val != null) out.append(val);
        } else {
            out.append("${").append(raw).append('}');
        }
        i = end + 1;
        continue;
    }

    /* ── $ENV_VAR (greedy) ────────────────────────────────────────── */
    /*
    if (ch == '$' && i + 1 < len && Character.isJavaIdentifierStart(input.charAt(i + 1))) {
        int j = i + 2;
        while (j < len && Character.isJavaIdentifierPart(input.charAt(j))) j++;
        String name = input.substring(i + 1, j);
        if (EnvironmentVariableName.isValid(name)) {
            String val = ctx.resolveEnvironmentVariable(name);
            if (val != null) out.append(val);
        } else {
            out.append('$').append(name);
        }
        i = j;
        continue;
    }

    /* ── Regular character ────────────────────────────────────────── */
    /*
            out.append(ch);
            i++;
        }

        return out.toString();
    }
    */
}
