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

package org.verifyica.pipeliner.execution;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.verifyica.pipeliner.common.ArchiveExtractor;
import org.verifyica.pipeliner.common.Checksum;
import org.verifyica.pipeliner.common.ChecksumException;
import org.verifyica.pipeliner.common.Downloader;
import org.verifyica.pipeliner.common.LRUCache;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement ExtensionManager */
public class ExtensionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

    private static final String FILE_URL_PREFIX = "file://";

    private static final Set<PosixFilePermission> PERMISSIONS = PosixFilePermissions.fromString("rwx------");

    private final LRUCache<String, Path> cache;

    /**
     * Extension shell scripts used to run an extension
     */
    public static final String[] SHELL_SCRIPTS = new String[] {"run.sh", "execute.sh", "entrypoint.sh", "ENTRYPOINT"};

    /** Constructor */
    public ExtensionManager() {
        this.cache = new LRUCache<>(25);
    }

    /**
     * Get the extension shell script
     *
     * @param environmentVariables environment variables
     * @param properties the properties
     * @param workingDirectory the working directory
     * @param url the URL of the extension
     * @param checksum the checksum of the extension (optional)
     * @return the path to the execute file
     * @throws IOException if an I/O error occurs
     * @throws ChecksumException If the SHA-256 checksum is invalid
     */
    public synchronized Path getShellScript(
            Map<String, String> environmentVariables,
            Map<String, String> properties,
            String workingDirectory,
            String url,
            String checksum)
            throws IOException, ChecksumException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getting extension shell script ...");
            LOGGER.trace("workingDirectory [%s]", workingDirectory);
            LOGGER.trace("URL [%s]", url);
            LOGGER.trace("checksum [%s]", checksum);
        }

        String downloadUrl;

        // Strip the file URL prefix if present
        if (url.toLowerCase(Locale.US).startsWith(FILE_URL_PREFIX)) {
            downloadUrl = url.substring(FILE_URL_PREFIX.length());
        } else {
            downloadUrl = url;
        }

        // Resolve the download URL to an absolute path based on the working directory
        downloadUrl = Paths.get(workingDirectory)
                .resolve(downloadUrl)
                .normalize()
                .toAbsolutePath()
                .toString();

        // Check if the extension already in the cache using the shell script as the key
        Path shellScript = cache.get(downloadUrl);
        if (shellScript != null) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("extension found in cache [%s]", downloadUrl);
            }

            return shellScript;
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("extension not found in cache [%s]", downloadUrl);
        }

        // Download the extension archive
        Path extensionArchive = Downloader.download(environmentVariables, properties, downloadUrl);

        // Check checksum if provided
        if (checksum != null) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("validating checksum ...");
            }

            // Get the checksum algorithm
            Checksum.Algorithm algorithm = Checksum.getAlgorithm(checksum);

            // Calculate the checksum of the extension archive
            String actualChecksum = Checksum.checksum(algorithm, extensionArchive);

            // Check if the actual checksum matches the expected checksum
            if (!actualChecksum.equalsIgnoreCase(checksum)) {
                throw new ChecksumException(
                        format("invalid %s checksum for [%s] expected [%s]", algorithm, downloadUrl, checksum));
            }
        }

        // Get the archive type
        ArchiveExtractor.ArchiveType archiveType = ArchiveExtractor.getArchiveType(downloadUrl);

        // Extract the extension archive
        Path extensionExtractedArchiveDirectory = ArchiveExtractor.extract(extensionArchive, archiveType);

        // Iterate through the possible shell scripts to find the first one that exists
        for (String scriptName : SHELL_SCRIPTS) {
            Path candidate = extensionExtractedArchiveDirectory.resolve(scriptName);

            if (Files.exists(candidate)) {
                shellScript = candidate;
                break;
            }
        }

        // If no shell script was found, throw an exception
        if (shellScript == null) {
            throw new IOException(format(
                    "extension [%s] must contain one of the following shell scripts [%s]",
                    downloadUrl, String.join("], [", SHELL_SCRIPTS)));
        }

        // Check if the shell script is a regular file
        if (!Files.isRegularFile(shellScript)) {
            throw new IOException(format(
                    "extension shell script [%s] found in extension [%s] is not a file",
                    shellScript.getFileName(), downloadUrl));
        }

        // Set extension shell script to be executable
        Files.setPosixFilePermissions(shellScript, PERMISSIONS);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("extension shell script [%s]", shellScript);
        }

        // Cache the extension using the shell script as the key
        cache.put(downloadUrl, shellScript);

        return shellScript;
    }
}
