/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Motivo de emisión de la nota de crédito/débito.
 *
 * <p>SIFEN catalog {@code motivo-emision-nc}, used by fields {@code E401}. Each constant carries
 * the code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/motivo-emision-nc.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum CreditDebitNoteReason implements CatalogCode {
  /** {@code 1}: Devolución y Ajuste de precios. */
  RETURN_AND_PRICE_ADJUSTMENT("1", "Devolución y Ajuste de precios"),

  /** {@code 2}: Devolución. */
  RETURN("2", "Devolución"),

  /** {@code 3}: Descuento. */
  DISCOUNT("3", "Descuento"),

  /** {@code 4}: Bonificación. */
  BONUS("4", "Bonificación"),

  /** {@code 5}: Crédito incobrable. */
  BAD_DEBT("5", "Crédito incobrable"),

  /** {@code 6}: Recupero de costo. */
  COST_RECOVERY("6", "Recupero de costo"),

  /** {@code 7}: Recupero de gasto. */
  EXPENSE_RECOVERY("7", "Recupero de gasto"),

  /** {@code 8}: Ajuste de precio. */
  PRICE_ADJUSTMENT("8", "Ajuste de precio");

  private final String code;
  private final String description;

  CreditDebitNoteReason(String code, String description) {
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
  public static Optional<CreditDebitNoteReason> fromCode(String code) {
    for (CreditDebitNoteReason value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
