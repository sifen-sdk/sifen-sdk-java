/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de documento asociado.
 *
 * <p>SIFEN catalog {@code tipo-documento-asociado}, used by fields {@code H002}. Each constant
 * carries the code that travels in the XML and the official description the SDK writes next to it.
 * Builders also accept the code as a {@code String}, for codes SIFEN adds before this enum knows
 * them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-documento-asociado.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum AssociatedDocumentType implements CatalogCode {
  /** {@code 1}: Electrónico. */
  ELECTRONIC("1", "Electrónico"),

  /** {@code 2}: Impreso. */
  PRINTED("2", "Impreso"),

  /** {@code 3}: Constancia Electrónica. */
  ELECTRONIC_CERTIFICATE("3", "Constancia Electrónica");

  private final String code;
  private final String description;

  AssociatedDocumentType(String code, String description) {
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
  public static Optional<AssociatedDocumentType> fromCode(String code) {
    for (AssociatedDocumentType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
