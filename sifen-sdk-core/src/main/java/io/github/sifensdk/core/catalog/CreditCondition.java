/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Condición de la operación a crédito.
 *
 * <p>SIFEN catalog {@code condicion-credito}, used by fields {@code E641}. Each constant carries
 * the code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/condicion-credito.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum CreditCondition implements CatalogCode {
  /** {@code 1}: Plazo. */
  TERM("1", "Plazo"),

  /** {@code 2}: Cuota. */
  INSTALLMENTS("2", "Cuota");

  private final String code;
  private final String description;

  CreditCondition(String code, String description) {
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
  public static Optional<CreditCondition> fromCode(String code) {
    for (CreditCondition value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
