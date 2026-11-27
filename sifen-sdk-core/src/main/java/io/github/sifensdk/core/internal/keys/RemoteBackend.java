/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.keys;

import io.github.sifensdk.core.signing.RemoteSigner;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Signs through a {@link RemoteSigner}: the data is hashed here with SHA-256 and only the 32-byte
 * digest reaches the signer, never the document. Every signature is verified with the certificate's
 * public key before it is used. RSASSA-PKCS1-v1_5 only, which is what SIFEN uses for XML signatures
 * and in its TLS 1.2 handshake.
 */
public final class RemoteBackend implements ExternalKey.Backend {

  private final RemoteSigner signer;
  private final PublicKey publicKey;
  private final int signatureLength;

  /**
   * Creates the backend.
   *
   * @param signer the remote signer
   * @param publicKey the public key of the signer's certificate, to verify each signature
   * @param modulusBits the size of the RSA key, to check the length of each signature
   */
  public RemoteBackend(RemoteSigner signer, PublicKey publicKey, int modulusBits) {
    this.signer = Objects.requireNonNull(signer, "signer");
    this.publicKey = Objects.requireNonNull(publicKey, "publicKey");
    this.signatureLength = (modulusBits + 7) / 8;
  }

  @Override
  public boolean supports(String algorithm) {
    return SifenKeysProvider.SHA256_WITH_RSA.equals(algorithm);
  }

  @Override
  public ExternalKey.Session open(String algorithm, @Nullable AlgorithmParameterSpec parameters)
      throws GeneralSecurityException {
    if (!supports(algorithm)) {
      throw new NoSuchAlgorithmException(
          "A remote signer only signs " + SifenKeysProvider.SHA256_WITH_RSA + ", not " + algorithm);
    }
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    // The same data is verified with the certificate's key: a signer with another key, or one that
    // hashes the digest again, fails here and not later at SIFEN.
    Signature verifier = Signature.getInstance(SifenKeysProvider.SHA256_WITH_RSA);
    verifier.initVerify(publicKey);
    return new ExternalKey.Session() {
      @Override
      public void update(byte[] data, int offset, int length) throws SignatureException {
        digest.update(data, offset, length);
        verifier.update(data, offset, length);
      }

      @Override
      public byte[] sign() throws SignatureException {
        byte[] signature = signer.signSha256Digest(digest.digest());
        if (signature == null || signature.length != signatureLength) {
          throw new SignatureException(
              "The remote signer returned a signature of "
                  + (signature == null ? "null" : signature.length + " bytes")
                  + "; an RSA signature of this key has "
                  + signatureLength);
        }
        byte[] copy = signature.clone();
        if (!verifier.verify(copy)) {
          throw new SignatureException(
              "The remote signer's signature does not verify with the certificate's public key");
        }
        return copy;
      }
    };
  }

  @Override
  public String description() {
    return "remote signer " + signer.getClass().getName();
  }
}
