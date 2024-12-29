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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class to implement EnvironmentVariableResolver */
public class CompleteResolver {

    private static final Pattern PROPERTY_TOKEN_PATTERN = Pattern.compile("\\$\\{\\{\\s*\\w+\\s*\\}\\}");

    private static final Pattern ENVIRONMENT_VARIABLE_TOKEN_PATTERN = Pattern.compile("\\$(\\w+)");

    /** Constructor */
    private CompleteResolver() {
        // INTENTIONALLY BLANK
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
        String workingString = PropertiesResolver.resolveProperties(properties, string);
        String previousString;

        do {
            previousString = workingString;
            workingString = resolveEnvironmentVariableTokens(environmentVariables, workingString);
            workingString = resolvePropertyTokens(properties, workingString);
        } while (!workingString.equals(previousString));

        return workingString.replaceAll(Pattern.quote("\\${{"), Matcher.quoteReplacement("${{"));
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
        Matcher matcher = ENVIRONMENT_VARIABLE_TOKEN_PATTERN.matcher(value);
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
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = PROPERTY_TOKEN_PATTERN.matcher(value);

        while (matcher.find()) {
            String token = matcher.group();
            String resolvedValue = properties.get(token);

            if (resolvedValue == null) {
                throw new ResolverException(format("unresolved property [%s]", token));
            }

            matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(resolvedValue));
        }

        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }
}
