/*
 * Copyright (C) Pipeliner project authors and contributors
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Locale;

/** Class to implement Checksum */
public class Checksum {

    /** Class to implement Algorithm */
    public enum Algorithm {

        /** SHA-1 */
        SHA_1("SHA-1"),

        /** SHA-256 */
        SHA_256("SHA-256"),

        /** SHA-512 */
        SHA_512("SHA-512");

        private final String algorithm;

        /**
         * Constructor
         *
         * @param algorithm the algorithm
         */
        Algorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        /**
         * Method to get the value of the algorithm
         *
         * @return the algorithm
         */
        public String value() {
            return algorithm;
        }

        @Override
        public String toString() {
            return algorithm;
        }
    }

    private static final int BUFFER_SIZE_BYTES = 16384;

    /**
     * Constructor
     */
    private Checksum() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get the algorithm based on the checksum
     *
     * @param checksum the checksum
     * @return the algorithm
     * @throws ChecksumException If the algorithm cannot be determined
     */
    public static Algorithm getAlgorithm(String checksum) throws ChecksumException {
        Precondition.notBlank(checksum, "checksum is null", "checksum is blank");

        int length = checksum.length();
        switch (length) {
            case 40:
                return Algorithm.SHA_1;
            case 64:
                return Algorithm.SHA_256;
            case 256:
                return Algorithm.SHA_512;
            default:
                throw new ChecksumException(format("error decoding algorithm for checksum length [%d]", length));
        }
    }

    /**
     * Method to calculate the checksum of a file
     *
     * @param algorithm the algorithm
     * @param file the file
     * @return the checksum
     * @throws ChecksumException If the algorithm is not supported or an error occurs
     */
    public static String checksum(Algorithm algorithm, Path file) throws ChecksumException {
        Precondition.notNull(algorithm, "algorithm is null");
        Precondition.notNull(file, "file is null");

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.value());
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
                hexString.append(format("%02x", b));
            }

            return hexString.toString().toLowerCase(Locale.ROOT);
        } catch (Throwable t) {
            throw new ChecksumException("error calculating SHA-256 checksum", t);
        }
    }
}
