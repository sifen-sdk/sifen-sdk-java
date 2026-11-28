/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Código de datos de relevancia de las mercaderías.
 *
 * <p>SIFEN catalog {@code relevancia-mercaderia}, used by fields {@code E715}. Each constant
 * carries the code that travels in the XML and the official description the SDK writes next to it.
 * Builders also accept the code as a {@code String}, for codes SIFEN adds before this enum knows
 * them.
 *
 * <p>Generated from {@code spec/catalogos/relevancia-mercaderia.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum GoodsRelevance implements CatalogCode {
  /** {@code 1}: Tolerancia de quiebra. */
  BREAKAGE_TOLERANCE("1", "Tolerancia de quiebra"),

  /** {@code 2}: Tolerancia de merma. */
  SHRINKAGE_TOLERANCE("2", "Tolerancia de merma");

  private final String code;
  private final String description;

  GoodsRelevance(String code, String description) {
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
  public static Optional<GoodsRelevance> fromCode(String code) {
    for (GoodsRelevance value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
