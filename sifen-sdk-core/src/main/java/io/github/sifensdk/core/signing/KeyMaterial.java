/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.signing;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * The private key and certificate used to sign documents (and to authenticate the TLS connection to
 * SIFEN).
 *
 * <p>ADR-0008: signing asks this interface for the key, not a file, so the same steps work for a
 * PKCS#12 file, a {@link java.security.KeyStore}, PEM, a PKCS#11 token or HSM, or any other
 * provider. The private key may be non-extractable (PKCS#11): the SDK only passes it to {@link
 * java.security.Signature} and never reads its encoded form. {@link KeyMaterials} has the usual
 * implementations.
 *
 * <p>Implementations must be thread-safe and must not reveal secrets in {@code toString()}.
 */
public interface KeyMaterial {

  /**
   * Returns the private key that signs.
   *
   * @return the private key; possibly a handle to a key that cannot leave its token
   */
  PrivateKey privateKey();

  /**
   * Returns the certificate of the private key (the signer's certificate), which goes in {@code
   * KeyInfo/X509Data/X509Certificate} of the signature.
   *
   * @return the signer's certificate
   */
  X509Certificate certificate();

  /**
   * Returns the certificate chain, starting with {@link #certificate()}.
   *
   * @return an unmodifiable list with at least one certificate
   */
  List<X509Certificate> certificateChain();
}
