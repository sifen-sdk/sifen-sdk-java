/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Motivo del evento de actualización de datos del transporte.
 *
 * <p>SIFEN catalog {@code motivo-evento-transporte}, used by fields {@code GET003}. Each constant
 * carries the code that travels in the XML and the official description the SDK writes next to it.
 * Builders also accept the code as a {@code String}, for codes SIFEN adds before this enum knows
 * them.
 *
 * <p>Generated from {@code spec/catalogos/motivo-evento-transporte.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum TransportUpdateReason implements CatalogCode {
  /** {@code 1}: Cambio del local de la entrega. */
  DELIVERY_PLACE_CHANGE("1", "Cambio del local de la entrega"),

  /** {@code 2}: Cambio del chofer. */
  DRIVER_CHANGE("2", "Cambio del chofer"),

  /** {@code 3}: Cambio del transportista. */
  CARRIER_CHANGE("3", "Cambio del transportista"),

  /** {@code 4}: Cambio de vehículo. */
  VEHICLE_CHANGE("4", "Cambio de vehículo");

  private final String code;
  private final String description;

  TransportUpdateReason(String code, String description) {
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
  public static Optional<TransportUpdateReason> fromCode(String code) {
    for (TransportUpdateReason value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
