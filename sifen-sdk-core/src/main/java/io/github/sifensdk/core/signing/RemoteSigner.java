/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.signing;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * A signer whose private key lives elsewhere: a remote signing service, a cloud KMS or HSM, or the
 * certification provider's custody (ADR-0008, ADR-0012). Adapt it with {@link
 * KeyMaterials#remote(RemoteSigner)}; it then signs documents and events and authenticates the TLS
 * connection to SIFEN, like any other key material.
 *
 * <p>The SDK computes what is signed and hashes it: the signer only receives a 32-byte SHA-256
 * digest, never the document or its data. For an XML signature that is the digest of the
 * canonicalized {@code SignedInfo}; in the TLS handshake, the digest of the handshake messages.
 *
 * <p>Implementations must be thread-safe and apply their own timeout: the SDK waits for {@link
 * #signSha256Digest(byte[])} to return. They must not reveal secrets in {@code toString()} or in
 * exception messages.
 */
public interface RemoteSigner {

  /**
   * Returns the certificate chain of the signing key, starting with the signer's certificate. The
   * SDK reads it once, when the key material is created.
   *
   * @return at least one certificate, with an RSA public key
   */
  List<X509Certificate> certificateChain();

  /**
   * Signs a SHA-256 digest with RSASSA-PKCS1-v1_5: the signature of the SHA-256 {@code DigestInfo}
   * that wraps it (RFC 8017, 9.2), as in AWS KMS {@code RSASSA_PKCS1_V1_5_SHA_256} with {@code
   * MessageType=DIGEST}, Azure Key Vault {@code RS256} or PKCS#11 {@code CKM_RSA_PKCS} over the
   * {@code DigestInfo}.
   *
   * @param sha256Digest the 32-byte digest; the array is the signer's to keep or clear
   * @return the signature, as long as the key's modulus (256 bytes for RSA 2048)
   * @throws RuntimeException if it cannot sign; the SDK reports it as a signing failure, with this
   *     exception as the cause
   */
  byte[] signSha256Digest(byte[] sha256Digest);
}
