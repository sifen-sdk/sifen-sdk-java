/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.qr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.internal.json.Json;
import io.github.sifensdk.core.signing.Csc;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class QrCodeTest {

  private static final String CDC = "01444444017001001001452822017012515873260988";
  private static final Csc TEST_CSC =
      Csc.of("0001", "ABCD0000000000000000000000000000".toCharArray());

  @SuppressWarnings("unchecked")
  private static Map<String, String> section(String name) throws IOException {
    String path = "/conformidad/qr/mt150-ejemplo-13-8-4.json";
    try (InputStream in = Objects.requireNonNull(QrCodeTest.class.getResourceAsStream(path))) {
      Map<String, Object> all =
          (Map<String, Object>) Json.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
      return (Map<String, String>) all.get(name);
    }
  }

  @Test
  void reproducesTheOfficialExampleByteForByte() throws IOException {
    Map<String, String> in = section("entrada");
    Map<String, String> expected = section("esperado");
    assertThat(in.get("ambiente")).isEqualTo("produccion");

    QrParameters parameters =
        new QrParameters(
            in.get("nVersion"),
            in.get("Id"),
            in.get("dFeEmiDE"),
            in.get("dRucRec"),
            null,
            in.get("dTotGralOpe"),
            in.get("dTotIVA"),
            Integer.parseInt(in.get("cItems")),
            in.get("DigestValue"));
    Csc csc = Csc.of(in.get("IdCSC"), in.get("CSC").toCharArray());

    QrCode qr = QrCode.generate(parameters, csc, Environment.PRODUCTION);

    assertThat(qr.data()).isEqualTo(expected.get("datos"));
    assertThat(qr.hash()).isEqualTo(expected.get("cHashQR"));
    assertThat(qr.url()).isEqualTo(expected.get("url"));
    assertThat(qr.url().replace("&", "&amp;")).isEqualTo(expected.get("dCarQR"));
    assertThat(qr.url()).doesNotContain(in.get("CSC"));
    assertThat(qr.toString()).doesNotContain(CDC);
  }

  @Test
  void usesTheTestAddressInTheTestEnvironment() {
    QrCode qr = QrCode.generate(parameters("88899990", null, "1", "0"), TEST_CSC, Environment.TEST);
    assertThat(qr.url()).startsWith("https://ekuatia.set.gov.py/consultas-test/qr?nVersion=150&");
  }

  @Test
  void usesTheIdentityDocumentWhenTheReceiverHasNoRuc() {
    QrCode qr = QrCode.generate(parameters(null, "1234567", "1", "0"), TEST_CSC, Environment.TEST);
    assertThat(qr.data()).contains("&dNumIDRec=1234567&").doesNotContain("dRucRec");
  }

  @Test
  void writesZeroForMissingValues() {
    QrCode qr = QrCode.generate(parameters(null, null, null, null), TEST_CSC, Environment.TEST);
    assertThat(qr.data()).contains("&dNumIDRec=0&dTotGralOpe=0&dTotIVA=0&cItems=1&");
  }

  @Test
  void rejectsBothReceiverIdentifications() {
    assertThatThrownBy(() -> parameters("88899990", "1234567", "1", "0"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static QrParameters parameters(
      String ruc, String idNumber, String total, String vatTotal) {
    return new QrParameters(
        "150",
        CDC,
        "2017-01-25T09:35:17",
        ruc,
        idNumber,
        total,
        vatTotal,
        1,
        "yzGYhUx1/XYYzksWB+fPR3Qc50c=");
  }
}
