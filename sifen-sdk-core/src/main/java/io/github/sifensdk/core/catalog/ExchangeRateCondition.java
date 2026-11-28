/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Condición del tipo de cambio.
 *
 * <p>SIFEN catalog {@code condicion-tipo-cambio}, used by fields {@code D017}. Each constant
 * carries the code that travels in the XML and the official description the SDK writes next to it.
 * Builders also accept the code as a {@code String}, for codes SIFEN adds before this enum knows
 * them.
 *
 * <p>Generated from {@code spec/catalogos/condicion-tipo-cambio.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum ExchangeRateCondition implements CatalogCode {
  /** {@code 1}: Global (un solo tipo de cambio para todo el DE). */
  GLOBAL("1", "Global (un solo tipo de cambio para todo el DE)"),

  /** {@code 2}: Por ítem (tipo de cambio distinto por ítem). */
  PER_ITEM("2", "Por ítem (tipo de cambio distinto por ítem)");

  private final String code;
  private final String description;

  ExchangeRateCondition(String code, String description) {
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
  public static Optional<ExchangeRateCondition> fromCode(String code) {
    for (ExchangeRateCondition value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
