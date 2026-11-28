/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Departamentos.
 *
 * <p>SIFEN catalog {@code departamento}, used by fields {@code D111}, {@code D219}, {@code E310},
 * {@code E317}, {@code E925}, {@code E945}, {@code GET004}, {@code GENFE017}. Each constant carries
 * the code that travels in the XML and the official description the SDK writes next to it. Builders
 * also accept the code as a {@code String}, for codes SIFEN adds before this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/departamento.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum Department implements CatalogCode {
  /** {@code 1}: CAPITAL. */
  CAPITAL("1", "CAPITAL"),

  /** {@code 2}: CONCEPCION. */
  CONCEPCION("2", "CONCEPCION"),

  /** {@code 3}: SAN PEDRO. */
  SAN_PEDRO("3", "SAN PEDRO"),

  /** {@code 4}: CORDILLERA. */
  CORDILLERA("4", "CORDILLERA"),

  /** {@code 5}: GUAIRA. */
  GUAIRA("5", "GUAIRA"),

  /** {@code 6}: CAAGUAZU. */
  CAAGUAZU("6", "CAAGUAZU"),

  /** {@code 7}: CAAZAPA. */
  CAAZAPA("7", "CAAZAPA"),

  /** {@code 8}: ITAPUA. */
  ITAPUA("8", "ITAPUA"),

  /** {@code 9}: MISIONES. */
  MISIONES("9", "MISIONES"),

  /** {@code 10}: PARAGUARI. */
  PARAGUARI("10", "PARAGUARI"),

  /** {@code 11}: ALTO PARANA. */
  ALTO_PARANA("11", "ALTO PARANA"),

  /** {@code 12}: CENTRAL. */
  CENTRAL("12", "CENTRAL"),

  /** {@code 13}: NEEMBUCU. */
  NEEMBUCU("13", "NEEMBUCU"),

  /** {@code 14}: AMAMBAY. */
  AMAMBAY("14", "AMAMBAY"),

  /** {@code 15}: PTE. HAYES. */
  PRESIDENTE_HAYES("15", "PTE. HAYES"),

  /** {@code 16}: BOQUERON. */
  BOQUERON("16", "BOQUERON"),

  /** {@code 17}: ALTO PARAGUAY. */
  ALTO_PARAGUAY("17", "ALTO PARAGUAY"),

  /** {@code 18}: CANINDEYU. */
  CANINDEYU("18", "CANINDEYU"),

  /** {@code 19}: CHACO. */
  CHACO("19", "CHACO"),

  /** {@code 20}: NUEVA ASUNCION. */
  NUEVA_ASUNCION("20", "NUEVA ASUNCION");

  private final String code;
  private final String description;

  Department(String code, String description) {
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
  public static Optional<Department> fromCode(String code) {
    for (Department value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
