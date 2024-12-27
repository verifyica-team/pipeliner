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

import c4451848.org.kamranzafar.jtar.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Class to implement ArchiveExtractor */
public class ArchiveExtractor {

    private static final String TEMPORARY_DIRECTORY_TAR_GZ = "pipeliner-extension-tar-gz-";

    private static final String TEMPORARY_DIRECTORY_ZIP = "pipeliner-extension-zip-";

    private static final int BUFFER_SIZE_BYTES = 16384;

    private static final Set<PosixFilePermission> PERMISSIONS = PosixFilePermissions.fromString("rwx------");

    /** Enum to represent the archive type */
    public enum ArchiveType {
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
    public static ArchiveType getArchiveType(String name) {
        if (name.toLowerCase().endsWith(".zip")) {
            return ArchiveType.ZIP;
        } else if (name.toLowerCase().endsWith(".tar.gz")) {
            return ArchiveType.TAR_GZ;
        } else {
            throw new IllegalArgumentException(format("unsupported extension format [%s]", name));
        }
    }

    /**
     * Extract the archive
     *
     * @param file the file
     * @param archiveType the archive type
     * @return the extracted path
     * @throws IOException If an error occurs
     */
    public static Path extract(Path file, ArchiveType archiveType) throws IOException {
        if (archiveType == ArchiveType.TAR_GZ) {
            return extractTarGz(file);
        } else if (archiveType == ArchiveType.ZIP) {
            return extractZip(file);
        } else {
            throw new IllegalArgumentException(format("unsupported extension format [%s]", archiveType));
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
        Path archiveDirectory = Files.createTempDirectory(TEMPORARY_DIRECTORY_ZIP);
        ShutdownHook.deleteOnExit(archiveDirectory);
        setPermissions(archiveDirectory);

        try (ZipInputStream zipInputStream =
                new ZipInputStream(new BufferedInputStream(Files.newInputStream(file), BUFFER_SIZE_BYTES))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path entryPath = archiveDirectory.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                    setPermissions(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    setPermissions(entryPath.getParent());
                    try (BufferedOutputStream bufferedOutputStream =
                            new BufferedOutputStream(Files.newOutputStream(entryPath), BUFFER_SIZE_BYTES)) {
                        byte[] buffer = new byte[BUFFER_SIZE_BYTES];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            bufferedOutputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        }

        return archiveDirectory;
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
        setPermissions(archiveDirectory);

        try (TarInputStream tarInputStream = new TarInputStream(
                new GZIPInputStream(new BufferedInputStream(Files.newInputStream(file)), BUFFER_SIZE_BYTES))) {
            TarEntry entry;
            while ((entry = tarInputStream.getNextEntry()) != null) {
                Path entryPath = archiveDirectory.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                    setPermissions(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    setPermissions(entryPath.getParent());
                    try (BufferedOutputStream bufferedOutputStream =
                            new BufferedOutputStream(Files.newOutputStream(entryPath), BUFFER_SIZE_BYTES)) {
                        byte[] buffer = new byte[BUFFER_SIZE_BYTES];
                        int bytesRead;
                        while ((bytesRead = tarInputStream.read(buffer)) != -1) {
                            bufferedOutputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        }

        return archiveDirectory;
    }

    /**
     * Set the permissions
     *
     * @param path the path
     * @throws IOException If an error occurs
     */
    private static void setPermissions(Path path) throws IOException {
        Files.setPosixFilePermissions(path, PERMISSIONS);

        if (Files.isDirectory(path)) {
            try (Stream<Path> paths = Files.walk(path)) {
                for (Path p : paths.toArray(Path[]::new)) {
                    Files.setPosixFilePermissions(p, PERMISSIONS);
                }
            }
        }
    }
}
