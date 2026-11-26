/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core;

/**
 * Base of every exception thrown by the SIFEN SDK.
 *
 * <p>Unchecked (ADR-0006). A rejection by SIFEN is never an exception: it is a result value.
 * Messages never contain secrets nor the contents of a document.
 */
public abstract class SifenException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception.
   *
   * @param message what went wrong, without secrets or document contents
   */
  protected SifenException(String message) {
    super(message);
  }

  /**
   * Creates the exception with a cause.
   *
   * @param message what went wrong, without secrets or document contents
   * @param cause the cause
   */
  protected SifenException(String message, Throwable cause) {
    super(message, cause);
  }
}
