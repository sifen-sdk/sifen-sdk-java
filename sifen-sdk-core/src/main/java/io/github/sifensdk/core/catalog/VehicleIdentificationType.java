/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de identificación del vehículo.
 *
 * <p>SIFEN catalog {@code tipo-identificacion-vehiculo}, used by fields {@code E967}, {@code
 * GET028}. Each constant carries the code that travels in the XML and the official description the
 * SDK writes next to it. Builders also accept the code as a {@code String}, for codes SIFEN adds
 * before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-identificacion-vehiculo.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum VehicleIdentificationType implements CatalogCode {
  /** {@code 1}: Número de identificación del vehículo. */
  VEHICLE_IDENTIFICATION_NUMBER("1", "Número de identificación del vehículo"),

  /** {@code 2}: Número de matrícula del vehículo. */
  LICENSE_PLATE("2", "Número de matrícula del vehículo");

  private final String code;
  private final String description;

  VehicleIdentificationType(String code, String description) {
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
  public static Optional<VehicleIdentificationType> fromCode(String code) {
    for (VehicleIdentificationType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
