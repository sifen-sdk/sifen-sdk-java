/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.cdc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.core.internal.json.Json;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class CdcTest {

  private static final String EXAMPLE = "01444444017001001001452822017012515873260988";

  @SuppressWarnings("unchecked")
  private static Map<String, Object> conformance(String path) throws IOException {
    try (InputStream in = Objects.requireNonNull(CdcTest.class.getResourceAsStream(path), path)) {
      return (Map<String, Object>)
          Json.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void composesTheOfficialExampleOfTheManual() throws IOException {
    Map<String, Object> example = conformance("/conformidad/cdc/mt150-ejemplo-10-1.json");
    Map<String, String> in = (Map<String, String>) (Map<?, ?>) example.get("entrada");
    Map<String, String> expected = (Map<String, String>) (Map<?, ?>) example.get("esperado");

    Cdc cdc =
        Cdc.compose(
            in.get("tipoDocumento"),
            in.get("rucEmisor"),
            in.get("digitoVerificadorRuc"),
            in.get("establecimiento"),
            in.get("puntoExpedicion"),
            in.get("numeroDocumento"),
            in.get("tipoContribuyente"),
            LocalDate.parse(in.get("fechaEmision"), DateTimeFormatter.BASIC_ISO_DATE),
            in.get("tipoEmision"),
            in.get("codigoSeguridad"));

    assertThat(cdc.value()).isEqualTo(expected.get("cdc"));
    assertThat(cdc.checkDigit()).isEqualTo(expected.get("digitoVerificador"));
    assertThat(cdc.kudeFormat()).isEqualTo(expected.get("representacionKude"));
    assertThat(Cdc.parse(expected.get("cdc"))).isEqualTo(cdc);
  }

  @Test
  void exposesTheComponents() {
    Cdc cdc = Cdc.parse(EXAMPLE);
    assertThat(cdc.documentType()).isEqualTo("01");
    assertThat(cdc.issuerRuc()).isEqualTo("44444401");
    assertThat(cdc.issuerRucCheckDigit()).isEqualTo("7");
    assertThat(cdc.establishment()).isEqualTo("001");
    assertThat(cdc.expeditionPoint()).isEqualTo("001");
    assertThat(cdc.documentNumber()).isEqualTo("0014528");
    assertThat(cdc.taxpayerType()).isEqualTo("2");
    assertThat(cdc.issueDate()).isEqualTo("20170125");
    assertThat(cdc.emissionType()).isEqualTo("1");
    assertThat(cdc.securityCode()).isEqualTo("587326098");
    assertThat(cdc.toString()).isEqualTo(EXAMPLE);
  }

  @Test
  void padsDocumentTypeRucAndNumberWithZeros() {
    Cdc cdc =
        Cdc.compose(
            "1",
            "444401",
            "7",
            "001",
            "002",
            "15",
            "1",
            LocalDate.of(2026, 3, 9),
            "1",
            "000000001");
    assertThat(cdc.value()).startsWith("0100444401700100200000151202603091000000001");
    assertThat(Cdc.parse(cdc.value())).isEqualTo(cdc);
  }

  @Test
  void rejectsAWrongCheckDigitLengthOrCharacters() {
    assertThatThrownBy(() -> Cdc.parse(EXAMPLE.substring(0, 43) + "7"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("check digit")
        .hasMessageNotContaining(EXAMPLE.substring(0, 43));
    assertThatThrownBy(() -> Cdc.parse(EXAMPLE.substring(1)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("44");
    assertThatThrownBy(() -> Cdc.parse("X" + EXAMPLE.substring(1)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("tCDC");
  }

  @Test
  void rejectsInvalidComponents() {
    LocalDate date = LocalDate.of(2026, 1, 1);
    assertThatThrownBy(() ->
            Cdc.compose("01", "4444440E", "7", "001", "001", "1", "1", date, "1", "123456789"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> Cdc.compose("01", "44444401", "7", "01", "001", "1", "1", date, "1", "123456789"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("C005");
    assertThatThrownBy(
            () -> Cdc.compose("01", "44444401", "7", "001", "001", "1", "1", date, "1", "12345678"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("B004");
  }

  @Test
  void checkDigitUsesWeightsTwoToElevenAndRemainderAtMostOneGivesZero() {
    // 43 zeros and a final 1: sum 2, remainder 2 -> 9
    assertThat(Cdc.checkDigit("0".repeat(42) + "1")).isEqualTo(9);
    // final 5 at weight 2: sum 10 -> 1; 6: sum 12, remainder 1 -> 0; 0: remainder 0 -> 0
    assertThat(Cdc.checkDigit("0".repeat(42) + "5")).isEqualTo(1);
    assertThat(Cdc.checkDigit("0".repeat(42) + "6")).isEqualTo(0);
    assertThat(Cdc.checkDigit("0".repeat(43))).isEqualTo(0);
    // the 11th digit from the right has weight 2 again: 1*2 -> 9
    assertThat(Cdc.checkDigit("0".repeat(32) + "1" + "0".repeat(10))).isEqualTo(9);
  }

  @Test
  void rucEndingInALetterStaysInTheCdcAndIsConvertedToAsciiForTheCheckDigit() {
    LocalDate date = LocalDate.of(2026, 1, 1);
    Cdc cdc = Cdc.compose("01", "4444440A", "7", "001", "001", "1", "1", date, "1", "123456789");
    String base = cdc.value().substring(0, 43);
    assertThat(cdc.issuerRuc()).isEqualTo("4444440A");
    // Manual A002: the letter is replaced by its ASCII code (A = 65) only to compute the digit.
    String numeric = base.replace("A", "65");
    int weight = 2;
    int sum = 0;
    for (int i = numeric.length() - 1; i >= 0; i--) {
      sum += (numeric.charAt(i) - '0') * weight;
      weight = weight == 11 ? 2 : weight + 1;
    }
    int expected = sum % 11 > 1 ? 11 - sum % 11 : 0;
    assertThat(cdc.checkDigit()).isEqualTo(String.valueOf(expected));
    assertThat(Cdc.parse(cdc.value())).isEqualTo(cdc);
  }

  @Test
  void securityCodeHasNineDigitsInRangeAndDiffersFromTheDocumentNumber() {
    SecureRandom random = new SecureRandom();
    for (int i = 0; i < 1000; i++) {
      String code = SecurityCode.generate(random, "0014528");
      assertThat(code).hasSize(9).containsOnlyDigits().isNotEqualTo("000000000");
      assertThat(Integer.parseInt(code)).isBetween(1, 999_999_999).isNotEqualTo(14528);
    }
  }

  @Test
  void securityCodeIsDrawnAgainWhenItEqualsTheDocumentNumber() {
    // A source that first returns the document number (minus one, since 1 is added) and then 41.
    SecureRandom fixed =
        new SecureRandom() {
          private static final long serialVersionUID = 1L;
          private int calls;

          @Override
          public int nextInt(int bound) {
            return calls++ == 0 ? 14527 : 41;
          }
        };
    assertThat(SecurityCode.generate(fixed, "0014528")).isEqualTo("000000042");
  }

  @Test
  void securityCodeRejectsANonNumericDocumentNumber() {
    assertThatThrownBy(() -> SecurityCode.generate(new SecureRandom(), "12a"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
