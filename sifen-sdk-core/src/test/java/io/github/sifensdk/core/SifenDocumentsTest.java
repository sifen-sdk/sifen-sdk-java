/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.core.internal.mapping.Samples;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.signing.Csc;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.KeyMaterials;
import io.github.sifensdk.core.signing.SignedDocument;
import io.github.sifensdk.core.validation.ValidationException;
import io.github.sifensdk.core.validation.ValidationResult;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/** End-to-end, offline: model, rules, XSD, signature and QR working together. */
class SifenDocumentsTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-11-26T13:05:00Z"), ZoneOffset.UTC);

  private final SifenDocuments documents =
      SifenDocuments.builder().environment(Environment.TEST).clock(CLOCK).build();

  @Test
  void theSampleInvoiceAndCreditNoteAreValid() {
    for (ElectronicDocument document :
        new ElectronicDocument[] {Samples.invoice(), Samples.creditNote()}) {
      ValidationResult result = documents.validate(document);
      assertThat(result.errors()).as(result.issues().toString()).isEmpty();
    }
  }

  @Test
  void signsAValidInvoiceWithItsQrAndKeepsEveryAmountAsGiven() throws IOException {
    SignedDocument signed = documents.sign(Samples.invoice(), keyMaterial(), testCsc());

    String xml = new String(signed.xml(), StandardCharsets.UTF_8);
    assertThat(signed.cdc()).hasSize(44);
    assertThat(xml).contains("<DE Id=\"" + signed.cdc() + "\">");
    assertThat(xml.indexOf("<Signature")).isGreaterThan(xml.indexOf("</DE>"));
    assertThat(xml.indexOf("<gCamFuFD>")).isGreaterThan(xml.indexOf("</Signature>"));
    assertThat(signed.qrUrl())
        .startsWith("https://ekuatia.set.gov.py/consultas-test/qr?nVersion=150");
    assertThat(xml).contains("<dFecFirma>2026-11-26T10:05:00</dFecFirma>");
    assertThat(xml)
        .contains("<dTotGralOpe>" + Samples.invoice().totals().netTotal().toPlainString());
  }

  @Test
  void refusesToSignAnInvalidDocumentAndSaysWhy() throws IOException {
    ElectronicDocument invoice = Samples.invoice();
    ElectronicDocument wrongTotal =
        invoice.toBuilder()
            .totals(
                invoice.totals().toBuilder()
                    .netTotal(invoice.totals().netTotal().add(BigDecimal.ONE))
                    .build())
            .build();

    assertThatThrownBy(() -> documents.sign(wrongTotal, keyMaterial(), testCsc()))
        .isInstanceOf(ValidationException.class)
        .satisfies(e ->
            assertThat(((ValidationException) e).result().errors())
                .anySatisfy(i -> assertThat(i.fieldId()).isEqualTo("F014")));
  }

  @Test
  void reportsWithoutChangingItAValueTheXsdRejects() {
    // The XSD only admits the VAT rate as an integer: 10.00 is reported, never rewritten as 10.
    ElectronicDocument invoice = Samples.invoice();
    ElectronicDocument scaled =
        invoice.toBuilder()
            .documentTypeData(
                invoice.documentTypeData().toBuilder()
                    .items(
                        invoice.documentTypeData().items().stream()
                            .map(item ->
                                item.toBuilder()
                                    .vat(
                                        item.vat().toBuilder()
                                            .rate(item.vat().rate().setScale(2))
                                            .build())
                                    .build())
                            .toList())
                    .build())
            .build();
    assertThat(documents.validate(scaled).errors())
        .isNotEmpty()
        .allSatisfy(i -> {
          assertThat(i.ruleId()).isEqualTo("XSD");
          assertThat(i.fieldId()).isEqualTo("E734");
        });
  }

  private static KeyMaterial keyMaterial() throws IOException {
    try (InputStream in = SifenDocumentsTest.class.getResourceAsStream("/signing/prueba.p12")) {
      return KeyMaterials.pkcs12(in.readAllBytes(), "prueba-sintetica".toCharArray());
    }
  }

  private static Csc testCsc() {
    return Csc.of("0001", "ABCD0000000000000000000000000000".toCharArray());
  }
}
