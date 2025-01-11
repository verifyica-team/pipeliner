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
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement ArchiveExtractor */
public class ArchiveExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveExtractor.class);

    private static final String ZIP_EXTENSIONS = ".zip";

    private static final String TAR_GZ_EXTENSION = ".tar.gz";

    private static final String TEMPORARY_DIRECTORY_TAR_GZ = "pipeliner-extension-tar-gz-";

    private static final String TEMPORARY_DIRECTORY_ZIP = "pipeliner-extension-zip-";

    private static final int BUFFER_SIZE_BYTES = 16384;

    private static final Set<PosixFilePermission> PERMISSIONS = PosixFilePermissions.fromString("rwx------");

    /** Enum to represent the archive type */
    public enum ArchiveType {

        /** TAR_GZ */
        TAR_GZ,
        /** ZIP */
        ZIP
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
        String lowerCaseName = name.toLowerCase(Locale.US);

        if (lowerCaseName.endsWith(ZIP_EXTENSIONS)) {
            return ArchiveType.ZIP;
        } else if (lowerCaseName.endsWith(TAR_GZ_EXTENSION)) {
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
     * @throws IOException if an I/O error occurs
     */
    public static Path extract(Path file, ArchiveType archiveType) throws IOException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("extracting archive ...");
            LOGGER.trace("file [%s]", file);
            LOGGER.trace("archive type [%s]", archiveType);
        }

        if (archiveType == ArchiveType.TAR_GZ) {
            return extractTarGz(file);
        } else if (archiveType == ArchiveType.ZIP) {
            return extractZip(file);
        } else {
            throw new IllegalArgumentException(format("unsupported extension format [%s]", archiveType));
        }
    }

    /**
     * Extract a zip archive
     *
     * @param file the file
     * @return the extracted path
     * @throws IOException if an I/O error occurs
     */
    private static Path extractZip(Path file) throws IOException {
        Path archiveDirectory = Files.createTempDirectory(TEMPORARY_DIRECTORY_ZIP);
        ShutdownHook.deleteOnExit(archiveDirectory);
        setPermissions(archiveDirectory);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("extracting zip archive ...");
            LOGGER.trace("file [%s]", file);
            LOGGER.trace("archive directory [%s]", archiveDirectory);
        }

        try (ZipInputStream zipInputStream =
                new ZipInputStream(new BufferedInputStream(Files.newInputStream(file), BUFFER_SIZE_BYTES))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                Path entryPath = archiveDirectory.resolve(zipEntry.getName());
                if (zipEntry.isDirectory()) {
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
     * Extract a tar.gz archive
     *
     * @param file the file
     * @return the extracted path
     * @throws IOException if an I/O error occurs
     */
    private static Path extractTarGz(Path file) throws IOException {
        Path archiveDirectory = Files.createTempDirectory(TEMPORARY_DIRECTORY_TAR_GZ);
        ShutdownHook.deleteOnExit(archiveDirectory);
        setPermissions(archiveDirectory);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("extracting tar.gz archive ...");
            LOGGER.trace("file [%s]", file);
            LOGGER.trace("archive directory [%s]", archiveDirectory);
        }

        try (TarInputStream tarInputStream = new TarInputStream(
                new GZIPInputStream(new BufferedInputStream(Files.newInputStream(file)), BUFFER_SIZE_BYTES))) {
            TarEntry tarEntry;
            while ((tarEntry = tarInputStream.getNextEntry()) != null) {
                Path entryPath = archiveDirectory.resolve(tarEntry.getName());
                if (tarEntry.isDirectory()) {
                    Files.createDirectories(entryPath);
                    setPermissions(entryPath);
                } else if (entryPath.getParent() != null) {
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
     * Set permissions
     *
     * @param path the path
     * @throws IOException if an I/O error occurs
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
