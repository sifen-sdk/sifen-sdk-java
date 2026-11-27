/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.internal.mapping.Samples;
import io.github.sifensdk.core.internal.mapping.TransportSamples;
import io.github.sifensdk.core.model.Carrier;
import io.github.sifensdk.core.model.CommercialOperation;
import io.github.sifensdk.core.model.DeliveryNote;
import io.github.sifensdk.core.model.DocumentTypeData;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.Item;
import io.github.sifensdk.core.model.Transport;
import io.github.sifensdk.core.model.TransportVehicle;
import io.github.sifensdk.core.signing.Csc;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.KeyMaterials;
import io.github.sifensdk.core.signing.SignedDocument;
import io.github.sifensdk.core.validation.ValidationIssue;
import io.github.sifensdk.core.validation.ValidationResult;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * The electronic delivery note (C002 = 7) and the transport of goods (E900) end to end, offline:
 * model, mapping, rules of {@code spec} and XSD.
 */
class TransportDocumentsTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-11-26T13:05:00Z"), ZoneOffset.UTC);

  private final SifenDocuments documents =
      SifenDocuments.builder().environment(Environment.TEST).clock(CLOCK).build();

  @Test
  void theSampleDeliveryNoteAndInvoiceWithTransportAreValid() {
    for (ElectronicDocument document :
        new ElectronicDocument[] {
          TransportSamples.deliveryNote(), TransportSamples.invoiceWithTransport()
        }) {
      ValidationResult result = documents.validate(document);
      assertThat(result.errors()).as(result.issues().toString()).isEmpty();
      assertThat(result.uncheckedRules()).doesNotContain("R-E506-D1", "R-E910-D1");
    }
  }

  @Test
  void signsTheDeliveryNoteWithoutTotals() throws IOException {
    SignedDocument signed =
        documents.sign(TransportSamples.deliveryNote(), keyMaterial(), testCsc());
    String xml = new String(signed.xml(), StandardCharsets.UTF_8);
    assertThat(xml).contains("<gCamNRE>", "<gTransp>").doesNotContain("<gTotSub>", "<gCamCond>");
    assertThat(signed.qrUrl()).contains("&dTotGralOpe=0&dTotIVA=0&cItems=1&");
  }

  // --- what the delivery note changes in the rest of the document --------------------------------

  @Test
  void theDeliveryNoteGroupIsRequiredInADeliveryNoteAndOnlyThere() {
    assertThat(ruleIds(note(d -> d.deliveryNote(null)))).contains("R-E500-C1");
    ElectronicDocument invoice = Samples.invoice();
    ElectronicDocument invoiceWithNote =
        invoice.toBuilder()
            .documentTypeData(
                invoice.documentTypeData().toBuilder()
                    .deliveryNote(TransportSamples.deliveryNote().documentTypeData().deliveryNote())
                    .build())
            .build();
    assertThat(ruleIds(invoiceWithNote)).contains("R-E500-P1");
  }

  @Test
  void aDeliveryNoteHasNoItemValuesVatTotalsConditionOrCommercialOperation() {
    ElectronicDocument invoice = Samples.invoice();
    Item valued = invoice.documentTypeData().items().get(0);
    ElectronicDocument note = TransportSamples.deliveryNote();
    Item goods = note.documentTypeData().items().get(0);

    assertThat(
            ruleIds(
                note(d ->
                    d.items(
                        List.of(
                            goods.toBuilder()
                                .valuation(valued.valuation())
                                .vat(valued.vat())
                                .build())))))
        .contains("R-E720-P1", "R-E730-P1");
    assertThat(
            ruleIds(
                note(d -> d.operationCondition(invoice.documentTypeData().operationCondition()))))
        .contains("R-E600-P1");
    assertThat(ruleIds(note.toBuilder().totals(invoice.totals()).build())).contains("R-F001-P1");
    assertThat(
            ruleIds(
                note.toBuilder()
                    .generalData(
                        note.generalData().toBuilder()
                            .commercialOperation(
                                CommercialOperation.builder()
                                    .taxTypeCode("1")
                                    .currencyCode("PYG")
                                    .build())
                            .build())
                    .build()))
        .contains("R-D010-P1");
  }

  @Test
  void aDeliveryNoteRequiresTheGoodsRelevanceCodeOfEachItem() {
    Item goods = TransportSamples.deliveryNote().documentTypeData().items().get(0);
    assertThat(
            ruleIds(
                note(d ->
                    d.items(
                        List.of(
                            goods.toBuilder()
                                .goodsRelevanceCode(null)
                                .breakageOrShrinkageQuantity(null)
                                .breakageOrShrinkageRate(null)
                                .build())))))
        .contains("R-E715-C1");
  }

  // --- E6 rules --------------------------------------------------------------------------------

  @Test
  void aSaleWithoutAssociatedInvoiceRequiresTheFutureInvoiceDate() {
    assertThat(ruleIds(noteGroup(n -> n.futureInvoiceDate(null)))).contains("R-E506-C1");
    assertThat(ruleIds(noteGroup(n -> n.reasonCode("2").futureInvoiceDate(null))))
        .doesNotContain("R-E506-C1");
  }

  @Test
  void theFutureInvoiceDateMustNotBeInALaterMonth() {
    // MT150 2604, implemented in Java (R-E506-D1).
    ValidationResult later =
        validate(noteGroup(n -> n.futureInvoiceDate(LocalDate.of(2026, 12, 1))));
    assertThat(issueOf(later, "R-E506-D1").fieldId()).isEqualTo("E506");
    assertThat(issueOf(later, "R-E506-D1").sifenCode()).isEqualTo("2604");
    assertThat(ruleIds(noteGroup(n -> n.futureInvoiceDate(LocalDate.of(2026, 11, 1)))))
        .doesNotContain("R-E506-D1");
    assertThat(ruleIds(noteGroup(n -> n.futureInvoiceDate(LocalDate.of(2025, 12, 31)))))
        .doesNotContain("R-E506-D1");
  }

  @Test
  void aTransferBetweenOwnPremisesRequiresTheIssuerAsReceiver() {
    assertThat(ruleIds(noteGroup(n -> n.reasonCode("7")))).contains("R-E501-D1");
  }

  @Test
  void aDescriptionThatDoesNotMatchItsCodeIsReported() {
    // The SDK writes the catalog description; an "other" code (99) takes the given one.
    assertThat(ruleIds(noteGroup(n -> n.reasonCode("99").reasonDescription(null))))
        .contains("R-E502-OB");
    assertThat(ruleIds(noteGroup(n -> n.reasonCode("99").reasonDescription("Motivo propio"))))
        .doesNotContain("R-E502-OB", "R-E502-D1");
  }

  // --- E10 rules -------------------------------------------------------------------------------

  @Test
  void transportIsRequiredInADeliveryNoteOptionalInAnInvoiceAndProhibitedInACreditNote() {
    assertThat(ruleIds(note(d -> d.transport(null)))).contains("R-E900-C1");
    assertThat(ruleIds(Samples.invoice())).doesNotContain("R-E900-C1");
    ElectronicDocument creditNote = Samples.creditNote();
    ElectronicDocument withTransport =
        creditNote.toBuilder()
            .documentTypeData(
                creditNote.documentTypeData().toBuilder()
                    .transport(
                        TransportSamples.invoiceWithTransport().documentTypeData().transport())
                    .build())
            .build();
    assertThat(ruleIds(withTransport)).contains("R-E900-P1", "R-E940-P1", "R-E960-P1");
  }

  @Test
  void aDeliveryNoteRequiresTypeDatesDepartureDeliveryAndVehicle() {
    assertThat(ruleIds(transport(t -> t.typeCode(null)))).contains("R-E901-C1");
    assertThat(ruleIds(transport(t -> t.estimatedStartDate(null).estimatedEndDate(null))))
        .contains("R-E909-C1", "R-E910-C2");
    assertThat(ruleIds(transport(t -> t.departureLocation(null)))).contains("R-E920-C1");
    assertThat(ruleIds(transport(t -> t.deliveryLocations(List.of())))).contains("R-E940-C1");
    assertThat(ruleIds(transport(t -> t.vehicles(List.of())))).contains("R-E960-C1");
  }

  @Test
  void theEndDateMustNotBeBeforeTheStartDate() {
    // C-402: compared with E909.
    assertThat(ruleIds(transport(t -> t.estimatedEndDate(LocalDate.of(2026, 11, 25)))))
        .contains("R-E910-D1");
  }

  @Test
  void anImportRequiresTheDispatchNumber() {
    assertThat(ruleIds(noteGroup(n -> n.reasonCode("5")))).contains("R-E908-C1");
  }

  @Test
  void theVehicleIdentificationFollowsItsType() {
    assertThat(ruleIds(vehicle(v -> v.identificationNumber(null)))).contains("R-E963-C1");
    assertThat(ruleIds(vehicle(v -> v.identificationTypeCode("2")))).contains("R-E965-C1");
    // C-403: the flight number is E966, prohibited outside air transport.
    assertThat(ruleIds(vehicle(v -> v.flightNumber("PZ1234")))).contains("R-E966-P1");
    assertThat(ruleIds(transport(t -> t.modeCode("3")))).contains("R-E966-C1");
  }

  @Test
  void theCarrierIsRequiredUnlessTheTruckIsIdentifiedByItsNumber() {
    // R-E980-C1 (NT 10, C-407): optional by road with E967 = 1, required otherwise.
    assertThat(ruleIds(transport(t -> t.carrier(null)))).doesNotContain("R-E980-C1");
    assertThat(
            ruleIds(
                transport(t ->
                    t.carrier(null)
                        .vehicles(
                            List.of(
                                TransportVehicle.builder()
                                    .type("Camión")
                                    .brand("Marca")
                                    .identificationTypeCode("2")
                                    .plateNumber("ABC123")
                                    .build())))))
        .contains("R-E980-C1");
  }

  @Test
  void theCarrierIdentificationFollowsItsNature() {
    assertThat(ruleIds(carrier(c -> c.ruc("44444402").checkDigit("5")))).contains("R-E983-P1");
    assertThat(ruleIds(carrier(c -> c.idDocumentTypeCode(null).idDocumentNumber(null))))
        .contains("R-E985-C1");
    assertThat(
            ruleIds(
                carrier(c ->
                    c.natureCode("1")
                        .idDocumentTypeCode(null)
                        .idDocumentNumber(null)
                        .ruc("44444402")
                        .checkDigit("3"))))
        .contains("R-E984-F1")
        .doesNotContain("R-E983-C1", "R-E985-P1");
    assertThat(ruleIds(carrier(c -> c.nationalityCode("PRY")))).doesNotContain("R-E989-C1");
  }

  @Test
  void departmentDistrictAndCityOfTheLocationsMustBeRelated() {
    assertThat(
            ruleIds(
                transport(t ->
                    t.departureLocation(
                        t.build().departureLocation().toBuilder().cityCode("3").build()))))
        .contains("R-E929-D2");
  }

  // ---------------------------------------------------------------------------------------------

  private ValidationResult validate(ElectronicDocument document) {
    return documents.validate(document);
  }

  private Set<String> ruleIds(ElectronicDocument document) {
    return validate(document).errors().stream()
        .map(ValidationIssue::ruleId)
        .collect(Collectors.toSet());
  }

  private static ValidationIssue issueOf(ValidationResult result, String ruleId) {
    return result.errors().stream()
        .filter(i -> i.ruleId().equals(ruleId))
        .findFirst()
        .orElseThrow(() -> new AssertionError(ruleId + " not reported: " + result.issues()));
  }

  private static ElectronicDocument note(UnaryOperator<DocumentTypeData.Builder> change) {
    ElectronicDocument note = TransportSamples.deliveryNote();
    return note.toBuilder()
        .documentTypeData(change.apply(note.documentTypeData().toBuilder()).build())
        .build();
  }

  private static ElectronicDocument noteGroup(UnaryOperator<DeliveryNote.Builder> change) {
    return note(d -> d.deliveryNote(change.apply(d.build().deliveryNote().toBuilder()).build()));
  }

  private static ElectronicDocument transport(UnaryOperator<Transport.Builder> change) {
    return note(d -> d.transport(change.apply(d.build().transport().toBuilder()).build()));
  }

  private static ElectronicDocument vehicle(UnaryOperator<TransportVehicle.Builder> change) {
    return transport(
        t -> t.vehicles(List.of(change.apply(t.build().vehicles().get(0).toBuilder()).build())));
  }

  private static ElectronicDocument carrier(UnaryOperator<Carrier.Builder> change) {
    return transport(t -> t.carrier(change.apply(t.build().carrier().toBuilder()).build()));
  }

  private static KeyMaterial keyMaterial() throws IOException {
    try (InputStream in = TransportDocumentsTest.class.getResourceAsStream("/signing/prueba.p12")) {
      return KeyMaterials.pkcs12(in.readAllBytes(), "prueba-sintetica".toCharArray());
    }
  }

  private static Csc testCsc() {
    return Csc.of("0001", "ABCD0000000000000000000000000000".toCharArray());
  }
}
