/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core;

import java.net.URI;

/**
 * A SIFEN environment.
 *
 * <p>The environment is always chosen explicitly by the caller; the SDK has no default and keeps no
 * global state, so documents for different environments can be handled in the same process.
 *
 * <p>Source: Manual Técnico v150 and Nota Técnica 10 (QR consultation addresses).
 */
public enum Environment {
  /** The test environment ({@code sifen-test.set.gov.py}). */
  TEST(
      URI.create("https://sifen-test.set.gov.py"),
      URI.create("https://ekuatia.set.gov.py/consultas-test/qr")),

  /** The production environment ({@code sifen.set.gov.py}). */
  PRODUCTION(
      URI.create("https://sifen.set.gov.py"),
      URI.create("https://ekuatia.set.gov.py/consultas/qr"));

  private final URI webServiceBaseUri;
  private final URI qrBaseUri;

  Environment(URI webServiceBaseUri, URI qrBaseUri) {
    this.webServiceBaseUri = webServiceBaseUri;
    this.qrBaseUri = qrBaseUri;
  }

  /**
   * Returns the base address of the SIFEN web services in this environment.
   *
   * @return the base address, without a trailing slash
   */
  public URI webServiceBaseUri() {
    return webServiceBaseUri;
  }

  /**
   * Returns the address the QR code of a document points to, without the query string.
   *
   * @return the QR consultation address
   */
  public URI qrBaseUri() {
    return qrBaseUri;
  }
}
