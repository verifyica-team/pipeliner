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
import org.verifyica.pipeliner.parser.Parser;
import org.verifyica.pipeliner.parser.SyntaxException;
import org.verifyica.pipeliner.parser.tokens.*;

/** Class to implement Resolver */
public class Resolver {

    private static final String DEFAULT_ENVIRONMENT_VARIABLE_VALUE = "";

    private static final String DEFAULT_VARIABLE_VALUE = "";

    /** Constructor */
    private Resolver() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to resolve a map of environment variables
     *
     * @param environmentVariables environment variables
     * @param variables the variables
     * @return a map with environment variables resolved
     * @throws UnresolvedException if an error occurs during resolving
     * @throws SyntaxException if an error occurs during tokenization
     */
    public static Map<String, String> resolveEnvironmentVariables(
            Map<String, String> environmentVariables, Map<String, String> variables)
            throws UnresolvedException, SyntaxException {
        Map<String, String> resolvedEnvironmentVariables = new TreeMap<>();

        // Iterate over the environment variables resolving them
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            String key = entry.getKey();
            String resolvedString = entry.getValue();
            String previousString;

            do {
                previousString = resolvedString;
                resolvedString = resolveAll(environmentVariables, variables, resolvedString);
            } while (!resolvedString.equals(previousString));

            // Tokenize the resolved string
            List<Token> tokens = Parser.parse(resolvedString);

            // Iterate over the tokens checking for unresolved environment variables
            for (Token token : tokens) {
                switch (token.getType()) {
                    case VARIABLE: {
                        throw new UnresolvedException(format("unresolved variable [%s]", token.getText()));
                    }
                    case SCOPED_VARIABLE: {
                        throw new UnresolvedException(format("unresolved scoped variable [%s]", token.getText()));
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
     * Method to resolve a map of variables
     *
     * @param variables the variables
     * @return a map with variables resolved
     * @throws UnresolvedException if an error occurs during resolving
     * @throws SyntaxException if an error occurs during tokenization
     */
    public static Map<String, String> resolveVariables(Map<String, String> variables)
            throws UnresolvedException, SyntaxException {
        Map<String, String> resolvedVariables = new TreeMap<>();

        // Iterate over the variables, resolving them
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String key = entry.getKey();
            String resolvedString = entry.getValue();
            String previousString;

            do {
                previousString = resolvedString;
                resolvedString = resolveVariablesSinglePass(variables, resolvedString);
            } while (!resolvedString.equals(previousString));

            // Parse the resolved string
            List<Token> parsedTokens = Parser.parse(resolvedString);

            // Iterate over the tokens checking for unresolved variables
            for (Token token : parsedTokens) {
                switch (token.getType()) {
                    case VARIABLE: {
                        throw new UnresolvedException(format("unresolved variable [%s]", token.getText()));
                    }
                    case SCOPED_VARIABLE: {
                        throw new UnresolvedException(format("unresolved scoped variable [%s]", token.getText()));
                    }
                    default: {
                        break;
                    }
                }
            }

            // Add resolved variable
            resolvedVariables.put(key, resolvedString);
        }

        return resolvedVariables;
    }

    /**
     * Method to resolve variables in a string
     *
     * @param variables the variables
     * @param input the input string
     * @return a string with variables resolved
     * @throws SyntaxException if an error occurs during tokenization
     */
    public static String resolveVariables(Map<String, String> variables, String input) throws SyntaxException {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();

        // Tokenize the input string and iterate over the tokens
        for (Token token : Parser.parse(input)) {
            // Get the next token
            if (token.getType() == Token.Type.VARIABLE) {
                // Resolve the VARIABLE token value
                String value = variables.getOrDefault(
                        token.cast(VariableToken.class).getScopedValue(), DEFAULT_VARIABLE_VALUE);

                // Append the resolved value
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
        for (Token token : Parser.parse(input)) {
            // Get the next token
            if (token.getType() == Token.Type.ENVIRONMENT_VARIABLE) {
                // Resolve the ENVIRONMENT_VARIABLE token value
                String value = environmentVariables.getOrDefault(
                        token.cast(EnvironmentVariableToken.class).getValue(), DEFAULT_ENVIRONMENT_VARIABLE_VALUE);

                // Append the resolved value
                result.append(value);
            } else {
                // Append the text
                result.append(token.getText());
            }
        }

        return result.toString();
    }

    /**
     * Method to resolve both environment variables and variables in a string
     *
     * @param variables the variables
     * @param input the input string
     * @return a string with variables resolved
     * @throws UnresolvedException if an error occurs during resolving
     * @throws SyntaxException if an error occurs during tokenization
     */
    private static String resolveVariablesSinglePass(Map<String, String> variables, String input)
            throws UnresolvedException, SyntaxException {
        StringBuilder stringBuilder = new StringBuilder();

        // Iterate over the tokens, resolving variables
        for (Token token : Parser.parse(input)) {
            switch (token.getType()) {
                case VARIABLE: {
                    String value = variables.getOrDefault(
                            token.cast(VariableToken.class).getScopedValue(), DEFAULT_VARIABLE_VALUE);

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
                    stringBuilder.append(
                            token.cast(EnvironmentVariableToken.class).getText());

                    break;
                }
                case TEXT: {
                    stringBuilder.append(token.cast(TextToken.class).getValue());

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
     * Method to resolve both environment variables and variables in a string
     *
     * @param environmentVariables the environment variables
     * @param variables the variables
     * @param input the input string
     * @return a string with environment variables and variables resolved
     * @throws UnresolvedException if an error occurs during resolving
     * @throws SyntaxException if an error occurs during tokenization
     */
    private static String resolveAll(
            Map<String, String> environmentVariables, Map<String, String> variables, String input)
            throws UnresolvedException, SyntaxException {
        StringBuilder stringBuilder = new StringBuilder();

        for (Token token : Parser.parse(input)) {
            switch (token.getType()) {
                case VARIABLE: {
                    String value = variables.getOrDefault(
                            token.cast(VariableToken.class).getValue(), DEFAULT_VARIABLE_VALUE);

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
                    String value = environmentVariables.getOrDefault(
                            token.cast(EnvironmentVariableToken.class).getValue(), DEFAULT_ENVIRONMENT_VARIABLE_VALUE);

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
