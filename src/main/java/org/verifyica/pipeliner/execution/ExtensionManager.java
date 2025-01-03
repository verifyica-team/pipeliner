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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.verifyica.pipeliner.common.ArchiveExtractor;
import org.verifyica.pipeliner.common.Checksum;
import org.verifyica.pipeliner.common.ChecksumException;
import org.verifyica.pipeliner.common.Downloader;

/** Class to implement ExtensionManager */
public class ExtensionManager {

    private static final ExtensionManager INSTANCE = new ExtensionManager();

    private static final String FILE_URL_PREFIX = "file://";

    private static final String EXECUTE_SHELL_SCRIPT = "execute.sh";

    private static final String RUN_SHELL_SCRIPT = "run.sh";

    private static final Set<PosixFilePermission> PERMISSIONS = PosixFilePermissions.fromString("rwx------");

    private final Map<String, Path> cache = new HashMap<>();

    /** Constructor */
    private ExtensionManager() {
        // INTENTIONALLY BLANK
    }

    /**
     * Get the extension shell script
     *
     * @param environmentVariables environment variables
     * @param properties properties
     * @param workingDirectory working directory
     * @param url URL of the extension
     * @param checksum checksum of the extension (optional)
     * @return the path to the execute file
     * @throws IOException If an error occurs
     * @throws ChecksumException If the SHA-256 checksum is invalid
     */
    public synchronized Path getExtensionShellScript(
            Map<String, String> environmentVariables,
            Map<String, String> properties,
            String workingDirectory,
            String url,
            String checksum)
            throws IOException, ChecksumException {
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

        // Check if the extension shell script is already in the cache
        Path shellScript = cache.get(downloadUrl);
        if (shellScript != null) {
            return shellScript;
        }

        // Download the extension archive
        Path extensionArchive = Downloader.download(environmentVariables, properties, downloadUrl);

        // Check checksum if provided
        if (checksum != null) {
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

        // Get the execute.sh shell script
        shellScript = extensionExtractedArchiveDirectory.resolve(EXECUTE_SHELL_SCRIPT);

        // Check if an execute.sh shell script exists
        if (Files.exists(shellScript)) {
            // Check if the execute.sh shell script is a file
            if (!Files.isRegularFile(shellScript)) {
                throw new IOException(
                        format("extension [execute.sh] found in extension [%s] is not a file", downloadUrl));
            }
        } else {
            // Get the run.sh shell script
            shellScript = extensionExtractedArchiveDirectory.resolve(RUN_SHELL_SCRIPT);

            // Check if a run.sh shell script exists
            if (!Files.exists(shellScript)) {
                throw new IOException(format("extension [%s] must contains either execute.sh or run.sh", downloadUrl));
            }

            // Check if the run.sh shell script is a file
            if (!Files.isRegularFile(shellScript)) {
                throw new IOException(format("extension [run.sh] found in extension [%s] is not a file", downloadUrl));
            }
        }

        // Set execute shell script to be executable
        Files.setPosixFilePermissions(shellScript, PERMISSIONS);

        // Put the extension shell script in the cache
        cache.put(downloadUrl, shellScript);

        return shellScript;
    }

    /**
     * Get the singleton instance of ExtensionManager
     *
     * @return the singleton instance of ExtensionManager
     */
    public static ExtensionManager getInstance() {
        return INSTANCE;
    }
}
