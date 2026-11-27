/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.model.AdditionalCommercialData;
import io.github.sifensdk.core.model.Electricity;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.Insurance;
import io.github.sifensdk.core.model.InsurancePolicy;
import io.github.sifensdk.core.model.SectorSpecificFields;
import io.github.sifensdk.core.model.Supermarket;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Synthetic invoices with the sector-specific commercial fields ({@code gCamEsp}, E790): the
 * invoice of {@link Samples#invoice()} (issued 2026-11-26, total 131000 guaraníes) with one sector
 * group each. Every value is invented.
 */
public final class SectorSamples {

  private SectorSamples() {}

  /**
   * An invoice with the electricity group ({@code gGrupEner}, E791): consumption 350.50 = 12750.50
   * − 12400.00 (R-E797-A1).
   *
   * @return the invoice
   */
  public static ElectronicDocument electricityInvoice() {
    return withSector(SectorSpecificFields.builder().addElectricity(electricity()).build());
  }

  /**
   * An invoice with the insurance group ({@code gGrupSeg}, E800) and one policy ({@code
   * gGrupPolSeg}, EA790), with the four required policy fields only.
   *
   * @return the invoice
   */
  public static ElectronicDocument insuranceInvoice() {
    return withSector(
        SectorSpecificFields.builder()
            .insurance(Insurance.builder().addPolicy(minimalPolicy()).build())
            .build());
  }

  /**
   * An invoice with the supermarket group ({@code gGrupSup}, E810): the receiver paid 150000 in
   * cash for 131000, got 18000 back and donated 1000.
   *
   * @return the invoice
   */
  public static ElectronicDocument supermarketInvoice() {
    return withSector(SectorSpecificFields.builder().supermarket(supermarket()).build());
  }

  /**
   * An invoice with the additional commercial data group ({@code gGrupAdi}, E820): a billing cycle
   * of November 2026 and a payment due date after the issue date (validations 2050-2055).
   *
   * @return the invoice
   */
  public static ElectronicDocument additionalCommercialDataInvoice() {
    return withSector(
        SectorSpecificFields.builder().additionalCommercialData(additionalData()).build());
  }

  /**
   * An invoice with every group of {@code gCamEsp} and every field informed, two electricity groups
   * included.
   *
   * @return the invoice
   */
  public static ElectronicDocument allSectorsInvoice() {
    return withSector(
        SectorSpecificFields.builder()
            .addElectricity(electricity())
            .addElectricity(
                Electricity.builder()
                    .meterNumber("MED-0002")
                    .previousReading(new BigDecimal("0"))
                    .currentReading(new BigDecimal("80.25"))
                    .consumption(new BigDecimal("80.25"))
                    .build())
            .insurance(
                Insurance.builder()
                    .companyCode("SEG-001")
                    .addPolicy(
                        minimalPolicy().toBuilder()
                            .validityStartDateTime(LocalDateTime.of(2026, 11, 26, 0, 0, 0))
                            .validityEndDateTime(LocalDateTime.of(2027, 11, 25, 23, 59, 59))
                            .itemInternalCode("A-001")
                            .build())
                    .addPolicy(
                        InsurancePolicy.builder()
                            .code("POL-AUTO")
                            .validityUnitDescription("mes")
                            .validityPeriod(new BigDecimal("6.5"))
                            .number("2026-000002")
                            .build())
                    .build())
            .supermarket(supermarket())
            .additionalCommercialData(
                additionalData().toBuilder()
                    .addPaymentDueDate(LocalDate.of(2026, 12, 20))
                    .addPaymentDueDate(LocalDate.of(2026, 12, 30))
                    .contractNumber("CTR-2026-0001")
                    .previousBalanceAmount(new BigDecimal("15000.5000"))
                    .dncpProcurementCode("DNCP-0001")
                    .build())
            .build());
  }

  private static Electricity electricity() {
    return Electricity.builder()
        .meterNumber("MED-0001")
        .activityCode("1")
        .categoryCode("R1")
        .previousReading(new BigDecimal("12400.00"))
        .currentReading(new BigDecimal("12750.50"))
        .consumption(new BigDecimal("350.50"))
        .build();
  }

  private static InsurancePolicy minimalPolicy() {
    return InsurancePolicy.builder()
        .code("POL-HOGAR")
        .validityUnitDescription("año")
        .validityPeriod(new BigDecimal("1"))
        .number("2026-000001")
        .build();
  }

  private static Supermarket supermarket() {
    return Supermarket.builder()
        .cashierName("Cajero 01")
        .cashAmount(new BigDecimal("150000"))
        .changeAmount(new BigDecimal("18000"))
        .donationAmount(new BigDecimal("1000"))
        .donationDescription("Redondeo solidario")
        .build();
  }

  private static AdditionalCommercialData additionalData() {
    return AdditionalCommercialData.builder()
        .cycle("2026-11")
        .cycleStartDate(LocalDate.of(2026, 11, 1))
        .cycleEndDate(LocalDate.of(2026, 11, 30))
        .addPaymentDueDate(LocalDate.of(2026, 12, 10))
        .build();
  }

  private static ElectronicDocument withSector(SectorSpecificFields sector) {
    ElectronicDocument invoice = Samples.invoice();
    return invoice.toBuilder()
        .documentTypeData(
            invoice.documentTypeData().toBuilder().sectorSpecificFields(sector).build())
        .build();
  }
}
