/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de operación.
 *
 * <p>SIFEN catalog {@code tipo-operacion}, used by fields {@code D202}, {@code GENFE027}. Each
 * constant carries the code that travels in the XML and the official description the SDK writes
 * next to it. Builders also accept the code as a {@code String}, for codes SIFEN adds before this
 * enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-operacion.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum OperationType implements CatalogCode {
  /** {@code 1}: B2B. */
  B2B("1", "B2B"),

  /** {@code 2}: B2C. */
  B2C("2", "B2C"),

  /** {@code 3}: B2G. */
  B2G("3", "B2G"),

  /** {@code 4}: B2F. */
  B2F("4", "B2F");

  private final String code;
  private final String description;

  OperationType(String code, String description) {
    this.code = code;
    this.description = description;
  }

  @Override
  public String code() {
    return code;
  }

  @Override
  public String description() {
    return description;
  }

  /**
   * Returns the constant for a code.
   *
   * @param code the code as it travels in the XML
   * @return the constant, or empty if this version of the SDK does not know the code
   */
  public static Optional<OperationType> fromCode(String code) {
    for (OperationType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
