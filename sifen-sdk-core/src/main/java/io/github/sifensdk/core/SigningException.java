/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core;

/** The document could not be signed, or the key material could not be loaded or used. */
public final class SigningException extends SifenException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception.
   *
   * @param message what went wrong
   */
  public SigningException(String message) {
    super(message);
  }

  /**
   * Creates the exception with a cause.
   *
   * @param message what went wrong
   * @param cause the cause
   */
  public SigningException(String message, Throwable cause) {
    super(message, cause);
  }
}
