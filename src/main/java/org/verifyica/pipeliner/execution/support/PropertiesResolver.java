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
import java.util.Optional;
import org.verifyica.pipeliner.execution.support.parser.PropertyParser;
import org.verifyica.pipeliner.execution.support.parser.PropertyParserException;
import org.verifyica.pipeliner.execution.support.parser.PropertyParserToken;

/** Class to implement PropertiesResolver */
public class PropertiesResolver {

    /** Constructor */
    private PropertiesResolver() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to resolve properties
     *
     * @param properties properties
     * @throws ResolverException ResolverException
     */
    public static void resolveProperties(Map<String, String> properties) throws ResolverException {
        try {
            boolean changesMade;

            do {
                changesMade = false;

                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    StringBuilder stringBuilder = new StringBuilder();
                    List<PropertyParserToken> tokens = PropertyParser.parse(value);

                    tokens.forEach(propertyParserToken -> {
                        switch (propertyParserToken.getType()) {
                            case PROPERTY: {
                                String property = propertyParserToken.getValue();
                                String resolvedProperty = properties.get(property);
                                if (resolvedProperty != null) {
                                    stringBuilder.append(resolvedProperty);
                                } else {
                                    stringBuilder.append(propertyParserToken.getToken());
                                }
                                break;
                            }
                            case TEXT: {
                                stringBuilder.append(propertyParserToken.getValue());
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                    });

                    String resolvedValue = stringBuilder.toString();
                    if (!value.equals(resolvedValue)) {
                        properties.put(key, resolvedValue);
                        changesMade = true;
                    }
                }
            } while (changesMade);

            for (Map.Entry<String, String> entry : properties.entrySet()) {
                Optional<PropertyParserToken> optionalPropertyParserToken =
                        PropertyParser.parse(entry.getValue()).stream()
                                .filter(propertyParserToken ->
                                        propertyParserToken.getType() == PropertyParserToken.Type.PROPERTY)
                                .findFirst();

                if (optionalPropertyParserToken.isPresent()) {
                    throw new ResolverException(format(
                            "unresolved property [%s]",
                            optionalPropertyParserToken.get().getValue()));
                }
            }
        } catch (PropertyParserException e) {
            throw new ResolverException("ParserException", e);
        }
    }

    /** Method to resolve properties
     *
     * @param properties properties
     * @throws ResolverException ResolverException
     */
    public static void resolveEnvironmentVariables(
            Map<String, String> properties, Map<String, String> environmentVariables) throws ResolverException {
        try {
            boolean changesMade;

            do {
                changesMade = false;

                for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    StringBuilder stringBuilder = new StringBuilder();
                    List<PropertyParserToken> tokens = PropertyParser.parse(value);

                    tokens.forEach(propertyParserToken -> {
                        switch (propertyParserToken.getType()) {
                            case PROPERTY: {
                                String tokenValue = propertyParserToken.getValue();
                                String resolvedTokenValue = properties.get(tokenValue);
                                if (resolvedTokenValue != null) {
                                    stringBuilder.append(resolvedTokenValue);
                                } else {
                                    stringBuilder.append(propertyParserToken.getToken());
                                }
                                break;
                            }
                            case TEXT: {
                                String tokenValue = propertyParserToken.getValue();
                                if (tokenValue.startsWith("$")) {
                                    String resolvedTokenValue = environmentVariables.get(tokenValue.substring(1));
                                    if (resolvedTokenValue != null) {
                                        stringBuilder.append(resolvedTokenValue);
                                    } else {
                                        stringBuilder.append(propertyParserToken.getValue());
                                    }
                                } else {
                                    stringBuilder.append(propertyParserToken.getValue());
                                }
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                    });

                    String resolvedValue = stringBuilder.toString();
                    if (!value.equals(resolvedValue)) {
                        environmentVariables.put(key, resolvedValue);
                        changesMade = true;
                    }
                }
            } while (changesMade);

            for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
                Optional<PropertyParserToken> optionalPropertyParserToken =
                        PropertyParser.parse(entry.getValue()).stream()
                                .filter(propertyParserToken ->
                                        propertyParserToken.getType() == PropertyParserToken.Type.PROPERTY)
                                .findFirst();

                if (optionalPropertyParserToken.isPresent()) {
                    throw new ResolverException(format(
                            "unresolved property [%s]",
                            optionalPropertyParserToken.get().getValue()));
                }
            }
        } catch (PropertyParserException e) {
            throw new ResolverException("ParserException", e);
        }
    }

    /**
     * Method to resolve properties in a string
     *
     * @param string string
     * @return a string with properties resolved
     * @throws ResolverException ResolverException
     */
    public static String resolveProperties(Map<String, String> properties, String string) throws ResolverException {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            List<PropertyParserToken> propertyParserTokens = PropertyParser.parse(string);

            for (PropertyParserToken propertyParserToken : propertyParserTokens) {
                switch (propertyParserToken.getType()) {
                    case TEXT: {
                        stringBuilder.append(propertyParserToken.getValue());
                        break;
                    }
                    case PROPERTY: {
                        stringBuilder.append(resolve(properties, propertyParserToken.getValue()));
                        break;
                    }
                    default: {
                        throw new ResolverException(format("unknown token type [%s]", propertyParserToken.getType()));
                    }
                }
            }

            return stringBuilder.toString();
        } catch (PropertyParserException e) {
            throw new ResolverException("ParserException", e);
        }
    }

    /**
     * Method to resolve a property
     *
     * @param properties properties
     * @param property property
     * @return resolved property
     * @throws ResolverException ResolverException
     */
    private static String resolve(Map<String, String> properties, String property) throws ResolverException {
        String value = properties.get(property);

        if (value == null) {
            throw new ResolverException(format("unresolved property [%s]", property));
        }

        if (value.startsWith("${{")) {
            return resolve(properties, value.substring(3, value.length() - 2).trim());
        } else {
            return value;
        }
    }
}
