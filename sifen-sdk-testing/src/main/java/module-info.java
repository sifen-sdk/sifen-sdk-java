/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Testing support for SIFEN SDK users: a simulated SIFEN, local and without network.
 *
 * <p>Depends on the JDK (including {@code jdk.httpserver}) and the SDK client only. It does not
 * depend on any test framework.
 */
module io.github.sifensdk.testing {
  requires transitive io.github.sifensdk.client;
  requires static org.jspecify;
  requires java.xml;
  requires jdk.httpserver;

  exports io.github.sifensdk.testing;
}
