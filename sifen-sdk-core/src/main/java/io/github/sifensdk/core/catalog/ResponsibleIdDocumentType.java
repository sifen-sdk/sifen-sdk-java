/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de documento de identidad del responsable de la generación del DE.
 *
 * <p>SIFEN catalog {@code tipo-documento-identidad-responsable}, used by fields {@code D141}. Each
 * constant carries the code that travels in the XML and the official description the SDK writes
 * next to it. Builders also accept the code as a {@code String}, for codes SIFEN adds before this
 * enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-documento-identidad-responsable.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum ResponsibleIdDocumentType implements CatalogCode {
  /** {@code 1}: Cédula paraguaya. */
  PARAGUAYAN_ID("1", "Cédula paraguaya"),

  /** {@code 2}: Pasaporte. */
  PASSPORT("2", "Pasaporte"),

  /** {@code 3}: Cédula extranjera. */
  FOREIGN_ID("3", "Cédula extranjera"),

  /** {@code 4}: Carnet de residencia. */
  RESIDENCE_PERMIT("4", "Carnet de residencia"),

  /** {@code 9}: Otro. */
  OTHER("9", "Otro");

  private final String code;
  private final String description;

  ResponsibleIdDocumentType(String code, String description) {
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
  public static Optional<ResponsibleIdDocumentType> fromCode(String code) {
    for (ResponsibleIdDocumentType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
