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

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class DFAGenerator {

    private final Node root = new Node();
    private int stateCount = 0;
    private final List<String> words = new ArrayList<>();

    public void add(String word) {
        words.add(word);
        Node node = root;

        for (char ch : word.toCharArray()) {
            node = node.children.computeIfAbsent(ch, k -> new Node());
        }

        node.isTerminal = true;
    }

    public String generateJava(String packageName, String className) {
        assignIds(root);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("/*\n");
        stringBuilder.append("* Copyright (C) Pipeliner project authors and contributors\n");
        stringBuilder.append("*\n");
        stringBuilder.append("* Licensed under the Apache License, Version 2.0 (the \"License\");\n");
        stringBuilder.append("* you may not use this file except in compliance with the License.\n");
        stringBuilder.append("* You may obtain a copy of the License at\n");
        stringBuilder.append("*\n");
        stringBuilder.append("* http://www.apache.org/licenses/LICENSE-2.0\n");
        stringBuilder.append("*\n");
        stringBuilder.append("* Unless required by applicable law or agreed to in writing, software\n");
        stringBuilder.append("* distributed under the License is distributed on an \"AS IS\" BASIS,\n");
        stringBuilder.append("* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n");
        stringBuilder.append("* See the License for the specific language governing permissions and\n");
        stringBuilder.append("* limitations under the License.\n");
        stringBuilder.append("*/\n\n");

        stringBuilder.append("package ").append(packageName).append(";\n\n");

        stringBuilder.append("import java.util.Arrays;\n\n");

        stringBuilder.append("/**\n");
        stringBuilder.append(" * Auto-generated DFA using 2D array.\n");
        stringBuilder.append(" */\n");
        stringBuilder.append("public final class ").append(className).append(" {\n\n");

        stringBuilder.append("    //\n");
        Collections.sort(words);
        for (String word : words) {
            stringBuilder.append("    //        ").append(word).append("\n");
        }
        stringBuilder.append("    //\n\n");

        stringBuilder.append("    private static final int[][] TRANSITIONS = new int[").append(stateCount).append("][128];\n");
        stringBuilder.append("    private static final boolean[] TERMINAL = new boolean[").append(stateCount).append("];\n\n");

        stringBuilder.append("    static {\n");
        stringBuilder.append("        for (int[] transition : TRANSITIONS) {\n");
        stringBuilder.append("            Arrays.fill(transition, -1);\n");
        stringBuilder.append("        }\n\n");

        emitArrayInit(stringBuilder, root);
        stringBuilder.append("    }\n\n");

        stringBuilder.append("    /**\n");
        stringBuilder.append("     * Constructor\n");
        stringBuilder.append("     */\n");
        stringBuilder.append("    private ").append(className).append("() {\n");
        stringBuilder.append("        // INTENTIONALLY EMPTY\n");
        stringBuilder.append("    }\n\n");

        stringBuilder.append("    /**\n");
        stringBuilder.append("     * Returns the length of the longest match in the given buffer from the specified start index to end index.\n");
        stringBuilder.append("     *\n");
        stringBuilder.append("     * @param characters the character buffer to search\n");
        stringBuilder.append("     * @param start the starting index (inclusive)\n");
        stringBuilder.append("     * @param end the ending index (exclusive)\n");
        stringBuilder.append("     * @return the length of the longest match, or 0 if no match is found\n");
        stringBuilder.append("     */\n");
        stringBuilder.append("    public static int longestMatch(char[] characters, int start, int end) {\n");
        stringBuilder.append("        int state = 0;\n");
        stringBuilder.append("        int maximumLength = 0;\n");
        stringBuilder.append("        for (int i = start; i < end; i++) {\n");
        stringBuilder.append("            char c = characters[i];\n");
        stringBuilder.append("            if (c >= 128) break;\n");
        stringBuilder.append("            int next = TRANSITIONS[state][c];\n");
        stringBuilder.append("            if (next == -1) break;\n");
        stringBuilder.append("            state = next;\n");
        stringBuilder.append("            if (TERMINAL[state]) maximumLength = i - start + 1;\n");
        stringBuilder.append("        }\n");
        stringBuilder.append("        return maximumLength;\n");
        stringBuilder.append("    }\n");
        stringBuilder.append("}\n");

        return stringBuilder.toString();
    }

    private void assignIds(Node node) {
        if (node.id != -1) {
            return;
        }

        node.id = stateCount++;
        for (Node child : node.children.values()) {
            assignIds(child);
        }
    }

    private void emitArrayInit(StringBuilder sb, Node node) {
        if (node.isTerminal) {
            sb.append("        TERMINAL[").append(node.id).append("] = true;\n");
        }
        for (Map.Entry<Character, Node> entry : node.children.entrySet()) {
            char ch = entry.getKey();
            Node child = entry.getValue();
            sb.append("        TRANSITIONS[").append(node.id)
                    .append("]['").append(escape(ch)).append("'] = ")
                    .append(child.id).append(";\n");
            emitArrayInit(sb, child);
        }
    }

    private static String escape(char ch) {
        switch (ch) {
            case '\\': return "\\\\";
            case '\'': return "\\'";
            case '\t': return "\\t";
            case '\n': return "\\n";
            case '\r': return "\\r";
            default:
                return (ch >= 32 && ch < 127)
                        ? String.valueOf(ch)
                        : String.format("\\u%04x", (int) ch);
        }
    }

    private static class Node {

        int id = -1;
        Map<Character, Node> children;
        boolean isTerminal = false;

        public Node() {
            children = new TreeMap<>();
        }
    }

    // Example usage
    public static void main(String[] args) {
        Set<String> words = Set.of(
                "#",
                "/*",
                "*/",
                "//",
                "\"",
                "'",
                "+:=",
                "::",
                ":=",
                "[",
                "]",
                "{",
                "}",
                "(",
                ")",
                "|",
                "environment-variable",
                "env",
                "execute",
                "exec",
                "halt",
                "if",
                "print",
                "println",
                "shell",
                "sleep",
                "str",
                "variable",
                "var",
                "working-directory"
        );

        DFAGenerator generator = new DFAGenerator();

        for (String keyword : words) {
            generator.add(keyword);
        }

        System.out.println(generator.generateJava("org.verifyica.pipeliner.core.parser", "DFA").trim());
    }
}
