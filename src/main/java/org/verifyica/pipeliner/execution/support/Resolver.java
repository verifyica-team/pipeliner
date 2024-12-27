/*
 * Copyright (C) 2024-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner.execution.support;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class to implement EnvironmentVariableResolver */
public class Resolver {

    /** Constructor */
    private Resolver() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to resolve environment variables
     *
     * @param environmentVariables environmentVariables
     * @param properties properties
     * @return resolved environment variables
     * @throws ResolverException resolverException
     */
    public static Map<String, String> resolveEnvironmentVariables(
            Map<String, String> environmentVariables, Map<String, String> properties) throws ResolverException {
        Map<String, String> resolvedEnvVars = new TreeMap<>();

        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            String key = entry.getKey();
            String value = resolveEnvironmentVariablesAndProperties(environmentVariables, properties, entry.getValue());
            resolvedEnvVars.put(key, value);
        }

        return resolvedEnvVars;
    }

    /**
     * Method to resolve environment variables and properties
     *
     * @param environmentVariables environmentVariables
     * @param properties properties
     * @param string string
     * @return resolved string
     * @throws ResolverException resolverException
     */
    public static String resolveEnvironmentVariablesAndProperties(
            Map<String, String> environmentVariables, Map<String, String> properties, String string)
            throws ResolverException {
        String previousValue;

        do {
            previousValue = string;
            string = resolveEnvironmentVariableTokens(environmentVariables, string);
            string = resolvePropertyTokens(properties, string);
        } while (!string.equals(previousValue));

        return string;
    }

    /**
     * Method to resolve properties
     *
     * @param properties properties
     * @param string string
     * @return resolved properties
     * @throws ResolverException resolverException
     */
    public static String resolveProperties(Map<String, String> properties, String string) throws ResolverException {
        String actualCommand = string;
        String previousCommand;

        do {
            previousCommand = actualCommand;
            List<String> tokens = parseTokens(previousCommand);

            for (String token : tokens) {
                if (token.startsWith("${{") && token.endsWith("}}")) {
                    String key = token.substring(3, token.length() - 2).trim();
                    String resolvedToken = properties.get(key);
                    if (resolvedToken == null) {
                        throw new ResolverException(format("unresolved property [%s]", token));
                    }
                    actualCommand = actualCommand.replace(token, resolvedToken);
                }
            }
        } while (!actualCommand.equals(previousCommand));

        return previousCommand;
    }

    /**
     * Method to resolve environment variable tokens
     *
     * @param environmentVariables environmentVariables
     * @param value value
     * @return resolved environment tokens
     * @throws ResolverException resolverException
     */
    private static String resolveEnvironmentVariableTokens(Map<String, String> environmentVariables, String value)
            throws ResolverException {
        Pattern pattern = Pattern.compile("\\$(\\w+)");
        Matcher matcher = pattern.matcher(value);
        StringBuffer stringBuffer = new StringBuffer();

        while (matcher.find()) {
            String token = matcher.group(1);
            String resolvedValue = environmentVariables.get(token);

            if (resolvedValue == null) {
                throw new ResolverException(format("unresolved environment variable [%s]", token));
            }

            matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(resolvedValue));
        }

        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }

    /**
     * Method to resolve property tokens
     *
     * @param properties properties
     * @param value value
     * @return resolved property tokens
     * @throws ResolverException resolverException
     */
    private static String resolvePropertyTokens(Map<String, String> properties, String value) throws ResolverException {
        Pattern pattern = Pattern.compile("\\$\\{\\{\\s*(.*?)\\s*\\}\\}");
        Matcher matcher = pattern.matcher(value);
        StringBuffer stringBuffer = new StringBuffer();

        while (matcher.find()) {
            String token = matcher.group(1);
            String resolvedValue = properties.get(token);

            if (resolvedValue == null) {
                throw new ResolverException(format("unresolved property [%s]", token));
            }

            matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(resolvedValue));
        }

        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }

    /**
     * Method to parse property tokens
     *
     * @param string string
     * @return list of tokens
     */
    private static List<String> parseTokens(String string) {
        List<String> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\$[A-Za-z0-9_]+|\\$\\{\\{\\s*[a-zA-Z0-9_\\-.]+\\s*\\}\\}?");
        Matcher matcher = pattern.matcher(string);

        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                tokens.add(string.substring(lastEnd, matcher.start()));
            }

            tokens.add(matcher.group());
            lastEnd = matcher.end();
        }

        if (lastEnd < string.length()) {
            tokens.add(string.substring(lastEnd));
        }

        return tokens;
    }
}
