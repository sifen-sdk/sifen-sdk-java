/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import java.util.Optional;

/**
 * Unidades de medida (Tabla 5).
 *
 * <p>SIFEN catalog {@code unidad-medida}, used by fields {@code E709}, {@code G051}, {@code G054}.
 * Each constant carries the code that travels in the XML and the official description the SDK
 * writes next to it. Builders also accept the code as a {@code String}, for codes SIFEN adds before
 * this enum knows them.
 *
 * <p>Generated from {@code spec/catalogos/unidad-medida.json} by {@code
 * scripts/generar-catalogos.py}; do not edit by hand.
 */
public enum UnitOfMeasure implements CatalogCode {
  /** {@code 87}: m (Metros). */
  METER("87", "m"),

  /** {@code 2366}: CPM (Costo por Mil). */
  COST_PER_THOUSAND("2366", "CPM"),

  /** {@code 2329}: UI (Unidad Internacional). */
  INTERNATIONAL_UNIT("2329", "UI"),

  /** {@code 110}: M3 (Metros cúbicos). */
  CUBIC_METER("110", "M3"),

  /** {@code 77}: UNI (Unidad). */
  UNIT("77", "UNI"),

  /** {@code 86}: g (Gramos). */
  GRAM("86", "g"),

  /** {@code 89}: LT (Litros). */
  LITER("89", "LT"),

  /** {@code 90}: MG (Miligramos). */
  MILLIGRAM("90", "MG"),

  /** {@code 91}: CM (Centimetros). */
  CENTIMETER("91", "CM"),

  /** {@code 92}: CM2 (Centimetros cuadrados). */
  SQUARE_CENTIMETER("92", "CM2"),

  /** {@code 93}: CM3 (Centimetros cubicos). */
  CUBIC_CENTIMETER("93", "CM3"),

  /** {@code 94}: PUL (Pulgadas). */
  INCH("94", "PUL"),

  /** {@code 96}: MM2 (Milímetros cuadrados). */
  SQUARE_MILLIMETER("96", "MM2"),

  /** {@code 79}: kg/m² (Kilogramos s/ metro cuadrado). */
  KILOGRAM_PER_SQUARE_METER("79", "kg/m2"),

  /** {@code 97}: AA (Año). */
  YEAR("97", "AA"),

  /** {@code 98}: ME (Mes). */
  MONTH("98", "ME"),

  /** {@code 99}: TN (Tonelada). */
  TON("99", "TN"),

  /** {@code 100}: Hs (Hora). */
  HOUR("100", "Hs"),

  /** {@code 101}: Mi (Minuto). */
  MINUTE("101", "Mi"),

  /** {@code 104}: DET (Determinación). */
  DETERMINATION("104", "DET"),

  /** {@code 103}: Ya (Yardas). */
  YARD("103", "Ya"),

  /** {@code 108}: MT (Metros). */
  METER_MT("108", "MT"),

  /** {@code 109}: M2 (Metros cuadrados). */
  SQUARE_METER("109", "M2"),

  /** {@code 95}: MM (Milímetros). */
  MILLIMETER("95", "MM"),

  /** {@code 666}: Se (Segundo). */
  SECOND("666", "Se"),

  /** {@code 102}: Di (Día). */
  DAY("102", "Di"),

  /** {@code 83}: kg (Kilogramos). */
  KILOGRAM("83", "kg"),

  /** {@code 88}: ML (Mililitros). */
  MILLILITER("88", "ML"),

  /** {@code 625}: Km (Kilómetros). */
  KILOMETER("625", "Km"),

  /** {@code 660}: ml (Metro lineal). */
  LINEAR_METER("660", "ml"),

  /** {@code 885}: GL (Unidad Medida Global). */
  GLOBAL_UNIT("885", "GL"),

  /** {@code 891}: pm (Por Milaje). */
  PER_MILLE("891", "pm"),

  /** {@code 869}: ha (Hectáreas). */
  HECTARE("869", "ha"),

  /** {@code 569}: ración (Ración). */
  RATION("569", "ración"),

  /** {@code 111}: 4A (Bovinas). */
  BOVINE_HEAD("111", "4A"),

  /** {@code 112}: Ci (Curie). */
  CURIE("112", "Ci"),

  /** {@code 113}: DOC (Docena). */
  DOZEN("113", "DOC"),

  /** {@code 114}: GLL (Galones (US) (3,7843 LT)). */
  US_GALLON("114", "GLL"),

  /** {@code 115}: GRO (Gruesas). */
  GROSS("115", "GRO"),

  /** {@code 116}: E4 (Kilogramo Bruto). */
  GROSS_KILOGRAM("116", "E4"),

  /** {@code 117}: KT (Kits). */
  KIT("117", "KT"),

  /** {@code 118}: M5 (Microcurie). */
  MICROCURIE("118", "M5"),

  /** {@code 119}: MCU (Milicurie). */
  MILLICURIE("119", "MCU"),

  /** {@code 120}: MIL (Millar). */
  THOUSAND("120", "MIL"),

  /** {@code 121}: PAR (Par). */
  PAIR("121", "PAR"),

  /** {@code 122}: FOT (Pies). */
  FOOT("122", "FOT"),

  /** {@code 123}: FTK (Pies Cuadradas). */
  SQUARE_FOOT("123", "FTK"),

  /** {@code 124}: PCE (Piezas). */
  PIECE("124", "PCE"),

  /** {@code 125}: KLT (Quilate). */
  CARAT("125", "KLT"),

  /** {@code 126}: RM (Resmas). */
  REAM("126", "RM"),

  /** {@code 127}: RO (Rollos). */
  ROLL("127", "RO"),

  /** {@code 128}: kWh (1000 Kilowatt Hora). */
  THOUSAND_KILOWATT_HOURS("128", "kWh"),

  /** {@code 129}: U(JGO) (Mazos). */
  DECK("129", "U(JGO)"),

  /** {@code 130}: DR (Tambores). */
  DRUM("130", "DR"),

  /** {@code 131}: BX (Caja). */
  BOX("131", "BX"),

  /** {@code 132}: SET (Juego). */
  SET("132", "SET"),

  /** {@code 133}: PK (Paquete). */
  PACKAGE("133", "PK"),

  /** {@code 134}: BG (Bolsa). */
  BAG("134", "BG"),

  /** {@code 135}: DPC (Docena Par). */
  DOZEN_PAIRS("135", "DPC"),

  /** {@code 136}: JR (Pote). */
  JAR("136", "JR"),

  /** {@code 137}: BL (Fardos). */
  BALE("137", "BL"),

  /** {@code 138}: AB (Bulto). */
  BUNDLE("138", "AB"),

  /** {@code 139}: BK (Cesta). */
  BASKET("139", "BK"),

  /** {@code 140}: BW (Peso Base). */
  BASIS_WEIGHT("140", "BW");

  private final String code;
  private final String description;

  UnitOfMeasure(String code, String description) {
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
  public static Optional<UnitOfMeasure> fromCode(String code) {
    for (UnitOfMeasure value : values()) {
      if (value.code.equals(code)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
