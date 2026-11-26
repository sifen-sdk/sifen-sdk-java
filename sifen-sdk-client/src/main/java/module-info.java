/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * SIFEN web service client: environments, mutual TLS per issuer, typed results.
 *
 * <p>Depends on the JDK and the SDK core only (ADR-0002). Packages under {@code internal} are not
 * exported.
 */
module io.github.sifensdk.client {
  requires transitive io.github.sifensdk.core;
  requires static transitive org.jspecify;
  requires java.net.http;
  requires java.xml;

  exports io.github.sifensdk.client;
}
