/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Motivo de emisión de la nota de remisión.
 *
 * <p>SIFEN catalog {@code motivo-emision-nr}, used by fields {@code E501}. Each constant carries
 * the code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/motivo-emision-nr.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum DeliveryNoteReason implements CatalogCode {
  /** {@code 1}: Traslado por ventas. */
  SALE("1", "Traslado por ventas"),

  /** {@code 2}: Traslado por consignación. */
  CONSIGNMENT("2", "Traslado por consignación"),

  /** {@code 3}: Exportación. */
  EXPORT("3", "Exportación"),

  /** {@code 4}: Traslado por compra. */
  PURCHASE("4", "Traslado por compra"),

  /** {@code 5}: Importación. */
  IMPORT("5", "Importación"),

  /** {@code 6}: Traslado por devolución. */
  RETURN("6", "Traslado por devolución"),

  /** {@code 7}: Traslado entre locales de la empresa. */
  BETWEEN_OWN_PREMISES("7", "Traslado entre locales de la empresa"),

  /** {@code 8}: Traslado de bienes por transformación. */
  TRANSFORMATION("8", "Traslado de bienes por transformación"),

  /** {@code 9}: Traslado de bienes por reparación. */
  REPAIR("9", "Traslado de bienes para reparación"),

  /** {@code 10}: Traslado por emisor móvil. */
  MOBILE_ISSUER("10", "Traslado por emisor móvil"),

  /** {@code 11}: Exhibición o Demostración. */
  EXHIBITION_OR_DEMONSTRATION("11", "Exhibición o Demostración"),

  /** {@code 12}: Participación en ferias. */
  TRADE_FAIR("12", "Participación en ferias"),

  /** {@code 13}: Traslado de encomienda. */
  PARCEL("13", "Traslado de encomienda"),

  /** {@code 14}: Decomiso. */
  SEIZURE("14", "Decomiso"),

  /** {@code 99}: Otro. */
  OTHER("99", "Otro");

  private final String code;
  private final String description;

  DeliveryNoteReason(String code, String description) {
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
  public static Optional<DeliveryNoteReason> fromCode(String code) {
    for (DeliveryNoteReason value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
