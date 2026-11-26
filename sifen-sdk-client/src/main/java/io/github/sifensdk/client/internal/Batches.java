/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import io.github.sifensdk.client.*;
import io.github.sifensdk.core.signing.SignedDocument;
import java.util.List;

/** Placeholder: implemented in this phase. */
public final class Batches {
  private Batches() {}

  public static BatchSubmission send(Transport transport, List<SignedDocument> documents) {
    throw new UnsupportedOperationException();
  }

  public static BatchResult result(Transport transport, String protocolNumber) {
    throw new UnsupportedOperationException();
  }
}
