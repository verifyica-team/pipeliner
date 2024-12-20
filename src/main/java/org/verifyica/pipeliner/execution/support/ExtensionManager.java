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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.verifyica.pipeliner.common.ArchiveExtractor;
import org.verifyica.pipeliner.common.Downloader;
import org.verifyica.pipeliner.common.Sha256Checksum;
import org.verifyica.pipeliner.common.Sha256ChecksumException;

/** Class to implement ExtensionManager */
public class ExtensionManager {

    private static final ExtensionManager INSTANCE = new ExtensionManager();

    private static final String FILE_URL_PREFIX = "file://";

    private static final String EXECUTE_SHELL_SCRIPT = "execute.sh";

    private static final Set<PosixFilePermission> PERMISSIONS = PosixFilePermissions.fromString("rwx------");

    private final Map<String, Path> cache = new HashMap<>();

    /** Constructor */
    private ExtensionManager() {
        // INTENTIONALLY BLANK
    }

    /**
     * Get the extension shell script
     *
     * @param url URL of the extension
     * @param sha256CheckSum SHA-256 checksum of the extension (optional)
     * @return the path to the execute file
     * @throws IOException If an error occurs
     * @throws Sha256ChecksumException If the SHA-256 checksum is invalid
     */
    public synchronized Path getExtensionShellScript(String url, String sha256CheckSum)
            throws IOException, Sha256ChecksumException {
        // Strip the file URL prefix if present
        String lowerCaseUrl = url.toLowerCase();
        if (lowerCaseUrl.startsWith(FILE_URL_PREFIX)) {
            url = url.substring(FILE_URL_PREFIX.length());
        }

        // Check if the extension shell script is already in the cache
        Path extensionShellScript = cache.get(url);
        if (extensionShellScript != null) {
            return extensionShellScript;
        }

        // Download the extension archive
        Path extensionArchive = Downloader.download(url);

        // Check the SHA-256 checksum if provided
        if (sha256CheckSum != null) {
            String actualSha256Checksum = Sha256Checksum.calculateChecksum(extensionArchive);
            if (!actualSha256Checksum.equalsIgnoreCase(sha256CheckSum)) {
                throw new Sha256ChecksumException(
                        format("invalid SHA-256 checksum for [%s] expected [%s]", url, sha256CheckSum));
            }
        }

        // Extract the extension archive
        ArchiveExtractor.Type type = ArchiveExtractor.getType(url);
        Path extensionExtractedArchiveDirectory = ArchiveExtractor.extract(extensionArchive, type);

        // Get the execute shell script
        extensionShellScript = extensionExtractedArchiveDirectory.resolve(EXECUTE_SHELL_SCRIPT);

        // Check if the execute shell script exists
        if (!Files.exists(extensionShellScript)) {
            throw new IOException(format("execute.sh not found in extension [%s]", url));
        }

        // Check if the execute shell script is a file
        if (!Files.isRegularFile(extensionShellScript)) {
            throw new IOException(format("extension [execute.sh] found in extension [%s] is not a file", url));
        }

        // Set execute shell script to be executable
        Files.setPosixFilePermissions(extensionShellScript, PERMISSIONS);

        // Put the extension shell script in the cache
        cache.put(url, extensionShellScript);

        return extensionShellScript;
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
