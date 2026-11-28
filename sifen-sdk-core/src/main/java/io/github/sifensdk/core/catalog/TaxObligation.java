/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Tipo de obligaciones (Tabla 12).
 *
 * <p>SIFEN catalog {@code obligacion-afectada}, used by fields {@code D031}. Each constant carries
 * the code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/obligacion-afectada.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum TaxObligation implements CatalogCode {
  /** {@code 113}: IMPUESTO A LA RENTA IRACIS - REGÍMENES ESPECIALES. */
  IRACIS_SPECIAL_REGIMES("113", "IMPUESTO A LA RENTA IRACIS - REGÍMENES ESPECIALES"),

  /** {@code 143}: TRIBUTO UNICO MAQUILA. */
  MAQUILA_SINGLE_TAX("143", "TRIBUTO UNICO MAQUILA"),

  /** {@code 211}: IMPUESTO AL VALOR AGREGADO - GRAVADAS Y EXONERADAS - EXPORTADORES. */
  VAT_EXPORTERS("211", "IMPUESTO AL VALOR AGREGADO - GRAVADAS Y EXONERADAS - EXPORTADORES"),

  /** {@code 311}: IMPUESTO SELECTIVO AL CONSUMO - GENERAL. */
  ISC_GENERAL("311", "IMPUESTO SELECTIVO AL CONSUMO - GENERAL"),

  /** {@code 321}: IMPUESTO SELECTIVO AL CONSUMO COMBUSTIBLES. */
  ISC_FUELS("321", "IMPUESTO SELECTIVO AL CONSUMO COMBUSTIBLES"),

  /** {@code 700}: IMPUESTO A LA RENTA EMPRESARIAL - RÉGIMEN GENERAL. */
  IRE_GENERAL("700", "IMPUESTO A LA RENTA EMPRESARIAL - RÉGIMEN GENERAL"),

  /** {@code 701}: IMPUESTO A LA RENTA EMPRESARIAL - SIMPLE. */
  IRE_SIMPLE("701", "IMPUESTO A LA RENTA EMPRESARIAL - SIMPLE"),

  /** {@code 703}: IMPUESTO DE ZONA FRANCA. */
  FREE_ZONE_TAX("703", "IMPUESTO DE ZONA FRANCA"),

  /** {@code 702}: IMPUESTO A LA RENTA EMPRESARIAL - RESIMPLE. */
  IRE_RESIMPLE("702", "IMPUESTO A LA RENTA EMPRESARIAL - RESIMPLE"),

  /** {@code 715}: IMPUESTO A LA RENTA PERSONAL - SERVICIOS PERSONALES. */
  IRP_PERSONAL_SERVICES("715", "IMPUESTO A LA RENTA PERSONAL - SERVICIOS PERSONALES"),

  /** {@code 716}: IMPUESTO A LA RENTA PERSONAL - RENTAS Y GANANCIAS DE CAPITAL. */
  IRP_CAPITAL_INCOME_AND_GAINS(
      "716", "IMPUESTO A LA RENTA PERSONAL - RENTAS Y GANANCIAS DE CAPITAL");

  private final String code;
  private final String description;

  TaxObligation(String code, String description) {
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
  public static Optional<TaxObligation> fromCode(String code) {
    for (TaxObligation value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
