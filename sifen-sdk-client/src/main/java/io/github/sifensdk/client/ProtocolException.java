/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import io.github.sifensdk.core.SifenException;
import org.jspecify.annotations.Nullable;

/**
 * SIFEN answered with something that is not a valid response of the service: an HTML page (for
 * example the load balancer's "access denied" page when the client certificate is rejected, NT 16),
 * a SOAP fault, or XML that does not match the service.
 *
 * <p>The raw exchange is kept for diagnosis. The outcome of a submission is unknown: query before
 * resending.
 */
public final class ProtocolException extends SifenException {

  private static final long serialVersionUID = 1L;

  private final transient @Nullable Exchange exchange;

  /**
   * Creates the exception.
   *
   * @param message what was wrong with the response, without document contents
   * @param exchange the raw exchange, if any
   */
  public ProtocolException(String message, @Nullable Exchange exchange) {
    super(message);
    this.exchange = exchange;
  }

  /**
   * The raw exchange with SIFEN.
   *
   * @return the exchange, or null if no response was received
   */
  public @Nullable Exchange exchange() {
    return exchange;
  }
}
