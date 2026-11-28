/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de operación de venta de vehículos.
 *
 * <p>SIFEN catalog {@code tipo-operacion-vehiculo}, used by fields {@code E771}. Each constant
 * carries the code that travels in the XML and the official description the SDK writes next to it.
 * Builders also accept the code as a {@code String}, for codes SIFEN adds before this enum knows
 * them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-operacion-vehiculo.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum VehicleSaleType implements CatalogCode {
  /** {@code 1}: Venta a representante. */
  TO_DEALER("1", "Venta a representante"),

  /** {@code 2}: Venta al consumidor final. */
  TO_FINAL_CONSUMER("2", "Venta al Consumidor final"),

  /** {@code 3}: Venta a gobierno. */
  TO_GOVERNMENT("3", "Venta a gobierno"),

  /** {@code 4}: Venta a flota de vehículos. */
  TO_FLEET("4", "Venta a flota de vehículos");

  private final String code;
  private final String description;

  VehicleSaleType(String code, String description) {
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
  public static Optional<VehicleSaleType> fromCode(String code) {
    for (VehicleSaleType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
