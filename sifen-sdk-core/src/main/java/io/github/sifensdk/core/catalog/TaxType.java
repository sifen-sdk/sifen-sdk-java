/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de impuesto afectado.
 *
 * <p>SIFEN catalog {@code tipo-impuesto}, used by fields {@code D013}. Each constant carries the
 * code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-impuesto.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum TaxType implements CatalogCode {
  /** {@code 1}: IVA. */
  VAT("1", "IVA"),

  /** {@code 2}: ISC. */
  ISC("2", "ISC"),

  /** {@code 3}: Renta. */
  INCOME_TAX("3", "Renta"),

  /** {@code 4}: Ninguno. */
  NONE("4", "Ninguno"),

  /** {@code 5}: IVA – Renta. */
  VAT_AND_INCOME_TAX("5", "IVA - Renta");

  private final String code;
  private final String description;

  TaxType(String code, String description) {
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
  public static Optional<TaxType> fromCode(String code) {
    for (TaxType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
