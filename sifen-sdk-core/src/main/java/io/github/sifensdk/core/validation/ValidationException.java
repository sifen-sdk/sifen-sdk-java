/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.validation;

import io.github.sifensdk.core.SifenException;

/** Thrown when an operation requires a valid document (for example, signing) and it is not. */
public final class ValidationException extends SifenException {

  private static final long serialVersionUID = 1L;

  private final transient ValidationResult result;

  /**
   * Creates the exception.
   *
   * @param result the validation result with at least one error
   */
  public ValidationException(ValidationResult result) {
    super("The document has " + result.errors().size() + " validation error(s); see result()");
    this.result = result;
  }

  /**
   * @return the validation result
   */
  public ValidationResult result() {
    return result;
  }
}
