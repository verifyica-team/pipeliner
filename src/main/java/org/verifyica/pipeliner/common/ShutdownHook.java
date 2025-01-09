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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.verifyica.pipeliner.Constants;
import org.verifyica.pipeliner.logger.Logger;
import org.verifyica.pipeliner.logger.LoggerFactory;

/** Class to implement ShutdownHook */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class ShutdownHook {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHook.class);

    private static final boolean DISABLED;

    static {
        String value = System.getenv(Constants.PIPELINER_DISABLE_SHUTDOWN_HOOK);
        if (value != null && (value.equalsIgnoreCase(Constants.TRUE) || value.equalsIgnoreCase(Constants.ONE))) {
            DISABLED = true;
        } else {
            DISABLED = false;
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("shutdown hooks disabled [%b]", DISABLED);
        }
    }

    /** Constructor */
    private ShutdownHook() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to register a shutdown hook to delete a path and all sub paths
     *
     * @param path path
     */
    public static void deleteOnExit(Path path) {
        if (DISABLED) {
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                deleteRecursively(path);
            } catch (IOException e) {
                // INTENTIONALLY BLANK
            }
        }));
    }

    /**
     * Method to recursively delete path and all sub paths
     *
     * @param path path
     * @throws IOException IOException
     */
    private static void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes basicFileAttributes) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException ioException) throws IOException {
                Files.delete(directory);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
