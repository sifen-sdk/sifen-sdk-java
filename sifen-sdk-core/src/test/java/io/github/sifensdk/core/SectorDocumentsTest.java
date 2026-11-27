/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.internal.mapping.SectorSamples;
import io.github.sifensdk.core.model.AdditionalCommercialData;
import io.github.sifensdk.core.model.Electricity;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.GeneralData;
import io.github.sifensdk.core.model.Insurance;
import io.github.sifensdk.core.model.SectorSpecificFields;
import io.github.sifensdk.core.validation.ValidationIssue;
import io.github.sifensdk.core.validation.ValidationResult;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;

/** The sector-specific fields (E9) through the whole offline validation: rules and XSD. */
class SectorDocumentsTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-11-26T13:05:00Z"), ZoneOffset.UTC);

  private final SifenDocuments documents =
      SifenDocuments.builder().environment(Environment.TEST).clock(CLOCK).build();

  @Test
  void everySectorSampleIsValid() {
    for (ElectronicDocument document :
        List.of(
            SectorSamples.electricityInvoice(),
            SectorSamples.insuranceInvoice(),
            SectorSamples.supermarketInvoice(),
            SectorSamples.additionalCommercialDataInvoice(),
            SectorSamples.allSectorsInvoice())) {
      ValidationResult result = documents.validate(document);
      assertThat(result.errors()).as(result.issues().toString()).isEmpty();
    }
  }

  // --- R-E797-A1 (NT 23): consumption = current reading − previous reading
  // ------------------------

  @Test
  void theConsumptionMustBeTheDifferenceOfTheReadings() {
    ValidationResult result =
        documents.validate(
            electricity(e -> e.toBuilder().consumption(new BigDecimal("351.01")).build()));
    assertThat(errors(result, "R-E797-A1"))
        .singleElement()
        .satisfies(i -> assertThat(i.fieldId()).isEqualTo("E797"));
  }

  @Test
  void theConsumptionAcceptsHalfAUnitOfDifference() {
    // ADR 0010: |reported − exact| <= 0.5 in the unit of the field.
    assertThat(
            errors(
                documents.validate(
                    electricity(e -> e.toBuilder().consumption(new BigDecimal("351.00")).build())),
                "R-E797-A1"))
        .isEmpty();
  }

  @Test
  void eachElectricityGroupIsCheckedWithItsOwnReadings() {
    // gGrupEner repeats up to 9 times (NT 23): the readings of one group never feed another's.
    ElectronicDocument document =
        withSector(
            SectorSpecificFields.builder()
                .addElectricity(reading("100", "150", "50"))
                .addElectricity(reading("200", "260", "50"))
                .build());
    List<ValidationIssue> found = errors(documents.validate(document), "R-E797-A1");
    assertThat(found).singleElement();
    assertThat(found.get(0).path()).contains("gGrupEner[2]");
  }

  @Test
  void theConsumptionIsNotCheckedWithoutBothReadings() {
    ElectronicDocument document =
        withSector(
            SectorSpecificFields.builder()
                .addElectricity(Electricity.builder().consumption(new BigDecimal("50")).build())
                .build());
    ValidationResult result = documents.validate(document);
    assertThat(result.errors()).as(result.issues().toString()).isEmpty();
  }

  @Test
  void beforeTheNt23TheOriginalFormulaIsReportedAsUnchecked() {
    // R-E797-A0 (MT150: E785 − E784, vehicle fields) applies until 2024-08-30 in test; its operands
    // are not readings, so the SDK does not evaluate it and R-E797-A1 is not yet in force.
    ElectronicDocument document =
        issuedOn(
            electricity(e -> e.toBuilder().consumption(new BigDecimal("999")).build()),
            LocalDate.of(2024, 8, 1));
    ValidationResult result = documents.validate(document);
    assertThat(result.uncheckedRules()).contains("R-E797-A0").doesNotContain("R-E797-A1");
    assertThat(errors(result, "R-E797-A1")).isEmpty();
    assertThat(errors(result, "R-E797-A0")).isEmpty();
  }

  // --- insurance: R-EA790-OB --------------------------------------------------------------------

  @Test
  void theInsuranceGroupNeedsAtLeastOnePolicy() {
    ElectronicDocument document =
        withSector(
            SectorSpecificFields.builder()
                .insurance(Insurance.builder().companyCode("SEG-001").build())
                .build());
    assertThat(documents.validate(document).errors())
        .anySatisfy(i -> assertThat(i.fieldId()).isEqualTo("EA790"));
  }

  // --- additional commercial data: validations 2050-2055 (C-401 reading) -------------------------

  @Test
  void theCycleRequiresItsStartDate() {
    ValidationResult result =
        documents.validate(additional(b -> b.cycleStartDate(null).cycleEndDate(null)));
    assertThat(errors(result, "R-E822-C1")).singleElement();
  }

  @Test
  void theCycleStartDateIsNotInformedWithoutACycle() {
    // C-401: the condition is on E821 (dCiclo), not on E811 (dNomCaj) as the Manual's text says.
    ValidationResult result = documents.validate(additional(b -> b.cycle(null)));
    assertThat(errors(result, "R-E822-P1")).singleElement();
  }

  @Test
  void theCycleStartRequiresTheCycleEnd() {
    ValidationResult result = documents.validate(additional(b -> b.cycleEndDate(null)));
    assertThat(errors(result, "R-E823-C1")).singleElement();
  }

  @Test
  void theCycleEndIsNotInformedWithoutTheCycleStart() {
    ValidationResult result =
        documents.validate(additional(b -> b.cycle(null).cycleStartDate(null)));
    assertThat(errors(result, "R-E823-P1")).singleElement();
  }

  @Test
  void theCycleEndIsNotBeforeItsStart() {
    ValidationResult result =
        documents.validate(additional(b -> b.cycleEndDate(LocalDate.of(2026, 10, 31))));
    assertThat(errors(result, "R-E823-D1")).singleElement();
    assertThat(
            errors(
                documents.validate(additional(b -> b.cycleEndDate(LocalDate.of(2026, 11, 1)))),
                "R-E823-D1"))
        .isEmpty();
  }

  @Test
  void thePaymentDueDateIsNotBeforeTheIssueDate() {
    ValidationResult result =
        documents.validate(
            additional(b ->
                b.addPaymentDueDate(LocalDate.of(2026, 11, 26))
                    .addPaymentDueDate(LocalDate.of(2026, 11, 25))));
    assertThat(errors(result, "R-E824-D1"))
        .singleElement()
        .satisfies(i -> assertThat(i.path()).contains("dVencPag[3]"));
  }

  // ---------------------------------------------------------------------------------------------

  private static List<ValidationIssue> errors(ValidationResult result, String ruleId) {
    return result.errors().stream().filter(i -> i.ruleId().equals(ruleId)).toList();
  }

  private static Electricity reading(String previous, String current, String consumption) {
    return Electricity.builder()
        .previousReading(new BigDecimal(previous))
        .currentReading(new BigDecimal(current))
        .consumption(new BigDecimal(consumption))
        .build();
  }

  private static ElectronicDocument electricity(UnaryOperator<Electricity> change) {
    ElectronicDocument sample = SectorSamples.electricityInvoice();
    Electricity e = sample.documentTypeData().sectorSpecificFields().electricity().get(0);
    return withSector(SectorSpecificFields.builder().addElectricity(change.apply(e)).build());
  }

  private static ElectronicDocument additional(
      UnaryOperator<AdditionalCommercialData.Builder> change) {
    AdditionalCommercialData a =
        SectorSamples.additionalCommercialDataInvoice()
            .documentTypeData()
            .sectorSpecificFields()
            .additionalCommercialData();
    return withSector(
        SectorSpecificFields.builder()
            .additionalCommercialData(change.apply(a.toBuilder()).build())
            .build());
  }

  private static ElectronicDocument withSector(SectorSpecificFields sector) {
    ElectronicDocument sample = SectorSamples.electricityInvoice();
    return sample.toBuilder()
        .documentTypeData(
            sample.documentTypeData().toBuilder().sectorSpecificFields(sector).build())
        .build();
  }

  private static ElectronicDocument issuedOn(ElectronicDocument document, LocalDate date) {
    GeneralData general = document.generalData();
    return document.toBuilder()
        .generalData(
            general.toBuilder()
                .issueDateTime(LocalDateTime.of(date, general.issueDateTime().toLocalTime()))
                .build())
        .build();
  }
}
