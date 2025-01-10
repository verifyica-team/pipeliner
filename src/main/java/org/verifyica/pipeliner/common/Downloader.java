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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;
import org.verifyica.pipeliner.model.EnvironmentVariableName;

/** Class to implement Downloader */
public class Downloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);

    private static final String HTTP_PREFIX = "http://";

    private static final String HTTPS_PREFIX = "https://";

    private static final String FILE_PREFIX = "file://";

    private static final String TEMPORARY_DIRECTORY_PREFIX = "pipeliner-extension-";

    private static final String TEMPORARY_DIRECTORY_SUFFIX = "";

    private static final int BUFFER_SIZE_BYTES = 16384;

    private static final Set<PosixFilePermission> PERMISSIONS = PosixFilePermissions.fromString("rwx------");

    private static final String PROPERTY_MATCHING_REGEX = "(?<!\\\\)\\$\\{\\{\\s*([a-zA-Z0-9\\-_.]+)\\s*}}";

    private static final String PIPELINER_EXTENSION_HTTP_USERNAME = "pipeliner.extension.http.username";

    private static final String PIPELINER_EXTENSION_USERNAME = "pipeliner.extension.username";

    private static final String PIPELINER_EXTENSION_HTTP_PASSWORD = "pipeliner.extension.http.password";

    private static final String PIPELINER_EXTENSION_PASSWORD = "pipeliner.extension.password";

    private static final String PIPELINER_EXTENSION_HTTP_CONNECT_TIMEOUT = "pipeliner.extension.http.connect.timeout";

    private static final String PIPELINER_EXTENSION_CONNECT_TIMEOUT = "pipeliner.extension.connect.timeout";

    private static final String PIPELINER_EXTENSION_HTTP_READ_TIMEOUT = "pipeliner.extension.http.read.timeout";

    private static final String PIPELINER_EXTENSION_READ_TIMEOUT = "pipeliner.extension.read.timeout";

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String BASIC_PREFIX = "Basic ";

    private static final Pattern PATTERN = Pattern.compile(PROPERTY_MATCHING_REGEX);

    /** Constructor */
    private Downloader() {
        // INTENTIONALLY BLANK
    }

    /**
     * Download a file
     *
     * @param environmentVariables environment variables
     * @param properties properties
     * @param url URL of the file
     * @return the path to the downloaded file
     * @throws IOException If an error occurs
     */
    public static Path download(Map<String, String> environmentVariables, Map<String, String> properties, String url)
            throws IOException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("downloading file ...");
            LOGGER.trace("URL [%s]", url);
        }

        String lowerCaseUrl = url.toLowerCase(Locale.US);
        Path archiveFile = Files.createTempFile(TEMPORARY_DIRECTORY_PREFIX, TEMPORARY_DIRECTORY_SUFFIX);
        Files.setPosixFilePermissions(archiveFile, PERMISSIONS);
        ShutdownHook.deleteOnExit(archiveFile);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("file [%s]", archiveFile);
        }

        if (lowerCaseUrl.startsWith(HTTP_PREFIX) || lowerCaseUrl.startsWith(HTTPS_PREFIX)) {
            URL webUrl = URI.create(url).toURL();
            URLConnection connection = webUrl.openConnection();

            String username = properties.get(PIPELINER_EXTENSION_HTTP_USERNAME);
            if (username == null) {
                username = properties.get(PIPELINER_EXTENSION_USERNAME);
                // TODO deprecation warning
            }

            String password = properties.get(PIPELINER_EXTENSION_HTTP_PASSWORD);
            if (password == null) {
                password = properties.get(PIPELINER_EXTENSION_PASSWORD);
                // TODO deprecation warning
            }

            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                username = resolvePropertyValue(environmentVariables, properties, username);
                password = resolvePropertyValue(environmentVariables, properties, password);

                String usernamePassword = username + ":" + password;
                String authorizationHeader =
                        BASIC_PREFIX + Base64.getEncoder().encodeToString(usernamePassword.getBytes());

                connection.setRequestProperty(AUTHORIZATION_HEADER, authorizationHeader);
            }

            String connectTimeout = properties.get(PIPELINER_EXTENSION_HTTP_CONNECT_TIMEOUT);
            if (connectTimeout == null) {
                connectTimeout = properties.get(PIPELINER_EXTENSION_CONNECT_TIMEOUT);
                // TODO deprecation warning
            }

            if (connectTimeout != null && !connectTimeout.isEmpty()) {
                connection.setConnectTimeout(Integer.parseInt(connectTimeout));
            }

            String readTimeout = properties.get(PIPELINER_EXTENSION_HTTP_READ_TIMEOUT);
            if (readTimeout == null) {
                readTimeout = properties.get(PIPELINER_EXTENSION_READ_TIMEOUT);
                // TODO deprecation warning
            }

            if (readTimeout != null && !readTimeout.isEmpty()) {
                connection.setReadTimeout(Integer.parseInt(readTimeout));
            }

            try (BufferedInputStream bufferedInputStream =
                            new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE_BYTES);
                    BufferedOutputStream bufferedOutputStream =
                            new BufferedOutputStream(Files.newOutputStream(archiveFile))) {
                byte[] buffer = new byte[BUFFER_SIZE_BYTES];
                int bytesRead;
                while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                }
            }
        } else {
            String fileUrl = url;

            if (lowerCaseUrl.startsWith(FILE_PREFIX)) {
                fileUrl = fileUrl.replace(FILE_PREFIX, "");
            }

            Path filePath = new File(fileUrl).toPath();

            Files.copy(filePath, archiveFile, StandardCopyOption.REPLACE_EXISTING);
            Files.setPosixFilePermissions(archiveFile, PERMISSIONS);
        }

        return archiveFile;
    }

    /**
     * Method to resolve a property
     *
     * @param environmentVariables env
     * @param properties with
     * @param string string
     * @return the string with properties resolved
     */
    private static String resolvePropertyValue(
            Map<String, String> environmentVariables, Map<String, String> properties, String string) {
        if (string == null) {
            return null;
        }

        String resolvedString = string;
        String previous;

        do {
            previous = resolvedString;
            Matcher matcher = PATTERN.matcher(resolvedString);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String key = matcher.group(1).trim();
                String value = properties.get(key);

                if (value == null) {
                    value = environmentVariables.get(key);
                    if (value == null) {
                        value = matcher.group(0);
                    }
                }

                matcher.appendReplacement(result, Matcher.quoteReplacement(value));
            }

            matcher.appendTail(result);
            resolvedString = result.toString();
        } while (!resolvedString.equals(previous));

        if (string.startsWith("$") && EnvironmentVariableName.isValid(string.substring(1))) {
            resolvedString = environmentVariables.get(string.substring(1));
        }

        return resolvedString;
    }
}
