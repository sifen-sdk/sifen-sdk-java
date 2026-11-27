/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Offline core of the SIFEN SDK: document model, XML, validation, CDC, signing and QR.
 *
 * <p>Depends on the JDK only (ADR-0002). Packages under {@code internal} are not exported, except
 * the keys held in a token or by a remote signer, to the SDK client only, for mutual TLS
 * (ADR-0012).
 */
@SuppressWarnings("module") // the client module is not on the core's module path
module io.github.sifensdk.core {
  requires static transitive org.jspecify;
  requires java.xml;
  requires java.xml.crypto;

  exports io.github.sifensdk.core;
  exports io.github.sifensdk.core.model;
  exports io.github.sifensdk.core.validation;
  exports io.github.sifensdk.core.signing;
  exports io.github.sifensdk.core.cdc;
  exports io.github.sifensdk.core.qr;
  exports io.github.sifensdk.core.internal.keys to
      io.github.sifensdk.client;
}
