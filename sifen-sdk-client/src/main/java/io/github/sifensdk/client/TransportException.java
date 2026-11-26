/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import io.github.sifensdk.core.SifenException;

/**
 * SIFEN could not be reached, or the connection failed (network, TLS, timeout).
 *
 * <p>When {@link #outcomeUnknown()} is true the request may have reached SIFEN: <strong>do not send
 * it again</strong>. Query the document by CDC (or the batch by protocol number) to learn its
 * state. SIFEN temporarily blocks a taxpayer that resends a document still being processed
 * (ADR-0007).
 */
public final class TransportException extends SifenException {

  private static final long serialVersionUID = 1L;

  private final boolean outcomeUnknown;

  /**
   * Creates the exception.
   *
   * @param message what failed, without secrets or document contents
   * @param outcomeUnknown whether the request may have reached SIFEN
   * @param cause the cause
   */
  public TransportException(String message, boolean outcomeUnknown, Throwable cause) {
    super(message, cause);
    this.outcomeUnknown = outcomeUnknown;
  }

  /**
   * Whether the request may have reached SIFEN, so its outcome is unknown.
   *
   * @return true when the request must not be resent before querying its state
   */
  public boolean outcomeUnknown() {
    return outcomeUnknown;
  }
}
