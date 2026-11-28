/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de receptor (eventos de notificación y desconocimiento).
 *
 * <p>SIFEN catalog {@code tipo-receptor-evento}, used by fields {@code GEN005}, {@code GED005}.
 * Each constant carries the code that travels in the XML and the official description the SDK
 * writes next to it. Builders also accept the code as a {@code String}, for codes SIFEN adds before
 * this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-receptor-evento.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum EventReceiverType implements CatalogCode {
  /** {@code 1}: Contribuyente. */
  TAXPAYER("1", "Contribuyente"),

  /** {@code 2}: No Contribuyente. */
  NON_TAXPAYER("2", "No Contribuyente");

  private final String code;
  private final String description;

  EventReceiverType(String code, String description) {
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
  public static Optional<EventReceiverType> fromCode(String code) {
    for (EventReceiverType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
