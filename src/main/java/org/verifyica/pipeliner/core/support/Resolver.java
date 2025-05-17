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

package org.verifyica.pipeliner.core.support;

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

    /**
     * Constructor
     */
    private Resolver() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to resolve all variables
     *
     * @param environmentVariables the environment variables
     * @param variables the variables
     * @param input the input
     * @return a string with all variables resolved
     * @throws UnresolvedException if an error occurs during resolving
     * @throws SyntaxException if an error occurs during tokenization
     */
    public static String resolveAllVariables(
            Map<String, String> environmentVariables, Map<String, String> variables, String input)
            throws UnresolvedException, SyntaxException {
        String current = input;
        String resolved;

        while (true) {
            resolved = resolveEnvironmentVariables(environmentVariables, current);
            resolved = resolveVariables(variables, resolved);

            // Exit the loop if there are no changes
            if (resolved.equals(current)) {
                break;
            }

            // Update current for the next iteration
            current = resolved;
        }

        // Tokenize the resolved string
        List<ParsedToken> parsedTokens = Parser.parse(resolved);

        // Iterate over the tokens checking for unresolved environment variables
        for (ParsedToken parsedToken : parsedTokens) {
            switch (parsedToken.getType()) {
                case VARIABLE: {
                    throw new UnresolvedException(format("unresolved variable [%s]", parsedToken.getText()));
                }
                case SCOPED_VARIABLE: {
                    throw new UnresolvedException(format("unresolved scoped variable [%s]", parsedToken.getText()));
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

        return resolved;
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
            String resolved = entry.getValue();
            String previous;

            do {
                previous = resolved;
                resolved = resolveAll(environmentVariables, variables, resolved);
            } while (!resolved.equals(previous));

            // Tokenize the resolved string
            List<ParsedToken> parsedTokens = Parser.parse(resolved);

            // Iterate over the tokens checking for unresolved environment variables
            for (ParsedToken parsedToken : parsedTokens) {
                switch (parsedToken.getType()) {
                    case VARIABLE: {
                        throw new UnresolvedException(format("unresolved variable [%s]", parsedToken.getText()));
                    }
                    case SCOPED_VARIABLE: {
                        throw new UnresolvedException(format("unresolved scoped variable [%s]", parsedToken.getText()));
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
            resolvedEnvironmentVariables.put(key, resolved);
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
            List<ParsedToken> parsedParsedTokens = Parser.parse(resolvedString);

            // Iterate over the tokens checking for unresolved variables
            for (ParsedToken parsedToken : parsedParsedTokens) {
                switch (parsedToken.getType()) {
                    case VARIABLE: {
                        throw new UnresolvedException(format("unresolved variable [%s]", parsedToken.getText()));
                    }
                    case SCOPED_VARIABLE: {
                        throw new UnresolvedException(format("unresolved scoped variable [%s]", parsedToken.getText()));
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
     * @throws UnresolvedException if an error occurs during resolving
     * @throws SyntaxException if an error occurs during tokenization
     */
    public static String resolveVariables(Map<String, String> variables, String input)
            throws UnresolvedException, SyntaxException {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();

        // Tokenize the input string and iterate over the tokens
        for (ParsedToken parsedToken : Parser.parse(input)) {
            // Get the next token
            if (parsedToken.getType() == ParsedToken.Type.VARIABLE) {
                ParsedVariable parsedVariable = parsedToken.cast(ParsedVariable.class);
                String value;

                if (parsedVariable.hasModifier(Modifier.REQUIRED)) {
                    value = variables.get(parsedVariable.getScopedValue());

                    if (value == null) {
                        throw new UnresolvedException(
                                format("unresolved required variable [%s]", parsedToken.getText()));
                    }
                } else {
                    value = variables.getOrDefault(parsedVariable.getScopedValue(), DEFAULT_VARIABLE_VALUE);
                }

                // Append the resolved value
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
    public static String resolveEnvironmentVariables(Map<String, String> environmentVariables, String input)
            throws SyntaxException {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();

        // Tokenize the input string and iterate over the tokens
        for (ParsedToken parsedToken : Parser.parse(input)) {
            // Get the next token
            if (parsedToken.getType() == ParsedToken.Type.ENVIRONMENT_VARIABLE) {
                // Resolve the ENVIRONMENT_VARIABLE token value
                String value = environmentVariables.getOrDefault(
                        parsedToken.cast(ParsedEnvironmentVariable.class).getValue(),
                        DEFAULT_ENVIRONMENT_VARIABLE_VALUE);

                // Append the resolved value
                result.append(value);
            } else {
                // Append the text
                result.append(parsedToken.getText());
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
        for (ParsedToken parsedToken : Parser.parse(input)) {
            switch (parsedToken.getType()) {
                case VARIABLE: {
                    ParsedVariable parsedVariable = parsedToken.cast(ParsedVariable.class);
                    String value;

                    if (parsedVariable.hasModifier(Modifier.REQUIRED)) {
                        value = variables.get(parsedVariable.getScopedValue());

                        if (value == null) {
                            throw new UnresolvedException(
                                    format("unresolved required variable [%s]", parsedToken.getText()));
                        }
                    } else {
                        value = variables.getOrDefault(parsedVariable.getScopedValue(), DEFAULT_VARIABLE_VALUE);
                    }

                    stringBuilder.append(value);

                    break;
                }
                case ENVIRONMENT_VARIABLE: {
                    stringBuilder.append(
                            parsedToken.cast(ParsedEnvironmentVariable.class).getText());

                    break;
                }
                case TEXT: {
                    stringBuilder.append(parsedToken.cast(ParsedText.class).getValue());

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

        for (ParsedToken parsedToken : Parser.parse(input)) {
            switch (parsedToken.getType()) {
                case VARIABLE: {
                    String value = variables.getOrDefault(
                            parsedToken.cast(ParsedVariable.class).getValue(), DEFAULT_VARIABLE_VALUE);

                    // Code left in the event that we want to throw an exception if a variable is unresolved
                    /*
                    if (value == null) {
                        throw new ResolverException(format("unresolved variable [%s]", parsedToken.getText()));
                    }
                    */

                    stringBuilder.append(value);
                    break;
                }
                case ENVIRONMENT_VARIABLE: {
                    String value = environmentVariables.getOrDefault(
                            parsedToken.cast(ParsedEnvironmentVariable.class).getValue(),
                            DEFAULT_ENVIRONMENT_VARIABLE_VALUE);

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
