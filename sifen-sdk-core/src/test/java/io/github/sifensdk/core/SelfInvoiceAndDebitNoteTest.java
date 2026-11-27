/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.internal.mapping.Samples;
import io.github.sifensdk.core.model.AssociatedDocument;
import io.github.sifensdk.core.model.DocumentTypeData;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.Item;
import io.github.sifensdk.core.model.ItemVat;
import io.github.sifensdk.core.model.Receiver;
import io.github.sifensdk.core.model.SelfInvoice;
import io.github.sifensdk.core.model.Totals;
import io.github.sifensdk.core.validation.ValidationIssue;
import io.github.sifensdk.core.validation.ValidationResult;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * End-to-end, offline: the self-invoice (C002 = 4) and the debit note (C002 = 6) of {@link Samples}
 * are valid (rules, arithmetic and XSD), and what the specification requires of each type is
 * reported when it is broken. Every value is synthetic.
 */
class SelfInvoiceAndDebitNoteTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-11-27T19:05:00Z"), ZoneOffset.UTC);

  private final SifenDocuments documents =
      SifenDocuments.builder().environment(Environment.TEST).clock(CLOCK).build();

  // --- self-invoice ------------------------------------------------------------------------------

  @Test
  void theSampleSelfInvoiceIsValid() {
    ValidationResult result = documents.validate(Samples.selfInvoice());
    assertThat(result.errors()).as(result.issues().toString()).isEmpty();
  }

  @Test
  void theSelfInvoiceGroupIsRequiredWithC002Equal4() {
    ElectronicDocument doc = withTypeData(d -> d.toBuilder().selfInvoice(null).build());
    assertThat(ruleIds(documents.validate(doc))).contains("R-E300-C1");
  }

  @Test
  void theSelfInvoiceGroupIsProhibitedInOtherTypes() {
    ElectronicDocument invoice = Samples.invoice();
    ElectronicDocument doc =
        invoice.toBuilder()
            .documentTypeData(
                invoice.documentTypeData().toBuilder()
                    .selfInvoice(Samples.selfInvoice().documentTypeData().selfInvoice())
                    .build())
            .build();
    assertThat(ruleIds(documents.validate(doc))).contains("R-E300-P1");
  }

  @Test
  void theSellerDistrictDescriptionIsCompletedAndChecked() {
    // R-E313-C1: description required when E312 is present; the SDK writes it from the catalog.
    ElectronicDocument doc = withSeller(s -> s.toBuilder().sellerDistrictCode("1").build());
    ValidationResult result = documents.validate(doc);
    assertThat(result.errors()).as(result.issues().toString()).isEmpty();
  }

  @Test
  void theSellerLocationMustBeRelated() {
    // R-E310-D1 (2553): department, district and city of the seller must be related.
    ElectronicDocument doc = withSeller(s -> s.toBuilder().sellerDepartmentCode("2").build());
    assertThat(ruleIds(documents.validate(doc))).contains("R-E310-D1");
    ElectronicDocument place =
        withSeller(s -> s.toBuilder().transactionDepartmentCode("2").build());
    assertThat(ruleIds(documents.validate(place))).contains("R-E317-D1");
  }

  @Test
  void theItemVatGroupIsNotInformed() {
    // R-E730-P1: no gCamIVA with C002 = 4.
    ItemVat vat =
        ItemVat.builder()
            .treatmentCode("1")
            .taxableProportionRate(new BigDecimal("100"))
            .rate(new BigDecimal("10"))
            .taxableBase(new BigDecimal("90909"))
            .amount(new BigDecimal("9091"))
            .exemptBase(new BigDecimal("0"))
            .build();
    ElectronicDocument doc =
        withTypeData(d ->
            d.toBuilder()
                .items(
                    d.items().stream()
                        .map(i -> i.toBuilder().vat(vat).build())
                        .collect(Collectors.toList()))
                .build());
    assertThat(ruleIds(documents.validate(doc))).contains("R-E730-P1");
  }

  @Test
  void theItemTotalIsPriceTimesQuantityAndTheGrossTotalTheirSum() {
    // R-EA008-A2: EA008 = E721 × E711, without discounts; R-F008-A3: F008 = sum of EA008.
    ElectronicDocument wrongItem =
        withTypeData(d -> {
          Item item = d.items().get(0);
          return d.toBuilder()
              .items(
                  List.of(
                      item.toBuilder()
                          .valuation(
                              item.valuation().toBuilder()
                                  .discountsAndAdvances(
                                      item.valuation().discountsAndAdvances().toBuilder()
                                          .total(new BigDecimal("99999"))
                                          .build())
                                  .build())
                          .build()))
              .build();
        });
    assertThat(ruleIds(documents.validate(wrongItem))).contains("R-EA008-A2");

    ElectronicDocument wrongGross =
        withTotals(t -> t.toBuilder().grossTotal(new BigDecimal("100001")).build());
    List<String> ids = ruleIds(documents.validate(wrongGross));
    assertThat(ids).contains("R-F008-A3");
    // R-F008-A1 (sum of F002 to F005) does not apply: those subtotals are prohibited (R-F00n-P4).
    assertThat(ids).doesNotContain("R-F008-A1");
  }

  @Test
  void subtotalsAndVatTotalsAreNotInformed() {
    ElectronicDocument doc =
        withTotals(t ->
            t.toBuilder()
                .vat10Subtotal(new BigDecimal("100000"))
                .vat10Amount(new BigDecimal("9091"))
                .vatTotal(new BigDecimal("9091"))
                .vat10TaxableBaseTotal(new BigDecimal("90909"))
                .vatTaxableBaseTotal(new BigDecimal("90909"))
                .commissionAmount(new BigDecimal("0"))
                .build());
    assertThat(ruleIds(documents.validate(doc)))
        .contains("R-F005-P4", "R-F016-P4", "R-F017-P4", "R-F019-P4", "R-F020-P4", "R-F025-P4");
  }

  @Test
  void theReceiverIsTheIssuer() {
    ElectronicDocument doc =
        withReceiver(r -> r.toBuilder().ruc("44444402").checkDigit("5").build());
    assertThat(ruleIds(documents.validate(doc))).contains("R-D206-D1");
    ElectronicDocument b2b = withReceiver(r -> r.toBuilder().operationTypeCode("1").build());
    assertThat(ruleIds(documents.validate(b2b))).contains("R-D202-F4");
  }

  @Test
  void theAssociatedDocumentIsAnElectronicProof() {
    ElectronicDocument self = Samples.selfInvoice();
    ElectronicDocument none = self.toBuilder().associatedDocuments(List.of()).build();
    assertThat(ruleIds(documents.validate(none))).contains("R-H001-C1");
    ElectronicDocument electronic =
        self.toBuilder().associatedDocuments(Samples.debitNote().associatedDocuments()).build();
    assertThat(ruleIds(documents.validate(electronic))).contains("R-H002-D1");
  }

  @Test
  void theSupersededGuaraniTotalFormulaIsNeverEvaluated() {
    // R-F023-A3 (F023 = F014 for C002 = 4) was removed by the NT 8 and F023 is prohibited with
    // C002 = 4 (R-F023-P4): it is neither reported nor listed as unchecked.
    ValidationResult result = documents.validate(Samples.selfInvoice());
    assertThat(ruleIds(result)).doesNotContain("R-F023-A3");
    assertThat(result.uncheckedRules()).doesNotContain("R-F023-A3");
  }

  // --- debit note --------------------------------------------------------------------------------

  @Test
  void theSampleDebitNoteIsValid() {
    ValidationResult result = documents.validate(Samples.debitNote());
    assertThat(result.errors()).as(result.issues().toString()).isEmpty();
  }

  @Test
  void theDebitNoteRequiresItsGroupAndAnAssociatedDocument() {
    ElectronicDocument note = Samples.debitNote();
    ElectronicDocument withoutGroup =
        note.toBuilder()
            .documentTypeData(note.documentTypeData().toBuilder().creditDebitNote(null).build())
            .build();
    assertThat(ruleIds(documents.validate(withoutGroup))).contains("R-E400-C1");
    ElectronicDocument withoutAssociated = note.toBuilder().associatedDocuments(List.of()).build();
    assertThat(ruleIds(documents.validate(withoutAssociated))).contains("R-H001-C1");
  }

  @Test
  void theDebitNoteFollowsTheNetTotalFormulaAndRejectsAnUnnamedReceiver() {
    ElectronicDocument note = Samples.debitNote();
    ElectronicDocument wrongTotal =
        note.toBuilder()
            .totals(note.totals().toBuilder().netTotal(new BigDecimal("110001")).build())
            .build();
    assertThat(ruleIds(documents.validate(wrongTotal))).contains("R-F014-A3");
    ElectronicDocument unnamed =
        note.toBuilder()
            .generalData(
                note.generalData().toBuilder()
                    .receiver(
                        Receiver.builder()
                            .natureCode("2")
                            .operationTypeCode("2")
                            .countryCode("PRY")
                            .idTypeCode("5")
                            .idNumber("0")
                            .name("Sin Nombre")
                            .build())
                    .build())
            .build();
    assertThat(ruleIds(documents.validate(unnamed))).contains("R-D208-F7");
  }

  @Test
  void theDebitNoteCannotBeAssociatedToAnElectronicProof() {
    ElectronicDocument note = Samples.debitNote();
    ElectronicDocument proof =
        note.toBuilder()
            .associatedDocuments(
                List.of(
                    AssociatedDocument.builder().typeCode("3").certificateTypeCode("1").build()))
            .build();
    assertThat(ruleIds(documents.validate(proof))).contains("R-H002-D2");
  }

  // ---------------------------------------------------------------------------------------------

  private static List<String> ruleIds(ValidationResult result) {
    return result.issues().stream().map(ValidationIssue::ruleId).collect(Collectors.toList());
  }

  private static ElectronicDocument withTypeData(UnaryOperator<DocumentTypeData> change) {
    ElectronicDocument self = Samples.selfInvoice();
    return self.toBuilder().documentTypeData(change.apply(self.documentTypeData())).build();
  }

  private static ElectronicDocument withSeller(UnaryOperator<SelfInvoice> change) {
    return withTypeData(d -> d.toBuilder().selfInvoice(change.apply(d.selfInvoice())).build());
  }

  private static ElectronicDocument withTotals(UnaryOperator<Totals> change) {
    ElectronicDocument self = Samples.selfInvoice();
    return self.toBuilder().totals(change.apply(self.totals())).build();
  }

  private static ElectronicDocument withReceiver(UnaryOperator<Receiver> change) {
    ElectronicDocument self = Samples.selfInvoice();
    return self.toBuilder()
        .generalData(
            self.generalData().toBuilder()
                .receiver(change.apply(self.generalData().receiver()))
                .build())
        .build();
  }
}
