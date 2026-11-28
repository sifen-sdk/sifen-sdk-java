/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de transacción.
 *
 * <p>SIFEN catalog {@code tipo-transaccion}, used by fields {@code D011}. Each constant carries the
 * code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-transaccion.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum TransactionType implements CatalogCode {
  /** {@code 1}: Venta de mercadería. */
  GOODS_SALE("1", "Venta de mercadería"),

  /** {@code 2}: Prestación de servicios. */
  SERVICES("2", "Prestación de servicios"),

  /** {@code 3}: Mixto (Venta de mercadería y servicios). */
  GOODS_AND_SERVICES("3", "Mixto (Venta de mercadería y servicios)"),

  /** {@code 4}: Venta de activo fijo. */
  FIXED_ASSET_SALE("4", "Venta de activo fijo"),

  /** {@code 5}: Venta de divisas. */
  CURRENCY_SALE("5", "Venta de divisas"),

  /** {@code 6}: Compra de divisas. */
  CURRENCY_PURCHASE("6", "Compra de divisas"),

  /** {@code 7}: Promoción o entrega de muestras. */
  PROMOTION_OR_SAMPLES("7", "Promoción o entrega de muestras"),

  /** {@code 8}: Donación. */
  DONATION("8", "Donación"),

  /** {@code 9}: Anticipo. */
  ADVANCE("9", "Anticipo"),

  /** {@code 10}: Compra de productos. */
  GOODS_PURCHASE("10", "Compra de productos"),

  /** {@code 11}: Compra de servicios. */
  SERVICES_PURCHASE("11", "Compra de servicios"),

  /** {@code 12}: Venta de crédito fiscal. */
  TAX_CREDIT_SALE("12", "Venta de crédito fiscal"),

  /** {@code 13}: Muestras médicas (Art. 3 RG 24/2014). */
  MEDICAL_SAMPLES("13", "Muestras médicas (Art. 3 RG 24/2014)");

  private final String code;
  private final String description;

  TransactionType(String code, String description) {
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
  public static Optional<TransactionType> fromCode(String code) {
    for (TransactionType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
