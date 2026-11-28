/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Naturaleza del receptor.
 *
 * <p>SIFEN catalog {@code naturaleza-receptor}, used by fields {@code D201}, {@code GENFE004}. Each
 * constant carries the code that travels in the XML and the official description the SDK writes
 * next to it. Builders also accept the code as a {@code String}, for codes SIFEN adds before this
 * enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/naturaleza-receptor.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum ReceiverNature implements CatalogCode {
  /** {@code 1}: contribuyente. */
  TAXPAYER("1", "contribuyente"),

  /** {@code 2}: no contribuyente. */
  NON_TAXPAYER("2", "no contribuyente");

  private final String code;
  private final String description;

  ReceiverNature(String code, String description) {
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
  public static Optional<ReceiverNature> fromCode(String code) {
    for (ReceiverNature value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
