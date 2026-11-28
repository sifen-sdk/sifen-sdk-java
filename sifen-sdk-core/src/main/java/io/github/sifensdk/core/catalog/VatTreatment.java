/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Forma de afectación tributaria del IVA (Tabla 6).
 *
 * <p>SIFEN catalog {@code afectacion-iva}, used by fields {@code E731}. Each constant carries the
 * code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/afectacion-iva.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum VatTreatment implements CatalogCode {
  /** {@code 1}: Gravado IVA. */
  TAXED("1", "Gravado IVA"),

  /** {@code 2}: Exonerado (Art. 100 - Ley 6380/2019). */
  EXONERATED("2", "Exonerado (Art. 100 - Ley 6380/2019)"),

  /** {@code 3}: Exento. */
  EXEMPT("3", "Exento"),

  /** {@code 4}: Gravado parcial (Grav- Exento). */
  PARTIALLY_TAXED("4", "Gravado parcial (Grav- Exento)");

  private final String code;
  private final String description;

  VatTreatment(String code, String description) {
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
  public static Optional<VatTreatment> fromCode(String code) {
    for (VatTreatment value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
