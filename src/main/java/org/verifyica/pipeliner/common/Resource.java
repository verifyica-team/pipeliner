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

package org.verifyica.pipeliner.common;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement Resource */
public class Resource {

    private static final Logger LOGGER = LoggerFactory.getLogger(Resource.class);

    private final String path;
    private final String content;

    /**
     * Constructor
     *
     * @param path Path to the resource
     * @throws IOException If an error occurs
     */
    private Resource(String path) throws IOException {
        this.path = path;
        this.content = load(path);
    }

    /**
     * Method to get the resource path
     *
     * @return the resource path
     */
    public String path() {
        return path;
    }

    /**
     * Method to get the resource content
     *
     * @return the resource content
     */
    public String content() {
        return content;
    }

    /**
     * Method to create a new Resource
     *
     * @param path path
     * @return a Resource
     * @throws IOException If an error occurs
     */
    public static Resource of(String path) throws IOException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("loading resource [%s]", path);
        }

        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }

        if (path.trim().isEmpty()) {
            throw new IllegalArgumentException("path is blank");
        }

        return new Resource(path.trim());
    }

    /**
     * Method to load a resource
     *
     * @param path path
     * @return content content
     * @throws IOException If an error occurs
     */
    private static String load(String path) throws IOException {
        InputStream inputStream = Resource.class.getResourceAsStream(path);
        if (inputStream == null) {
            throw new IOException(format("resource [%s] not found", path));
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (content.length() > 0) {
                    content.append(System.lineSeparator());
                }
                content.append(line);
            }

            return content.toString();
        }
    }
}
