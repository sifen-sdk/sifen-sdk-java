/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Responsable de la emisión de la nota de remisión.
 *
 * <p>SIFEN catalog {@code responsable-emision-nr}, used by fields {@code E503}. Each constant
 * carries the code that travels in the XML and the official description the SDK writes next to it.
 * Builders also accept the code as a {@code String}, for codes SIFEN adds before this enum knows
 * them.
 *
 * <p>Generated from {@code spec/catalogos/responsable-emision-nr.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum DeliveryNoteIssuer implements CatalogCode {
  /** {@code 1}: Emisor de la factura. */
  INVOICE_ISSUER("1", "Emisor de la factura"),

  /** {@code 2}: Poseedor de la factura y bienes. */
  INVOICE_AND_GOODS_HOLDER("2", "Poseedor de la factura y bienes"),

  /** {@code 3}: Empresa transportista. */
  CARRIER_COMPANY("3", "Empresa transportista"),

  /** {@code 4}: Despachante de Aduanas. */
  CUSTOMS_BROKER("4", "Despachante de Aduanas"),

  /** {@code 5}: Agente de transporte o intermediario. */
  TRANSPORT_AGENT("5", "Agente de transporte o intermediario");

  private final String code;
  private final String description;

  DeliveryNoteIssuer(String code, String description) {
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
  public static Optional<DeliveryNoteIssuer> fromCode(String code) {
    for (DeliveryNoteIssuer value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
