/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Denominación de la tarjeta.
 *
 * <p>SIFEN catalog {@code denominacion-tarjeta}, used by fields {@code E621}. Each constant carries
 * the code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/denominacion-tarjeta.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum CardBrand implements CatalogCode {
  /** {@code 1}: Visa. */
  VISA("1", "Visa"),

  /** {@code 2}: Mastercard. */
  MASTERCARD("2", "Mastercard"),

  /** {@code 3}: American Express. */
  AMERICAN_EXPRESS("3", "American Express"),

  /** {@code 4}: Maestro. */
  MAESTRO("4", "Maestro"),

  /** {@code 5}: Panal. */
  PANAL("5", "Panal"),

  /** {@code 6}: Cabal. */
  CABAL("6", "Cabal"),

  /** {@code 99}: Otro. */
  OTHER("99", "Otro");

  private final String code;
  private final String description;

  CardBrand(String code, String description) {
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
  public static Optional<CardBrand> fromCode(String code) {
    for (CardBrand value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
