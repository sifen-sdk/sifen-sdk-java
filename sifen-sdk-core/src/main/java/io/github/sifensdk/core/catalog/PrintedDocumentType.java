/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de documento impreso asociado.
 *
 * <p>SIFEN catalog {@code tipo-documento-impreso}, used by fields {@code H009}. Each constant
 * carries the code that travels in the XML and the official description the SDK writes next to it.
 * Builders also accept the code as a {@code String}, for codes SIFEN adds before this enum knows
 * them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-documento-impreso.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum PrintedDocumentType implements CatalogCode {
  /** {@code 1}: Factura. */
  INVOICE("1", "Factura"),

  /** {@code 2}: Nota de crédito. */
  CREDIT_NOTE("2", "Nota de crédito"),

  /** {@code 3}: Nota de débito. */
  DEBIT_NOTE("3", "Nota de débito"),

  /** {@code 4}: Nota de remisión. */
  DELIVERY_NOTE("4", "Nota de remisión"),

  /** {@code 5}: Comprobante de retención. */
  WITHHOLDING_RECEIPT("5", "Comprobante de retención");

  private final String code;
  private final String description;

  PrintedDocumentType(String code, String description) {
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
  public static Optional<PrintedDocumentType> fromCode(String code) {
    for (PrintedDocumentType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
