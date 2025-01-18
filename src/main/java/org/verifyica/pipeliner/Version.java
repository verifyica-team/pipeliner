/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

package org.verifyica.pipeliner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Class to implement Version */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Version {

    private static final String PIPELINER_PROPERTIES = "/pipeliner.properties";

    private static final String VERSION_KEY = "version";

    private static final String VERSION_UNKNOWN = "unknown";

    /**
     * Constructor
     */
    private Version() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get the version
     *
     * @return the version
     */
    public static String getVersion() {
        String value = VERSION_UNKNOWN;

        // Load the version from the properties file
        try (InputStream inputStream = Pipeliner.class.getResourceAsStream(PIPELINER_PROPERTIES)) {
            if (inputStream != null) {
                try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                    Properties properties = new Properties();
                    properties.load(inputStreamReader);
                    value = properties.getProperty(VERSION_KEY).trim();
                }
            }
        } catch (IOException e) {
            // INTENTIONALLY BLANK
        }

        return value;
    }
}
