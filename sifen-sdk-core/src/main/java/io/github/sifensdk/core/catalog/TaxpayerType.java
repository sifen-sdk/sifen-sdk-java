/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de contribuyente.
 *
 * <p>SIFEN catalog {@code tipo-contribuyente}, used by fields {@code D103}, {@code D205}, {@code
 * GENFE007}. Each constant carries the code that travels in the XML and the official description
 * the SDK writes next to it. Builders also accept the code as a {@code String}, for codes SIFEN
 * adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-contribuyente.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum TaxpayerType implements CatalogCode {
  /** {@code 1}: Persona Física. */
  INDIVIDUAL("1", "Persona Física"),

  /** {@code 2}: Persona Jurídica. */
  LEGAL_ENTITY("2", "Persona Jurídica");

  private final String code;
  private final String description;

  TaxpayerType(String code, String description) {
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
  public static Optional<TaxpayerType> fromCode(String code) {
    for (TaxpayerType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
