/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.KeyMaterials;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * The synthetic client certificate of the core's tests ({@code prueba.p12}, password {@code
 * prueba-sintetica}; see {@code sifen-sdk-core/src/test/resources/signing/README.md}), read from
 * its place in the repository instead of copying the key.
 */
final class SyntheticClient {

  static final Path PKCS12 =
      Path.of("..", "sifen-sdk-core", "src", "test", "resources", "signing", "prueba.p12");
  static final char[] PASSWORD = "prueba-sintetica".toCharArray();

  private SyntheticClient() {}

  static KeyMaterial keyMaterial() {
    return KeyMaterials.pkcs12(PKCS12, PASSWORD);
  }

  static KeyStore keyStore() {
    try (InputStream in = Files.newInputStream(PKCS12)) {
      KeyStore store = KeyStore.getInstance("PKCS12");
      store.load(in, PASSWORD);
      return store;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  static X509Certificate certificate() {
    try {
      return (X509Certificate) keyStore().getCertificate("firma");
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * A TLS context that trusts the simulated SIFEN and presents the synthetic certificate or not.
   */
  static SSLContext tls(SimulatedSifen sifen, boolean presentCertificate) {
    try {
      TrustManagerFactory tmf =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(sifen.serverTrustStore());
      KeyManagerFactory kmf =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(keyStore(), PASSWORD);
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(presentCertificate ? kmf.getKeyManagers() : null, tmf.getTrustManagers(), null);
      return context;
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }
}
