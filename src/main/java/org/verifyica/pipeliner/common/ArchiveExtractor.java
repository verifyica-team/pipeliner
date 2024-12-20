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
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

/** Class to implement Extractor */
public class ArchiveExtractor {

    private static final String TEMPORARY_DIRECTORY_TAR_GZ = "pipeliner-extension-tar-gz-";

    private static final String TEMPORARY_DIRECTORY_ZIP = "pipeliner-extension-zip-";

    private static final int BUFFER_SIZE_BYTES = 16384;

    private static final Set<PosixFilePermission> PERMISSIONS = PosixFilePermissions.fromString("rwx------");

    /** Enum to represent the archive type */
    public enum Type {
        /** TAR_GZ */
        TAR_GZ,
        /** ZIP */
        ZIP;
    }
    /** Constructor */
    private ArchiveExtractor() {
        // INTENTIONALLY BLANK
    }

    /**
     * Get the archive type
     *
     * @param name the name
     * @return the archive type
     */
    public static Type getType(String name) {
        if (name.toLowerCase().endsWith(".zip")) {
            return Type.ZIP;
        } else if (name.toLowerCase().endsWith(".tar.gz")) {
            return Type.TAR_GZ;
        } else {
            throw new IllegalArgumentException(format("unsupported extension format [%s]", name));
        }
    }

    /**
     * Extract the archive
     *
     * @param file the file
     * @param type the archive type
     * @return the extracted path
     * @throws IOException If an error occurs
     */
    public static Path extract(Path file, Type type) throws IOException {
        if (type == Type.TAR_GZ) {
            return extractTarGz(file);
        } else if (type == Type.ZIP) {
            return extractZip(file);
        } else {
            throw new IllegalArgumentException(format("unsupported extension format [%s]", type));
        }
    }

    /**
     * Extract the zip archive
     *
     * @param file the file
     * @return the extracted path
     * @throws IOException If an error occurs
     */
    private static Path extractZip(Path file) throws IOException {
        Path temporaryArchiveFile = Files.createTempDirectory(TEMPORARY_DIRECTORY_ZIP);
        ShutdownHook.deleteOnExit(temporaryArchiveFile);
        Files.setPosixFilePermissions(temporaryArchiveFile, PERMISSIONS);

        try (InputStream fileIn = Files.newInputStream(file);
                ZipInputStream zipIn = new ZipInputStream(fileIn)) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                Path entryPath = temporaryArchiveFile.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                    Files.setPosixFilePermissions(entryPath, PERMISSIONS);
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

        return temporaryArchiveFile;
    }

    /**
     * Extract the tar.gz archive
     *
     * @param file the file
     * @return the extracted path
     * @throws IOException If an error occurs
     */
    private static Path extractTarGz(Path file) throws IOException {
        Path archiveDirectory = Files.createTempDirectory(TEMPORARY_DIRECTORY_TAR_GZ);
        ShutdownHook.deleteOnExit(archiveDirectory);
        Files.setPosixFilePermissions(archiveDirectory, PERMISSIONS);

        try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file));
                TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream))) {
            ArchiveEntry archiveEntry;
            while ((archiveEntry = tar.getNextEntry()) != null) {
                Path extractTo = archiveDirectory.resolve(archiveEntry.getName());
                if (archiveEntry.isDirectory()) {
                    Files.createDirectories(extractTo);
                    Files.setPosixFilePermissions(extractTo, PERMISSIONS);
                } else {
                    Files.copy(tar, extractTo);
                }
            }
        }

        return archiveDirectory;
    }
}
