/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.validation;

import io.github.sifensdk.core.SifenException;
import org.jspecify.annotations.Nullable;

/**
 * Thrown when an operation requires a valid document or event (for example, signing) and it is not.
 *
 * <p>The validation result is not serialized: its messages may quote the document's data, which
 * must not travel with a serialized exception. A deserialized instance has no result.
 */
public final class ValidationException extends SifenException {

  private static final long serialVersionUID = 1L;

  private final transient @Nullable ValidationResult result;

  /**
   * Creates the exception.
   *
   * @param result the validation result with at least one error
   */
  public ValidationException(ValidationResult result) {
    super("Validation found " + result.errors().size() + " error(s); see result()");
    this.result = result;
  }

  /**
   * The validation result.
   *
   * @return the validation result; null only if this exception was deserialized (the result is not
   *     serialized)
   */
  public @Nullable ValidationResult result() {
    return result;
  }
}
