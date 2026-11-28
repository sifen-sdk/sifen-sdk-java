/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de régimen (Tabla 1).
 *
 * <p>SIFEN catalog {@code tipo-regimen}, used by fields {@code D104}. Each constant carries the
 * code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-regimen.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum RegimeType implements CatalogCode {
  /** {@code 1}: Régimen de Turismo. */
  TOURISM("1", "Régimen de Turismo"),

  /** {@code 2}: Importador. */
  IMPORTER("2", "Importador"),

  /** {@code 3}: Exportador. */
  EXPORTER("3", "Exportador"),

  /** {@code 4}: Maquila. */
  MAQUILA("4", "Maquila"),

  /** {@code 5}: Ley N° 60/90. */
  LAW_60_90("5", "Ley N° 60/90"),

  /** {@code 6}: Régimen del Pequeño Productor. */
  SMALL_PRODUCER("6", "Régimen del Pequeño Productor"),

  /** {@code 7}: Régimen del Mediano Productor. */
  MEDIUM_PRODUCER("7", "Régimen del Mediano Productor"),

  /** {@code 8}: Régimen Contable. */
  ACCOUNTING("8", "Régimen Contable");

  private final String code;
  private final String description;

  RegimeType(String code, String description) {
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
  public static Optional<RegimeType> fromCode(String code) {
    for (RegimeType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
