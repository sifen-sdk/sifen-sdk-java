/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de combustible.
 *
 * <p>SIFEN catalog {@code tipo-combustible}, used by fields {@code E779}. Each constant carries the
 * code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-combustible.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum FuelType implements CatalogCode {
  /** {@code 1}: Gasolina. */
  GASOLINE("1", "Gasolina"),

  /** {@code 2}: Diésel. */
  DIESEL("2", "Diésel"),

  /** {@code 3}: Etanol. */
  ETHANOL("3", "Etanol"),

  /** {@code 4}: GNV. */
  NATURAL_GAS("4", "GNV"),

  /** {@code 5}: Flex. */
  FLEX("5", "Flex"),

  /** {@code 9}: Otro. */
  OTHER("9", "Otro");

  private final String code;
  private final String description;

  FuelType(String code, String description) {
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
  public static Optional<FuelType> fromCode(String code) {
    for (FuelType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
