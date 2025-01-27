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

package org.verifyica.pipeliner.core.support;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
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
import org.verifyica.pipeliner.common.ShutdownHooks;
import org.verifyica.pipeliner.core.EnvironmentVariable;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

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

    private static final String VARIABLE_MATCHING_REGEX = "(?<!\\\\)\\$\\{\\{\\s*([a-zA-Z0-9\\-_.]+)\\s*}}";

    private static final Pattern VARIABLE_MATCHING_PATTERN = Pattern.compile(VARIABLE_MATCHING_REGEX);

    private static final Matcher VARIABLE_MATCHING_MATCHER = VARIABLE_MATCHING_PATTERN.matcher("");

    private static final String PIPELINER_EXTENSION_HTTP_USERNAME = "pipeliner_extension_http_username";

    private static final String PIPELINER_EXTENSION_HTTP_PASSWORD = "pipeliner_extension_http_password";

    private static final String PIPELINER_EXTENSION_HTTP_CONNECT_TIMEOUT = "pipeliner_extension_http_connect_timeout";

    private static final String PIPELINER_EXTENSION_HTTP_READ_TIMEOUT = "pipeliner_extension_http_read_timeout";

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String BASIC_PREFIX = "Basic ";

    /**
     * Constructor
     */
    private Downloader() {
        // INTENTIONALLY BLANK
    }

    /**
     * Download a file
     *
     * @param environmentVariables the environment variables
     * @param variables the variables
     * @param url the URL of the file
     * @return the path to the downloaded file
     * @throws IOException if an I/O error occurs
     */
    public static Path download(Map<String, String> environmentVariables, Map<String, String> variables, String url)
            throws IOException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("downloading file ...");
            LOGGER.trace("URL [%s]", url);
        }

        String lowerCaseUrl = url.toLowerCase(Locale.ROOT);
        Path archiveFile = Files.createTempFile(TEMPORARY_DIRECTORY_PREFIX, TEMPORARY_DIRECTORY_SUFFIX);
        Files.setPosixFilePermissions(archiveFile, PERMISSIONS);
        ShutdownHooks.deleteOnExit(archiveFile);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("file [%s]", archiveFile);
        }

        if (lowerCaseUrl.startsWith(HTTP_PREFIX) || lowerCaseUrl.startsWith(HTTPS_PREFIX)) {
            URL webUrl = URI.create(url).toURL();
            URLConnection connection = webUrl.openConnection();

            // Get the extension HTTP username
            String username = variables.get(PIPELINER_EXTENSION_HTTP_USERNAME);

            // Get the extension HTTP password
            String password = variables.get(PIPELINER_EXTENSION_HTTP_PASSWORD);

            // If the username and password are not empty, set the authorization header
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                username = resolveVariableValue(environmentVariables, variables, username);
                password = resolveVariableValue(environmentVariables, variables, password);

                String usernamePassword = username + ":" + password;
                String authorizationHeader = BASIC_PREFIX
                        + Base64.getEncoder().encodeToString(usernamePassword.getBytes(StandardCharsets.UTF_8));

                connection.setRequestProperty(AUTHORIZATION_HEADER, authorizationHeader);
            }

            // Get the extension HTTP connect timeout
            String connectTimeout = variables.get(PIPELINER_EXTENSION_HTTP_CONNECT_TIMEOUT);

            if (connectTimeout != null && !connectTimeout.isEmpty()) {
                connection.setConnectTimeout(Integer.parseInt(connectTimeout));
            }

            // Get the extension HTTP read timeout
            String readTimeout = variables.get(PIPELINER_EXTENSION_HTTP_READ_TIMEOUT);

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
     * Method to resolve a variable value
     *
     * @param environmentVariables the environment variables
     * @param variables the variables
     * @param string the string
     * @return the string with variables resolved
     */
    private static String resolveVariableValue(
            Map<String, String> environmentVariables, Map<String, String> variables, String string) {
        if (string == null) {
            return null;
        }

        String resolvedString = string;
        String previous;

        do {
            previous = resolvedString;
            VARIABLE_MATCHING_MATCHER.reset(resolvedString);
            StringBuffer result = new StringBuffer();

            while (VARIABLE_MATCHING_MATCHER.find()) {
                String key = VARIABLE_MATCHING_MATCHER.group(1).trim();
                String value = variables.get(key);

                if (value == null) {
                    value = environmentVariables.get(key);
                    if (value == null) {
                        value = VARIABLE_MATCHING_MATCHER.group(0);
                    }
                }

                VARIABLE_MATCHING_MATCHER.appendReplacement(result, Matcher.quoteReplacement(value));
            }

            VARIABLE_MATCHING_MATCHER.appendTail(result);
            resolvedString = result.toString();
        } while (!resolvedString.equals(previous));

        if (string.startsWith("$") && EnvironmentVariable.isValid(string.substring(1))) {
            resolvedString = environmentVariables.get(string.substring(1));
        }

        return resolvedString;
    }
}
