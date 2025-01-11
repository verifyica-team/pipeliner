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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.verifyica.pipeliner.tokenizer.Token;
import org.verifyica.pipeliner.tokenizer.Tokenizer;
import org.verifyica.pipeliner.tokenizer.TokenizerException;

/** Class to implement Resolver */
public class Resolver {

    private static final String UNRESOLVED_PROPERTY_REGEX = "(?<!\\\\)\\$\\{\\{\\s*.*\\s*}}";

    private static final Pattern UNRESOLVED_PROPERTY_PATTERN = Pattern.compile(UNRESOLVED_PROPERTY_REGEX);

    /** Constructor */
    private Resolver() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to resolve a map of environment variables
     *
     * @param environmentVariables environment variables
     * @param properties the properties
     * @return a map with environment variables resolved
     * @throws ResolverException ResolverException
     * @throws TokenizerException TokenizerException
     */
    public static Map<String, String> resolveEnvironmentVariables(
            Map<String, String> environmentVariables, Map<String, String> properties)
            throws ResolverException, TokenizerException {
        Map<String, String> resolvedEnvironmentVariables = new TreeMap<>();

        // Iterate over the environment variables resolving them
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            String key = entry.getKey();
            String resolvedString = entry.getValue();
            String previousString;

            do {
                previousString = resolvedString;
                resolvedString =
                        resolveEnvironmentVariablesSinglePass(environmentVariables, properties, resolvedString);
            } while (!resolvedString.equals(previousString));

            // Tokenize the resolved string
            List<Token> tokens = Tokenizer.tokenize(resolvedString);

            // Iterate over the tokens checking for unresolved environment variables
            for (Token token : tokens) {
                switch (token.getType()) {
                    case PROPERTY: {
                        throw new ResolverException(format("unresolved property [%s]", token.getText()));
                    }
                    case ENVIRONMENT_VARIABLE: {
                        throw new ResolverException(format("unresolved environment variable [%s]", token.getText()));
                    }
                    default: {
                        break;
                    }
                }
            }

            // Add resolved environment variable to the map
            resolvedEnvironmentVariables.put(key, resolvedString);
        }

        return resolvedEnvironmentVariables;
    }

    /**
     * Method to resolve a map of properties
     *
     * @param properties the properties
     * @return a map with properties resolved
     * @throws ResolverException ResolverException
     * @throws TokenizerException TokenizerException
     */
    public static Map<String, String> resolveProperties(Map<String, String> properties)
            throws ResolverException, TokenizerException {
        Map<String, String> resolvedProperties = new TreeMap<>();

        // Iterate over the properties resolving them
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String resolvedString = entry.getValue();
            String previousString;

            do {
                previousString = resolvedString;
                resolvedString = resolvePropertiesSinglePass(properties, resolvedString);
            } while (!resolvedString.equals(previousString));

            // Tokenize the resolved string
            List<Token> tokens = Tokenizer.tokenize(resolvedString);

            // Iterate over the tokens checking for unresolved properties
            for (Token token : tokens) {
                switch (token.getType()) {
                    case PROPERTY: {
                        throw new ResolverException(format("unresolved property [%s]", token.getText()));
                    }
                    case ENVIRONMENT_VARIABLE:
                    case TEXT: {
                        break;
                    }
                    default: {
                        throw new ResolverException(format("unknown token type [%s]", token.getType()));
                    }
                }
            }

            // Add resolved property to the map
            resolvedProperties.put(key, resolvedString);
        }

        return resolvedProperties;
    }

    /**
     * Method to resolve properties in a string
     *
     * @param properties the properties
     * @param input the input string
     * @return a string with properties resolved
     */
    public static String replaceProperties(Map<String, String> properties, String input) {
        String workingString = input;

        // Iterate over the properties replacing them in the string
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Regular expression to match ${{ foo }} or ${{foo}} based on the key
            String regex = "(?<!\\\\)\\$\\{\\{\\s*" + Pattern.quote(key) + "\\s*}}";

            // Replace all defined properties in the string
            workingString = workingString.replaceAll(regex, Matcher.quoteReplacement(value));
        }

        // Remove all unresolved properties from the string
        Matcher matcher = UNRESOLVED_PROPERTY_PATTERN.matcher(workingString);
        workingString = matcher.replaceAll("");

        return workingString;
    }

    /**
     * Method to resolve environment variables in a string
     *
     * @param environmentVariables the environment variables
     * @param input the input string
     * @return a string with environment variables resolved
     */
    public static String replaceEnvironmentVariables(Map<String, String> environmentVariables, String input) {
        String workingString = input;

        // Iterate over the environment variables replacing them in the string
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Regular express to match $FOO or ${FOO}, but not \$FOO or \${FOO}
            String regex = "(?<!\\\\)\\$(\\{" + Pattern.quote(key) + "\\}|\\b" + Pattern.quote(key) + "\\b)";

            // Replace all defined environment variables in the string
            workingString = workingString.replaceAll(regex, Matcher.quoteReplacement(value));
        }

        return workingString;
    }

    /**
     * Method to resolve both environment variables and properties in a string
     *
     * @param properties the properties
     * @param input the input string
     * @return a string with properties resolved
     * @throws ResolverException ResolverException
     * @throws TokenizerException TokenizerException
     */
    private static String resolvePropertiesSinglePass(Map<String, String> properties, String input)
            throws ResolverException, TokenizerException {
        StringBuilder stringBuilder = new StringBuilder();

        // Tokenize the input string
        List<Token> tokens = Tokenizer.tokenize(input);

        // Iterate over the tokens resolving properties
        for (Token token : tokens) {
            switch (token.getType()) {
                case PROPERTY: {
                    String value = properties.getOrDefault(token.getValue(), "");
                    // Code left in the event that we want to throw an exception if a property is unresolved
                    /*
                    if (value == null) {
                        throw new ResolverException(format("unresolved property [%s]", token.getText()));
                    }
                    */
                    stringBuilder.append(value);
                    break;
                }
                case ENVIRONMENT_VARIABLE: {
                    stringBuilder.append(token.getText());
                    break;
                }
                case TEXT: {
                    stringBuilder.append(token.getValue());
                    break;
                }
                default: {
                    throw new ResolverException(format("unknown token type [%s]", token.getType()));
                }
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Method to resolve both environment variables and properties in a string
     *
     * @param environmentVariables the environment variables
     * @param properties the properties
     * @param input the input string
     * @return a string with environment variables and properties resolved
     * @throws ResolverException ResolverException
     * @throws TokenizerException TokenizerException
     */
    private static String resolveEnvironmentVariablesSinglePass(
            Map<String, String> environmentVariables, Map<String, String> properties, String input)
            throws ResolverException, TokenizerException {
        StringBuilder stringBuilder = new StringBuilder();

        // Tokenize the input string
        List<Token> tokens = Tokenizer.tokenize(input);

        // Iterate over the tokens resolving properties and environment variables
        for (Token token : tokens) {
            switch (token.getType()) {
                case PROPERTY: {
                    String value = properties.getOrDefault(token.getValue(), "");
                    // Code left in the event that we want to throw an exception if a property is unresolved
                    /*
                    if (value == null) {
                        throw new ResolverException(format("unresolved property [%s]", token.getText()));
                    }
                    */
                    stringBuilder.append(value);
                    break;
                }
                case ENVIRONMENT_VARIABLE: {
                    String value = environmentVariables.getOrDefault(token.getValue(), "");
                    // Code left in the event that we want to throw an exception if an environment variable is
                    // unresolved
                    /*
                    if (value == null) {
                        throw new ResolverException(format("unresolved environment variable [%s]", token.getText()));
                    }
                    */
                    stringBuilder.append(value);
                    break;
                }
                case TEXT: {
                    stringBuilder.append(token.getValue());
                    break;
                }
                default: {
                    throw new ResolverException(format("unknown token type [%s]", token.getType()));
                }
            }
        }

        return stringBuilder.toString();
    }
}
