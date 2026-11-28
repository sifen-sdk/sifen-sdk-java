/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Responsable del costo del flete.
 *
 * <p>SIFEN catalog {@code responsable-flete}, used by fields {@code E905}. Each constant carries
 * the code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/responsable-flete.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum FreightPayer implements CatalogCode {
  /** {@code 1}: Emisor de la Factura Electrónica. */
  INVOICE_ISSUER("1", "Emisor de la Factura Electrónica"),

  /** {@code 2}: Receptor de la Factura Electrónica. */
  INVOICE_RECEIVER("2", "Receptor de la Factura Electrónica"),

  /** {@code 3}: Tercero. */
  THIRD_PARTY("3", "Tercero"),

  /** {@code 4}: Agente intermediario del transporte (cuando intervenga). */
  TRANSPORT_AGENT("4", "Agente intermediario del transporte (cuando intervenga)"),

  /** {@code 5}: Transporte propio. */
  OWN_TRANSPORT("5", "Transporte propio");

  private final String code;
  private final String description;

  FreightPayer(String code, String description) {
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
  public static Optional<FreightPayer> fromCode(String code) {
    for (FreightPayer value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
