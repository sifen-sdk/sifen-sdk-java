/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Modalidad del transporte.
 *
 * <p>SIFEN catalog {@code modalidad-transporte}, used by fields {@code E903}, {@code GET024}. Each
 * constant carries the code that travels in the XML and the official description the SDK writes
 * next to it. Builders also accept the code as a {@code String}, for codes SIFEN adds before this
 * enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/modalidad-transporte.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum TransportMode implements CatalogCode {
  /** {@code 1}: Terrestre. */
  LAND("1", "Terrestre"),

  /** {@code 2}: Fluvial. */
  RIVER("2", "Fluvial"),

  /** {@code 3}: Aéreo. */
  AIR("3", "Aéreo"),

  /** {@code 4}: Multimodal. */
  MULTIMODAL("4", "Multimodal");

  private final String code;
  private final String description;

  TransportMode(String code, String description) {
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
  public static Optional<TransportMode> fromCode(String code) {
    for (TransportMode value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
