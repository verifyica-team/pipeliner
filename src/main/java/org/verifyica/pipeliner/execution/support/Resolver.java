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
import org.verifyica.pipeliner.parser.ParsedToken;
import org.verifyica.pipeliner.parser.Parser;
import org.verifyica.pipeliner.parser.SyntaxException;

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
            List<ParsedToken> parsedTokens = Parser.parse(resolvedString);

            // Iterate over the tokens checking for unresolved environment variables
            for (ParsedToken parsedToken : parsedTokens) {
                switch (parsedToken.getType()) {
                    case PROPERTY: {
                        throw new UnresolvedException(format("unresolved property [%s]", parsedToken.getText()));
                    }
                    case ENVIRONMENT_VARIABLE: {
                        throw new UnresolvedException(
                                format("unresolved environment variable [%s]", parsedToken.getText()));
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
            List<ParsedToken> parsedTokens = Parser.parse(resolvedString);

            // Iterate over the tokens checking for unresolved properties
            for (ParsedToken parsedToken : parsedTokens) {
                if (parsedToken.getType() == ParsedToken.Type.PROPERTY) {
                    throw new UnresolvedException(format("unresolved property [%s]", parsedToken.getText()));
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
        Iterator<ParsedToken> iterator = Parser.parse(input).iterator();
        while (iterator.hasNext()) {
            // Get the next parsed token
            ParsedToken parsedToken = iterator.next();

            if (parsedToken.getType() == ParsedToken.Type.PROPERTY) {
                // Resolve the PROPERTY token value
                String value = properties.getOrDefault(parsedToken.getValue(), DEFAULT_PROPERTY_VALUE);
                result.append(value);
            } else {
                // Append the text
                result.append(parsedToken.getText());
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
        Iterator<ParsedToken> iterator = Parser.parse(input).iterator();
        while (iterator.hasNext()) {
            // Get the next token
            ParsedToken parsedToken = iterator.next();

            if (parsedToken.getType() == ParsedToken.Type.ENVIRONMENT_VARIABLE) {
                // Resolve the ENVIRONMENT_VARIABLE token value
                String value =
                        environmentVariables.getOrDefault(parsedToken.getValue(), DEFAULT_ENVIRONMENT_VARIABLE_VALUE);
                result.append(value);
            } else {
                result.append(parsedToken.getText());
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
        List<ParsedToken> parsedTokens = Parser.parse(input);

        // Iterate over the parsed tokens resolving properties
        for (ParsedToken parsedToken : parsedTokens) {
            switch (parsedToken.getType()) {
                case PROPERTY: {
                    String value = properties.getOrDefault(parsedToken.getValue(), "");
                    // Code left in the event that we want to throw an exception if a property is unresolved
                    /*
                    if (value == null) {
                        throw new ResolverException(format("unresolved property [%s]", parsedToken.getText()));
                    }
                    */
                    stringBuilder.append(value);
                    break;
                }
                case ENVIRONMENT_VARIABLE: {
                    stringBuilder.append(parsedToken.getText());
                    break;
                }
                case TEXT: {
                    stringBuilder.append(parsedToken.getValue());
                    break;
                }
                default: {
                    throw new UnresolvedException(format("unknown token type [%s]", parsedToken.getType()));
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
        List<ParsedToken> parsedTokens = Parser.parse(input);

        // Iterate over the tokens resolving properties and environment variables
        for (ParsedToken parsedToken : parsedTokens) {
            switch (parsedToken.getType()) {
                case PROPERTY: {
                    String value = properties.getOrDefault(parsedToken.getValue(), "");
                    // Code left in the event that we want to throw an exception if a property is unresolved
                    /*
                    if (value == null) {
                        throw new ResolverException(format("unresolved property [%s]", parsedToken.getText()));
                    }
                    */
                    stringBuilder.append(value);
                    break;
                }
                case ENVIRONMENT_VARIABLE: {
                    String value = environmentVariables.getOrDefault(parsedToken.getValue(), "");
                    // Code left in the event that we want to throw an exception if an environment variable is
                    // unresolved
                    /*
                    if (value == null) {
                        throw new ResolverException(format("unresolved environment variable [%s]", parsedToken.getText()));
                    }
                    */
                    stringBuilder.append(value);
                    break;
                }
                case TEXT: {
                    stringBuilder.append(parsedToken.getText());
                    break;
                }
                default: {
                    throw new UnresolvedException(format("unknown token type [%s]", parsedToken.getType()));
                }
            }
        }

        return stringBuilder.toString();
    }
}
