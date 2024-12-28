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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

/** Class to implement Sha256Checksum */
public class Sha256 {

    private static final String ALGORITHM_SHA_256 = "SHA-256";

    private static final int BUFFER_SIZE_BYTES = 16384;

    /** Constructor */
    private Sha256() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to calculate the SHA-256 checksum of a file
     *
     * @param file path
     * @return the SHA-256 checksum
     * @throws ChecksumException If an error occurs
     */
    public static String checksum(Path file) throws ChecksumException {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM_SHA_256);
            try (BufferedInputStream bufferedInputStream =
                    new BufferedInputStream(Files.newInputStream(file), BUFFER_SIZE_BYTES)) {
                byte[] buffer = new byte[BUFFER_SIZE_BYTES];
                int bytesRead;
                while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString().toLowerCase();
        } catch (Throwable t) {
            throw new ChecksumException("error calculating SHA-256 checksum", t);
        }
    }
}
