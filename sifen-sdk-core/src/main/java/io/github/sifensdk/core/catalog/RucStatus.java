/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Estado del RUC consultado.
 *
 * <p>SIFEN catalog {@code estado-ruc}, used by fields {@code ContRUC04}. Each constant carries the
 * code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/estado-ruc.json} by {@code scripts/generar-catalogos.py};
 * do not edit by hand.
 */
public enum RucStatus implements CatalogCode {
  /** {@code ACT}: Activo. */
  ACTIVE("ACT", "Activo"),

  /** {@code SUS}: Suspensión Temporal. */
  TEMPORARILY_SUSPENDED("SUS", "Suspensión Temporal"),

  /** {@code SAD}: Suspensión Administrativa. */
  ADMINISTRATIVELY_SUSPENDED("SAD", "Suspensión Administrativa"),

  /** {@code BLQ}: Bloqueado. */
  BLOCKED("BLQ", "Bloqueado"),

  /** {@code CAN}: Cancelado. */
  CANCELLED("CAN", "Cancelado"),

  /** {@code CDE}: Cancelado Definitivo. */
  PERMANENTLY_CANCELLED("CDE", "Cancelado Definitivo");

  private final String code;
  private final String description;

  RucStatus(String code, String description) {
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
  public static Optional<RucStatus> fromCode(String code) {
    for (RucStatus value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
