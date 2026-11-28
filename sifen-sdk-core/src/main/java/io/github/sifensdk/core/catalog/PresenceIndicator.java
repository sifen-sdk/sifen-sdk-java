/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Indicador de presencia.
 *
 * <p>SIFEN catalog {@code indicador-presencia}, used by fields {@code E011}. Each constant carries
 * the code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/indicador-presencia.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum PresenceIndicator implements CatalogCode {
  /** {@code 1}: Operación presencial. */
  IN_PERSON("1", "Operación presencial"),

  /** {@code 2}: Operación electrónica. */
  ELECTRONIC("2", "Operación electrónica"),

  /** {@code 3}: Operación telemarketing. */
  TELEMARKETING("3", "Operación telemarketing"),

  /** {@code 4}: Venta a domicilio. */
  HOME_DELIVERY("4", "Venta a domicilio"),

  /** {@code 5}: Operación bancaria. */
  BANKING("5", "Operación bancaria"),

  /** {@code 6}: Operación cíclica. */
  RECURRING("6", "Operación cíclica"),

  /** {@code 9}: Otro. */
  OTHER("9", "Otro");

  private final String code;
  private final String description;

  PresenceIndicator(String code, String description) {
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
  public static Optional<PresenceIndicator> fromCode(String code) {
    for (PresenceIndicator value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
