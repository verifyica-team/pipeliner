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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.verifyica.pipeliner.lexer.Lexer;
import org.verifyica.pipeliner.lexer.SyntaxException;
import org.verifyica.pipeliner.lexer.Token;

/** Class to implement Resolver */
public class Resolver {

    private static final String DEFAULT_PROPERTY_VALUE = "";

    private static final String DEFAULT_ENVIRONMENT_VARIABLE_VALUE = "";

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
     * @throws UnresolvedException if an error occurs during resolving
     * @throws SyntaxException if an error occurs during tokenization
     */
    public static Map<String, String> resolveEnvironmentVariables(
            Map<String, String> environmentVariables, Map<String, String> properties)
            throws UnresolvedException, SyntaxException {
        Map<String, String> resolvedEnvironmentVariables = new TreeMap<>();

        // Iterate over the environment variables resolving them
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            String key = entry.getKey();
            String resolvedString = entry.getValue();
            String previousString;

            do {
                previousString = resolvedString;
                resolvedString = resolveAll(environmentVariables, properties, resolvedString);
            } while (!resolvedString.equals(previousString));

            // Tokenize the resolved string
            List<Token> tokens = Lexer.tokenize(resolvedString);

            // Iterate over the tokens checking for unresolved environment variables
            for (Token token : tokens) {
                switch (token.getType()) {
                    case PROPERTY: {
                        throw new UnresolvedException(format("unresolved property [%s]", token.getText()));
                    }
                    case ENVIRONMENT_VARIABLE: {
                        throw new UnresolvedException(format("unresolved environment variable [%s]", token.getText()));
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
     * @throws UnresolvedException if an error occurs during resolving
     * @throws SyntaxException if an error occurs during tokenization
     */
    public static Map<String, String> resolveProperties(Map<String, String> properties)
            throws UnresolvedException, SyntaxException {
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
            List<Token> tokens = Lexer.tokenize(resolvedString);

            // Iterate over the tokens checking for unresolved properties
            for (Token token : tokens) {
                if (token.getType() == Token.Type.PROPERTY) {
                    throw new UnresolvedException(format("unresolved property [%s]", token.getText()));
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
     * @throws SyntaxException if an error occurs during tokenization
     */
    public static String replaceProperties(Map<String, String> properties, String input) throws SyntaxException {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();

        // Tokenize the input string and iterate over the tokens
        Iterator<Token> iterator = Lexer.tokenize(input).iterator();
        while (iterator.hasNext()) {
            // Get the next token
            Token token = iterator.next();

            if (token.getType() == Token.Type.PROPERTY) {
                // Resolve the PROPERTY token value
                String value = properties.getOrDefault(token.getValue(), DEFAULT_PROPERTY_VALUE);
                result.append(value);
            } else {
                // Append the text
                result.append(token.getText());
            }
        }

        return result.toString();
    }

    /**
     * Method to resolve environment variables in a string
     *
     * @param environmentVariables the environment variables
     * @param input the input string
     * @return a string with environment variables resolved
     * @throws SyntaxException if an error occurs during tokenization
     */
    public static String replaceEnvironmentVariables(Map<String, String> environmentVariables, String input)
            throws SyntaxException {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();

        // Tokenize the input string and iterate over the tokens
        Iterator<Token> iterator = Lexer.tokenize(input).iterator();
        while (iterator.hasNext()) {
            // Get the next token
            Token token = iterator.next();

            if (token.getType() == Token.Type.ENVIRONMENT_VARIABLE) {
                // Resolve the ENVIRONMENT_VARIABLE token value
                String value = environmentVariables.getOrDefault(token.getValue(), DEFAULT_ENVIRONMENT_VARIABLE_VALUE);
                result.append(value);
            } else {
                result.append(token.getText());
            }
        }

        return result.toString();
    }

    /**
     * Method to resolve both environment variables and properties in a string
     *
     * @param properties the properties
     * @param input the input string
     * @return a string with properties resolved
     * @throws UnresolvedException if an error occurs during resolving
     * @throws SyntaxException if an error occurs during tokenization
     */
    private static String resolvePropertiesSinglePass(Map<String, String> properties, String input)
            throws UnresolvedException, SyntaxException {
        StringBuilder stringBuilder = new StringBuilder();

        // Tokenize the input string
        List<Token> tokens = Lexer.tokenize(input);

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
                    throw new UnresolvedException(format("unknown token type [%s]", token.getType()));
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
     * @throws UnresolvedException if an error occurs during resolving
     * @throws SyntaxException if an error occurs during tokenization
     */
    private static String resolveAll(
            Map<String, String> environmentVariables, Map<String, String> properties, String input)
            throws UnresolvedException, SyntaxException {
        StringBuilder stringBuilder = new StringBuilder();

        // Tokenize the input string
        List<Token> tokens = Lexer.tokenize(input);

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
                    stringBuilder.append(token.getText());
                    break;
                }
                default: {
                    throw new UnresolvedException(format("unknown token type [%s]", token.getType()));
                }
            }
        }

        return stringBuilder.toString();
    }
}
