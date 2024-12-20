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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

/** Class to implement Extractor */
public class Extractor {

    private static final String TEMPORARY_DIRECTORY_TAR_GZ = "pipeliner-extension-tar-gz-";

    private static final String TEMPORARY_DIRECTORY_ZIP = "pipeliner-extension-zip-";

    private static final int BUFFER_SIZE_BYTES = 16384;

    private static final FileAttribute<?> PERMISSIONS =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));

    /** Enum to represent the archive type */
    public enum ArchiveType {
        /** TAR_GZ */
        TAR_GZ,
        /** ZIP */
        ZIP;

        /**
         * Decode the archive type
         *
         * @param filename the filename
         * @return the archive type
         */
        public static ArchiveType decode(String filename) {
            String name = filename.toLowerCase();
            if (name.endsWith(".tar.gz")) {
                return TAR_GZ;
            } else if (name.endsWith(".zip")) {
                return ZIP;
            } else {
                throw new IllegalArgumentException(format("unsupported package format [%s]", name));
            }
        }
    }

    /** Constructor */
    private Extractor() {
        // INTENTIONALLY BLANK
    }

    /**
     * Extract the archive
     *
     * @param path the path
     * @param archiveType the archive type
     * @return the extracted path
     * @throws IOException If an error occurs
     */
    public static Path extract(Path path, ArchiveType archiveType) throws IOException {
        if (archiveType == ArchiveType.TAR_GZ) {
            return extractTarGz(path);
        } else if (archiveType == ArchiveType.ZIP) {
            return extractZip(path);
        } else {
            throw new IllegalArgumentException(format("unsupported package format [%s]", archiveType));
        }
    }

    /**
     * Extract the zip archive
     *
     * @param path the path
     * @return the extracted path
     * @throws IOException If an error occurs
     */
    private static Path extractZip(Path path) throws IOException {
        Path temporaryDirectoryPath = Files.createTempDirectory(TEMPORARY_DIRECTORY_ZIP, PERMISSIONS);
        ShutdownHook.deleteOnExit(temporaryDirectoryPath);

        try (InputStream fileIn = Files.newInputStream(path);
                ZipInputStream zipIn = new ZipInputStream(fileIn)) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                Path entryPath = temporaryDirectoryPath.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (OutputStream out = Files.newOutputStream(entryPath)) {
                        byte[] buffer = new byte[BUFFER_SIZE_BYTES];
                        int bytesRead;
                        while ((bytesRead = zipIn.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        }

        return temporaryDirectoryPath;
    }

    /**
     * Extract the tar.gz archive
     *
     * @param path the path
     * @return the extracted path
     * @throws IOException If an error occurs
     */
    private static Path extractTarGz(Path path) throws IOException {
        Path temporaryDirectoryPath = Files.createTempDirectory(TEMPORARY_DIRECTORY_TAR_GZ, PERMISSIONS);
        ShutdownHook.deleteOnExit(temporaryDirectoryPath);

        try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(path));
                TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream))) {
            ArchiveEntry archiveEntry;
            while ((archiveEntry = tar.getNextEntry()) != null) {
                Path extractTo = temporaryDirectoryPath.resolve(archiveEntry.getName());
                if (archiveEntry.isDirectory()) {
                    Files.createDirectories(extractTo);
                } else {
                    Files.copy(tar, extractTo);
                }
            }
        }

        return temporaryDirectoryPath;
    }
}
