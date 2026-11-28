/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de documento electrónico.
 *
 * <p>SIFEN catalog {@code tipo-documento}, used by fields {@code C002}, {@code GEI007}. Each
 * constant carries the code that travels in the XML and the official description the SDK writes
 * next to it. Builders also accept the code as a {@code String}, for codes SIFEN adds before this
 * enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-documento.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum DocumentType implements CatalogCode {
  /** {@code 1}: Factura electrónica. */
  INVOICE("1", "Factura electrónica"),

  /**
   * {@code 2}: Factura electrónica de exportación.
   *
   * <p>Defined by SIFEN but not in force yet.
   */
  EXPORT_INVOICE("2", "Factura electrónica de exportación"),

  /**
   * {@code 3}: Factura electrónica de importación.
   *
   * <p>Defined by SIFEN but not in force yet.
   */
  IMPORT_INVOICE("3", "Factura electrónica de importación"),

  /** {@code 4}: Autofactura electrónica. */
  SELF_INVOICE("4", "Autofactura electrónica"),

  /** {@code 5}: Nota de crédito electrónica. */
  CREDIT_NOTE("5", "Nota de crédito electrónica"),

  /** {@code 6}: Nota de débito electrónica. */
  DEBIT_NOTE("6", "Nota de débito electrónica"),

  /** {@code 7}: Nota de remisión electrónica. */
  DELIVERY_NOTE("7", "Nota de remisión electrónica"),

  /**
   * {@code 8}: Comprobante de retención electrónico.
   *
   * <p>Defined by SIFEN but not in force yet.
   */
  WITHHOLDING_RECEIPT("8", "Comprobante de retención electrónico"),

  /** {@code 9}: Boleta de venta electrónica. */
  SALES_RECEIPT("9", "Boleta de venta electrónica"),

  /** {@code 10}: Boleta resimple electrónica. */
  RESIMPLE_RECEIPT("10", "Boleta resimple electrónica");

  private final String code;
  private final String description;

  DocumentType(String code, String description) {
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
  public static Optional<DocumentType> fromCode(String code) {
    for (DocumentType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
