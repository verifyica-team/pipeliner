/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Locale;

/** Class to implement Signature */
public class Signature {

    private static final String CERTIFICATE_X_509 = "X.509";
    private static final String BASE64_SUFFIX = ".base64";
    private static final String B64_SUFFIX = ".b64";

    /**
     * Constructor
     */
    private Signature() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to sign a file
     *
     * @param file the file to check
     * @param signatureAlgorithm the signature algorithm
     * @param signatureFile the signature file
     * @param certificateFile the certificate file
     * @return true if the file is signed with the signature, else false
     * @throws SignatureException if an error occurs
     */
    public static boolean validate(String file, String certificateFile, String signatureAlgorithm, String signatureFile)
            throws SignatureException {
        try {
            // Load the public key from the certificate
            X509Certificate certificate = loadCertificate(certificateFile);

            // Get the public key from the certificate
            PublicKey publicKey = certificate.getPublicKey();

            // Load the signature bytes
            byte[] signatureBytes = Files.readAllBytes(Paths.get(signatureFile));

            if (signatureFile.toLowerCase(Locale.ROOT).endsWith(BASE64_SUFFIX)
                    || signatureFile.toLowerCase(Locale.ROOT).endsWith(B64_SUFFIX)) {
                // Base64 encoded signature

                // Remove all whitespaces from the signature
                String base64Signature = new String(signatureBytes, StandardCharsets.US_ASCII).replaceAll("\\s", "");

                // Decode the base64 signature
                signatureBytes = Base64.getDecoder().decode(base64Signature);
            }

            // Create a Signature
            java.security.Signature signature = java.security.Signature.getInstance(signatureAlgorithm);

            // Initialize the Signature with the public key
            signature.initVerify(publicKey);

            // Load the file bytes
            byte[] fileBytes = Files.readAllBytes(Paths.get(file));

            // Update the signature with the file bytes
            signature.update(fileBytes);

            // Verify the signature
            return signature.verify(signatureBytes);
        } catch (java.security.SignatureException e) {
            return false;
        } catch (Throwable t) {
            throw new SignatureException("exception validating signature", t);
        }
    }

    /**
     * Load a certificate from a file
     *
     * @param filename the certificate file name
     * @return the X509 certificate
     * @throws IOException if an I/O error occurs
     * @throws CertificateException if an error occurs
     */
    private static X509Certificate loadCertificate(String filename) throws IOException, CertificateException {
        try (FileInputStream certFileInputStream = new FileInputStream(filename)) {
            return (X509Certificate)
                    CertificateFactory.getInstance(CERTIFICATE_X_509).generateCertificate(certFileInputStream);
        }
    }

    /**
     * Main method
     *
     * @param args the arguments
     * @throws Throwable if an error occurs
     */
    public static void main(String[] args) throws Throwable {
        if (args == null || args.length != 4) {
            System.out.println("Usage: Signature <FILE> <PUBLIC_CERTIFICATE> <SIGNATURE_ALGORITHM> <SIGNATURE>");
            System.exit(1);
        }

        if (validate(args[0], args[1], args[2], args[3])) {
            System.out.println("valid signature");
        } else {
            System.out.println("invalid signature");
            System.exit(1);
        }
    }
}
