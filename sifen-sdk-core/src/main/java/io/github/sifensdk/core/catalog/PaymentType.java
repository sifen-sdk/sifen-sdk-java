/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de pago.
 *
 * <p>SIFEN catalog {@code tipo-pago}, used by fields {@code E606}. Each constant carries the code
 * that travels in the XML and the official description the SDK writes next to it. Builders also
 * accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/tipo-pago.json} by {@code scripts/generar-catalogos.py};
 * do not edit by hand.
 */
public enum PaymentType implements CatalogCode {
  /** {@code 1}: Efectivo. */
  CASH("1", "Efectivo"),

  /** {@code 2}: Cheque. */
  CHECK("2", "Cheque"),

  /** {@code 3}: Tarjeta de crédito. */
  CREDIT_CARD("3", "Tarjeta de crédito"),

  /** {@code 4}: Tarjeta de débito. */
  DEBIT_CARD("4", "Tarjeta de débito"),

  /** {@code 5}: Transferencia. */
  TRANSFER("5", "Transferencia"),

  /** {@code 6}: Giro. */
  MONEY_ORDER("6", "Giro"),

  /** {@code 7}: Billetera electrónica. */
  E_WALLET("7", "Billetera electrónica"),

  /** {@code 8}: Tarjeta empresarial. */
  CORPORATE_CARD("8", "Tarjeta empresarial"),

  /** {@code 9}: Vale. */
  VOUCHER("9", "Vale"),

  /** {@code 10}: Retención. */
  WITHHOLDING("10", "Retención"),

  /** {@code 11}: Pago por anticipo. */
  ADVANCE_PAYMENT("11", "Pago por anticipo"),

  /** {@code 12}: Valor fiscal. */
  FISCAL_VALUE("12", "Valor fiscal"),

  /** {@code 13}: Valor comercial. */
  COMMERCIAL_VALUE("13", "Valor comercial"),

  /** {@code 14}: Compensación. */
  OFFSET("14", "Compensación"),

  /** {@code 15}: Permuta. */
  BARTER("15", "Permuta"),

  /** {@code 16}: Pago bancario. */
  BANK_PAYMENT("16", "Pago bancario"),

  /** {@code 17}: Pago Móvil. */
  MOBILE_PAYMENT("17", "Pago Móvil"),

  /** {@code 18}: Donación. */
  DONATION("18", "Donación"),

  /** {@code 19}: Promoción. */
  PROMOTION("19", "Promoción"),

  /** {@code 20}: Consumo Interno. */
  INTERNAL_CONSUMPTION("20", "Consumo Interno"),

  /** {@code 21}: Pago Electrónico. */
  ELECTRONIC_PAYMENT("21", "Pago Electrónico"),

  /** {@code 99}: Otro. */
  OTHER("99", "Otro");

  private final String code;
  private final String description;

  PaymentType(String code, String description) {
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
  public static Optional<PaymentType> fromCode(String code) {
    for (PaymentType value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
