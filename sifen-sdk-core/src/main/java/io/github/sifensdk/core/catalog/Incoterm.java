/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Condiciones de negociación – Incoterms (Tabla 10).
 *
 * <p>SIFEN catalog {@code condicion-negociacion}, used by fields {@code E906}. Each constant
 * carries the code that travels in the XML and the official description the SDK writes next to it.
 * Builders also accept the code as a {@code String}, for codes SIFEN adds before this enum knows
 * them.
 *
 * <p>Generated from {@code spec/catalogos/condicion-negociacion.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum Incoterm implements CatalogCode {
  /** {@code CFR}: Costo y flete. */
  CFR("CFR", "Costo y flete"),

  /** {@code CIF}: Costo, seguro y flete. */
  CIF("CIF", "Costo, seguro y flete"),

  /** {@code CIP}: Transporte y seguro pagados hasta. */
  CIP("CIP", "Transporte y seguros pagados hasta"),

  /** {@code CPT}: Transporte pagado hasta. */
  CPT("CPT", "Transporte pagado hasta"),

  /** {@code DAP}: Entregada en lugar convenido. */
  DAP("DAP", "Entregada en el lugar convenido"),

  /** {@code DAT}: Entregada en terminal. */
  DAT("DAT", "Entregada en terminal"),

  /** {@code DDP}: Entregada derechos pagados. */
  DDP("DDP", "Entregada derechos pagados"),

  /** {@code EXW}: En fabrica. */
  EXW("EXW", "En fabrica"),

  /** {@code FAS}: Franco al costado del buque. */
  FAS("FAS", "Franco al costado del buque"),

  /** {@code FCA}: Franco transportista. */
  FCA("FCA", "Franco transportista"),

  /** {@code FOB}: Franco a bordo. */
  FOB("FOB", "Franco a bordo");

  private final String code;
  private final String description;

  Incoterm(String code, String description) {
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
  public static Optional<Incoterm> fromCode(String code) {
    for (Incoterm value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
