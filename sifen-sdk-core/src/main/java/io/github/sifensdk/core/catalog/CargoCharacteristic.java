/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Características de la carga.
 *
 * <p>SIFEN catalog {@code caracteristica-carga}, used by fields {@code G057}. Each constant carries
 * the code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/caracteristica-carga.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum CargoCharacteristic implements CatalogCode {
  /** {@code 1}: Mercaderías con cadena de frío. */
  COLD_CHAIN("1", "Mercaderías con cadena de frío"),

  /** {@code 2}: Carga peligrosa. */
  DANGEROUS_GOODS("2", "Carga peligrosa"),

  /** {@code 3}: Otro de características similares (especificar). */
  OTHER("3", "Otro de características similares (especificar)");

  private final String code;
  private final String description;

  CargoCharacteristic(String code, String description) {
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
  public static Optional<CargoCharacteristic> fromCode(String code) {
    for (CargoCharacteristic value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
