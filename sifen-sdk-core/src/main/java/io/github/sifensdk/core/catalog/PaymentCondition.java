/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Condición de la operación.
 *
 * <p>SIFEN catalog {@code condicion-operacion}, used by fields {@code E601}. Each constant carries
 * the code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/condicion-operacion.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum PaymentCondition implements CatalogCode {
  /** {@code 1}: Contado. */
  CASH("1", "Contado"),

  /** {@code 2}: Crédito. */
  CREDIT("2", "Crédito");

  private final String code;
  private final String description;

  PaymentCondition(String code, String description) {
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
  public static Optional<PaymentCondition> fromCode(String code) {
    for (PaymentCondition value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
