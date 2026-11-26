/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.internal.json.Json;
import io.github.sifensdk.core.model.ElectronicDocument;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class CdcTest {

  @Test
  @SuppressWarnings("unchecked")
  void matchesTheOfficialExampleOfTheManual() throws IOException {
    Map<String, Object> example;
    try (InputStream in =
        Objects.requireNonNull(
            getClass().getResourceAsStream("/conformidad/cdc/mt150-ejemplo-10-1.json"))) {
      example =
          (Map<String, Object>) Json.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }
    Map<String, String> in = (Map<String, String>) example.get("entrada");
    Map<String, String> expected = (Map<String, String>) example.get("esperado");

    LocalDate day = LocalDate.parse(in.get("fechaEmision"), DateTimeFormatter.BASIC_ISO_DATE);
    ElectronicDocument document =
        withCdcFields(
            in.get("tipoDocumento"),
            in.get("rucEmisor"),
            in.get("digitoVerificadorRuc"),
            in.get("establecimiento"),
            in.get("puntoExpedicion"),
            in.get("numeroDocumento"),
            in.get("tipoContribuyente"),
            day.atTime(15, 45, 0),
            in.get("tipoEmision"),
            in.get("codigoSeguridad"));

    String cdc = Cdc.of(document);
    assertThat(cdc).isEqualTo(expected.get("cdc")).hasSize(44);
    assertThat(String.valueOf(cdc.charAt(43))).isEqualTo(expected.get("digitoVerificador"));
    assertThat(DocumentMapper.toTree(document, day.atTime(16, 0)).findAll("A003").get(0).text())
        .isEqualTo(expected.get("digitoVerificador"));
  }

  @Test
  void padsDocumentTypeRucAndNumberWithZeros() {
    ElectronicDocument document =
        withCdcFields(
            "5",
            "800001",
            "3",
            "002",
            "003",
            "45",
            "1",
            LocalDateTime.of(2026, 11, 26, 8, 0),
            "1",
            "000000001");
    String cdc = Cdc.of(document);
    assertThat(cdc).hasSize(44).startsWith("0500800001300200300000451202611261000000001");
    assertThat(cdc.charAt(43) - '0')
        .isEqualTo(io.github.sifensdk.core.cdc.Cdc.checkDigit(cdc.substring(0, 43)));
  }

  private static ElectronicDocument withCdcFields(
      String type,
      String ruc,
      String dv,
      String establishment,
      String point,
      String number,
      String taxpayerType,
      LocalDateTime issued,
      String emissionType,
      String securityCode) {
    ElectronicDocument base = Samples.invoice();
    return base.toBuilder()
        .operation(
            base.operation().toBuilder()
                .emissionTypeCode(emissionType)
                .securityCode(securityCode)
                .build())
        .stamp(
            base.stamp().toBuilder()
                .documentTypeCode(type)
                .establishment(establishment)
                .dispatchPoint(point)
                .documentNumber(number)
                .build())
        .generalData(
            base.generalData().toBuilder()
                .issueDateTime(issued)
                .issuer(
                    base.generalData().issuer().toBuilder()
                        .ruc(ruc)
                        .checkDigit(dv)
                        .taxpayerTypeCode(taxpayerType)
                        .build())
                .build())
        .build();
  }
}
