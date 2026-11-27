/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.keys;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Signature;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Signs with a key that only its own provider can use (a PKCS#11 token or HSM through a SunPKCS11
 * instance, or any non-extractable key of a {@link java.security.KeyStore}), without that provider
 * being installed.
 */
public final class ProviderBackend implements ExternalKey.Backend {

  private final Provider provider;
  private final PrivateKey key;
  private final String description;

  /**
   * Creates the backend.
   *
   * @param provider the provider that holds the key; it need not be installed
   * @param key the key handle
   * @param description a description without secrets
   */
  public ProviderBackend(Provider provider, PrivateKey key, String description) {
    this.provider = Objects.requireNonNull(provider, "provider");
    this.key = Objects.requireNonNull(key, "key");
    this.description = Objects.requireNonNull(description, "description");
  }

  @Override
  public boolean supports(String algorithm) {
    return provider.getService("Signature", algorithm) != null;
  }

  @Override
  public ExternalKey.Session open(String algorithm, @Nullable AlgorithmParameterSpec parameters)
      throws GeneralSecurityException {
    Signature signature = Signature.getInstance(algorithm, provider);
    if (parameters != null) {
      signature.setParameter(parameters);
    }
    signature.initSign(key);
    return new ExternalKey.Session() {
      @Override
      public void update(byte[] data, int offset, int length) throws GeneralSecurityException {
        signature.update(data, offset, length);
      }

      @Override
      public byte[] sign() throws GeneralSecurityException {
        return signature.sign();
      }
    };
  }

  @Override
  public String description() {
    return description;
  }
}
