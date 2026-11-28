/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de documento de identidad del receptor (evento de nominación).
 *
 * <p>SIFEN catalog {@code tipo-documento-identidad-nominacion}, used by fields {@code GENFE010}.
 * Each constant carries the code that travels in the XML and the official description the SDK
 * writes next to it. Builders also accept the code as a {@code String}, for codes SIFEN adds before
 * this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-documento-identidad-nominacion.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum NominationIdDocumentType implements CatalogCode {
  /** {@code 1}: Cédula paraguaya. */
  PARAGUAYAN_ID("1", "Cédula paraguaya"),

  /** {@code 2}: Pasaporte. */
  PASSPORT("2", "Pasaporte"),

  /** {@code 3}: Cédula extranjera. */
  FOREIGN_ID("3", "Cédula extranjera"),

  /** {@code 4}: Carnet de residencia. */
  RESIDENCE_PERMIT("4", "Carnet de residencia"),

  /**
   * {@code 5}: Tarjeta Diplomática de exoneración fiscal.
   *
   * <p>No longer valid since 2026-03-09.
   *
   * @deprecated SIFEN no longer accepts this code
   */
  @Deprecated
  DIPLOMATIC_CARD_FORMER_CODE("5", "Tarjeta Diplomática de exoneración fiscal"),

  /** {@code 6}: Tarjeta Diplomática de exoneración fiscal. */
  DIPLOMATIC_CARD("6", "Tarjeta Diplomática de exoneración fiscal"),

  /** {@code 9}: Otro. */
  OTHER("9", "Otro");

  private final String code;
  private final String description;

  NominationIdDocumentType(String code, String description) {
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
  public static Optional<NominationIdDocumentType> fromCode(String code) {
    for (NominationIdDocumentType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
