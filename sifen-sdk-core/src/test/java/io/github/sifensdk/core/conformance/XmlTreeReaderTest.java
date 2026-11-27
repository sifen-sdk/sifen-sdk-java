/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.conformance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.core.internal.mapping.DocumentMapper;
import io.github.sifensdk.core.internal.mapping.EventMapper;
import io.github.sifensdk.core.internal.mapping.Samples;
import io.github.sifensdk.core.internal.mapping.SectorSamples;
import io.github.sifensdk.core.internal.mapping.TransportSamples;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.internal.xml.XmlWriter;
import io.github.sifensdk.core.model.Cancellation;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.Event;
import io.github.sifensdk.core.model.EventDetail;
import io.github.sifensdk.core.model.NumberInvalidation;
import io.github.sifensdk.core.model.ReceiptNotification;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The test-side XML reader of the conformance battery gives back the tree the mappers build: same
 * elements, field ids, texts and attributes, for documents and events written by {@link XmlWriter}.
 */
class XmlTreeReaderTest {

  private static final LocalDateTime SIGNED = LocalDateTime.of(2026, 11, 27, 9, 0, 0);
  private static final String CDC = "01444444017001001001452822017012515873260988"; // MT150 10.1

  static Stream<ElectronicDocument> documents() {
    return Stream.of(
        Samples.invoice(),
        Samples.creditNote(),
        Samples.debitNote(),
        Samples.selfInvoice(),
        TransportSamples.deliveryNote(),
        TransportSamples.invoiceWithTransport(),
        SectorSamples.allSectorsInvoice());
  }

  static Stream<EventDetail> events() {
    return Stream.of(
        Cancellation.builder().cdc(CDC).reason("Motivo de prueba").build(),
        NumberInvalidation.builder()
            .stampNumber("12345678")
            .establishment("001")
            .dispatchPoint("001")
            .rangeStartNumber("0000101")
            .rangeEndNumber("0000102")
            .documentTypeCode("1")
            .reason("Motivo de prueba")
            .build(),
        ReceiptNotification.builder()
            .cdc(CDC)
            .issueDateTime(SIGNED)
            .receptionDateTime(SIGNED.plusHours(1))
            .receiverTypeCode("1")
            .receiverName("Receptor de prueba")
            .receiverRuc("44444401")
            .receiverCheckDigit("7")
            .guaraniTotal(new BigDecimal("110000"))
            .build());
  }

  @ParameterizedTest
  @MethodSource("documents")
  void readsBackTheTreeOfADocument(ElectronicDocument document) {
    Node mapped = DocumentMapper.toTree(document, SIGNED);

    assertSameTree(XmlTreeReader.read(XmlWriter.write(mapped)), mapped);
  }

  @ParameterizedTest
  @MethodSource("events")
  void readsBackTheTreeOfAnEvent(EventDetail detail) {
    Node mapped = EventMapper.toTree(Event.builder().id("1").detail(detail).build(), SIGNED);

    assertSameTree(XmlTreeReader.read(XmlWriter.write(mapped)), mapped);
  }

  @Test
  void rejectsAnotherRoot() {
    byte[] xml =
        "<rEnviDe xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\"/>".getBytes(StandardCharsets.UTF_8);

    assertThatThrownBy(() -> XmlTreeReader.read(xml))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("rEnviDe");
  }

  private static void assertSameTree(Node read, Node mapped) {
    assertThat(read.path()).isEqualTo(mapped.path());
    assertThat(read.fieldId()).as(mapped.path()).isEqualTo(mapped.fieldId());
    assertThat(read.text()).as(mapped.path()).isEqualTo(mapped.text());
    assertThat(read.attributes()).as(mapped.path()).isEqualTo(mapped.attributes());
    assertThat(read.children()).as(mapped.path()).hasSameSizeAs(mapped.children());
    for (int i = 0; i < mapped.children().size(); i++) {
      assertSameTree(read.children().get(i), mapped.children().get(i));
    }
  }
}
