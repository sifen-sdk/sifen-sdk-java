/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.model.AffectedObligation;
import io.github.sifensdk.core.model.AssociatedDocument;
import io.github.sifensdk.core.model.CardPayment;
import io.github.sifensdk.core.model.Cargo;
import io.github.sifensdk.core.model.CheckPayment;
import io.github.sifensdk.core.model.CommercialOperation;
import io.github.sifensdk.core.model.ComplementaryData;
import io.github.sifensdk.core.model.Credit;
import io.github.sifensdk.core.model.CreditDebitNote;
import io.github.sifensdk.core.model.DocumentTypeData;
import io.github.sifensdk.core.model.EconomicActivity;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.GeneralData;
import io.github.sifensdk.core.model.GenerationResponsible;
import io.github.sifensdk.core.model.GoodsTracking;
import io.github.sifensdk.core.model.Installment;
import io.github.sifensdk.core.model.Invoice;
import io.github.sifensdk.core.model.Issuer;
import io.github.sifensdk.core.model.Item;
import io.github.sifensdk.core.model.ItemDiscountsAndAdvances;
import io.github.sifensdk.core.model.ItemValues;
import io.github.sifensdk.core.model.ItemVat;
import io.github.sifensdk.core.model.NewVehicle;
import io.github.sifensdk.core.model.Operation;
import io.github.sifensdk.core.model.OperationCondition;
import io.github.sifensdk.core.model.Payment;
import io.github.sifensdk.core.model.PublicProcurement;
import io.github.sifensdk.core.model.Receiver;
import io.github.sifensdk.core.model.Stamp;
import io.github.sifensdk.core.model.Totals;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A synthetic document with every field of the model informed (one element per list), used to check
 * that the mapping covers every field, in XSD order, and writes values as given. Not a valid SIFEN
 * document: it mixes groups of invoices and credit notes. The CDC components are those of the
 * example in MT150 section 10.1.
 */
final class FullDocument {

  private FullDocument() {}

  static ElectronicDocument document() {
    return ElectronicDocument.builder()
        .operation(
            Operation.builder()
                .emissionTypeCode("1")
                .emissionTypeDescription("Descripción libre")
                .securityCode("587326098")
                .issuerInformation("vB005")
                .taxAuthorityInformation("vB006")
                .build())
        .stamp(
            Stamp.builder()
                .documentTypeCode("1")
                .documentTypeDescription("Descripción libre")
                .number("vC004")
                .establishment("001")
                .dispatchPoint("001")
                .documentNumber("0014528")
                .series("vC010")
                .startDate(LocalDate.of(2026, 3, 14))
                .build())
        .generalData(
            GeneralData.builder()
                .issueDateTime(LocalDateTime.of(2017, 1, 25, 10, 11, 12))
                .commercialOperation(
                    CommercialOperation.builder()
                        .transactionTypeCode("1")
                        .transactionTypeDescription("Descripción libre")
                        .taxTypeCode("1")
                        .taxTypeDescription("Descripción libre")
                        .currencyCode("AED")
                        .currencyDescription("Descripción libre")
                        .exchangeRateConditionCode("1")
                        .exchangeRate(new BigDecimal("2200.500"))
                        .advanceConditionCode("1")
                        .advanceConditionDescription("Descripción libre")
                        .addAffectedObligation(
                            AffectedObligation.builder()
                                .code("113")
                                .description("Descripción libre")
                                .build())
                        .build())
                .issuer(
                    Issuer.builder()
                        .ruc("44444401")
                        .checkDigit("7")
                        .taxpayerTypeCode("2")
                        .regimeTypeCode("1")
                        .name("vD105")
                        .tradeName("vD106")
                        .address("vD107")
                        .houseNumber("vD108")
                        .addressComplement1("vD109")
                        .addressComplement2("vD110")
                        .departmentCode("1")
                        .departmentDescription("Descripción libre")
                        .districtCode("1")
                        .districtDescription("Descripción libre")
                        .cityCode("1")
                        .cityDescription("Descripción libre")
                        .phone("vD117")
                        .email("vD118")
                        .branchName("vD119")
                        .addEconomicActivity(
                            EconomicActivity.builder().code("vD131").description("vD132").build())
                        .generationResponsible(
                            GenerationResponsible.builder()
                                .idDocumentTypeCode("1")
                                .idDocumentTypeDescription("Descripción libre")
                                .idDocumentNumber("vD143")
                                .name("vD144")
                                .position("vD145")
                                .build())
                        .build())
                .receiver(
                    Receiver.builder()
                        .natureCode("1")
                        .operationTypeCode("1")
                        .countryCode("MKD")
                        .countryDescription("Descripción libre")
                        .taxpayerTypeCode("1")
                        .ruc("vD206")
                        .checkDigit("vD207")
                        .idDocumentTypeCode("1")
                        .idDocumentTypeDescription("Descripción libre")
                        .idDocumentNumber("vD210")
                        .name("vD211")
                        .tradeName("vD212")
                        .address("vD213")
                        .houseNumber("vD218")
                        .departmentCode("1")
                        .departmentDescription("Descripción libre")
                        .districtCode("1")
                        .districtDescription("Descripción libre")
                        .cityCode("1")
                        .cityDescription("Descripción libre")
                        .phone("vD214")
                        .mobilePhone("vD215")
                        .email("vD216")
                        .customerCode("vD217")
                        .build())
                .build())
        .documentTypeData(
            DocumentTypeData.builder()
                .invoice(
                    Invoice.builder()
                        .presenceIndicatorCode("1")
                        .presenceIndicatorDescription("Descripción libre")
                        .futureShipmentDate(LocalDate.of(2026, 3, 24))
                        .publicProcurement(
                            PublicProcurement.builder()
                                .modalityCode("vE021")
                                .entityCode("vE022")
                                .yearCode("vE023")
                                .sequence("vE024")
                                .contractingCodeIssueDate(LocalDate.of(2026, 3, 1))
                                .build())
                        .build())
                .creditDebitNote(
                    CreditDebitNote.builder()
                        .reasonCode("1")
                        .reasonDescription("Descripción libre")
                        .build())
                .operationCondition(
                    OperationCondition.builder()
                        .conditionCode("1")
                        .conditionDescription("Descripción libre")
                        .addPayment(
                            Payment.builder()
                                .typeCode("1")
                                .typeDescription("Descripción libre")
                                .amount(new BigDecimal("9100.5000"))
                                .currencyCode("AED")
                                .currencyDescription("Descripción libre")
                                .exchangeRate(new BigDecimal("9400.500"))
                                .card(
                                    CardPayment.builder()
                                        .brandCode("1")
                                        .brandDescription("Descripción libre")
                                        .processorName("vE623")
                                        .processorRuc("vE624")
                                        .processorCheckDigit("vE625")
                                        .processingMethodCode("1")
                                        .authorizationCode("vE627")
                                        .holderName("vE628")
                                        .number("vE629")
                                        .build())
                                .check(
                                    CheckPayment.builder()
                                        .number("vE631")
                                        .issuingBank("vE632")
                                        .build())
                                .build())
                        .credit(
                            Credit.builder()
                                .conditionCode("1")
                                .conditionDescription("Descripción libre")
                                .term("vE643")
                                .installmentCount(Integer.valueOf(109))
                                .downPaymentAmount(new BigDecimal("11000.500"))
                                .addInstallment(
                                    Installment.builder()
                                        .currencyCode("AED")
                                        .currencyDescription("Descripción libre")
                                        .amount(new BigDecimal("11300.50"))
                                        .dueDate(LocalDate.of(2026, 3, 3))
                                        .build())
                                .build())
                        .build())
                .addItem(
                    Item.builder()
                        .internalCode("vE701")
                        .tariffHeading("vE702")
                        .mercosurCommonNomenclature("vE703")
                        .dncpGeneralCode("vE704")
                        .dncpSpecificCode("vE705")
                        .productGtin("vE706")
                        .packageGtin("vE707")
                        .description("vE708")
                        .unitOfMeasureCode("87")
                        .unitOfMeasureDescription("Descripción libre")
                        .quantity(new BigDecimal("12500.50"))
                        .originCountryCode("MKD")
                        .originCountryDescription("Descripción libre")
                        .additionalInformation("vE714")
                        .goodsRelevanceCode("1")
                        .goodsRelevanceDescription("Descripción libre")
                        .breakageOrShrinkageQuantity(new BigDecimal("13100.5000"))
                        .breakageOrShrinkageRate(new BigDecimal("13200.5"))
                        .advanceCdc("vE719")
                        .values(
                            ItemValues.builder()
                                .unitPrice(new BigDecimal("13400.500"))
                                .exchangeRate(new BigDecimal("13500.5000"))
                                .grossTotal(new BigDecimal("13600.5"))
                                .discountsAndAdvances(
                                    ItemDiscountsAndAdvances.builder()
                                        .discountAmount(new BigDecimal("13700.50"))
                                        .discountRate(new BigDecimal("13800.500"))
                                        .globalDiscountAmount(new BigDecimal("13900.5000"))
                                        .advanceAmount(new BigDecimal("14000.5"))
                                        .globalAdvanceAmount(new BigDecimal("14100.50"))
                                        .total(new BigDecimal("14200.500"))
                                        .guaraniTotal(new BigDecimal("14300.5000"))
                                        .build())
                                .build())
                        .vat(
                            ItemVat.builder()
                                .treatmentCode("1")
                                .treatmentDescription("Descripción libre")
                                .taxableProportionRate(new BigDecimal("14600.500"))
                                .rate(new BigDecimal("14700.5000"))
                                .taxableBase(new BigDecimal("14800.5"))
                                .amount(new BigDecimal("14900.50"))
                                .exemptBase(new BigDecimal("15000.500"))
                                .build())
                        .goodsTracking(
                            GoodsTracking.builder()
                                .lotNumber("vE751")
                                .expirationDate(LocalDate.of(2026, 3, 13))
                                .serialNumber("vE753")
                                .orderNumber("vE754")
                                .shipmentTrackingNumber("vE755")
                                .productRegistrationNumber("vE759")
                                .commercialEntityRegistrationNumber("vE760")
                                .productName("vE761")
                                .build())
                        .newVehicle(
                            NewVehicle.builder()
                                .saleOperationTypeCode("1")
                                .saleOperationTypeDescription("Descripción libre")
                                .chassis("vE773")
                                .color("vE774")
                                .enginePower(Integer.valueOf(163))
                                .engineCapacity(Integer.valueOf(164))
                                .netWeight(new BigDecimal("16500.50"))
                                .grossWeight(new BigDecimal("16600.500"))
                                .fuelTypeCode("1")
                                .fuelTypeDescription("Descripción libre")
                                .engineNumber("vE781")
                                .maxTractionCapacity(new BigDecimal("17000.500"))
                                .manufactureYear(Integer.valueOf(171))
                                .vehicleType("vE784")
                                .passengerCapacity(Integer.valueOf(173))
                                .cylinders("vE786")
                                .build())
                        .build())
                .build())
        .totals(
            Totals.builder()
                .exemptSubtotal(new BigDecimal("17500.5000"))
                .exoneratedSubtotal(new BigDecimal("17600.5"))
                .vat5Subtotal(new BigDecimal("17700.50"))
                .vat10Subtotal(new BigDecimal("17800.500"))
                .grossTotal(new BigDecimal("17900.5000"))
                .itemDiscountTotal(new BigDecimal("18000.5"))
                .itemGlobalDiscountTotal(new BigDecimal("18100.50"))
                .itemAdvanceTotal(new BigDecimal("18200.500"))
                .itemGlobalAdvanceTotal(new BigDecimal("18300.5000"))
                .globalDiscountRate(new BigDecimal("18400.5"))
                .discountTotal(new BigDecimal("18500.50"))
                .advanceTotal(new BigDecimal("18600.500"))
                .roundingAmount(new BigDecimal("18700.5000"))
                .commissionAmount(new BigDecimal("18800.5"))
                .netTotal(new BigDecimal("18900.50"))
                .vat5Amount(new BigDecimal("19000.500"))
                .vat10Amount(new BigDecimal("19100.5000"))
                .vat5RoundingAmount(new BigDecimal("19200.5"))
                .vat10RoundingAmount(new BigDecimal("19300.50"))
                .commissionVatAmount(new BigDecimal("19400.500"))
                .vatTotal(new BigDecimal("19500.5000"))
                .vat5TaxableBaseTotal(new BigDecimal("19600.5"))
                .vat10TaxableBaseTotal(new BigDecimal("19700.50"))
                .vatTaxableBaseTotal(new BigDecimal("19800.500"))
                .guaraniTotal(new BigDecimal("19900.5000"))
                .build())
        .complementaryData(
            ComplementaryData.builder()
                .purchaseOrderNumber("vG002")
                .salesOrderNumber("vG003")
                .accountingEntryNumber("vG004")
                .cargo(
                    Cargo.builder()
                        .volumeUnitCode("87")
                        .volumeUnitDescription("Descripción libre")
                        .totalVolume(new BigDecimal("20500.50"))
                        .weightUnitCode("87")
                        .weightUnitDescription("Descripción libre")
                        .totalWeight(new BigDecimal("20800.5"))
                        .characteristicCode("1")
                        .characteristicDescription("Descripción libre")
                        .build())
                .build())
        .addAssociatedDocument(
            AssociatedDocument.builder()
                .typeCode("1")
                .typeDescription("Descripción libre")
                .cdc("vH004")
                .printedStamp("vH005")
                .establishment("vH006")
                .dispatchPoint("vH007")
                .documentNumber("vH008")
                .printedDocumentTypeCode("1")
                .printedDocumentTypeDescription("Descripción libre")
                .printedIssueDate(LocalDate.of(2026, 3, 25))
                .withholdingReceiptNumber("vH012")
                .fiscalCreditResolutionNumber("vH013")
                .certificateTypeCode("1")
                .certificateTypeDescription("Descripción libre")
                .certificateNumber("vH016")
                .certificateControlNumber("vH017")
                .mergedRuc("vH018")
                .build())
        .build();
  }

  /** Text expected in the tree for every leaf the emitter informs, by field id. */
  static Map<String, String> expectedTexts() {
    Map<String, String> m = new HashMap<>();
    m.put("B002", "1");
    m.put("B004", "587326098");
    m.put("B005", "vB005");
    m.put("B006", "vB006");
    m.put("C002", "1");
    m.put("C004", "vC004");
    m.put("C005", "001");
    m.put("C006", "001");
    m.put("C007", "0014528");
    m.put("C010", "vC010");
    m.put("C008", "2026-03-14");
    m.put("D002", "2017-01-25T10:11:12");
    m.put("D011", "1");
    m.put("D013", "1");
    m.put("D015", "AED");
    m.put("D017", "1");
    m.put("D018", "2200.500");
    m.put("D019", "1");
    m.put("D031", "113");
    m.put("D101", "44444401");
    m.put("D102", "7");
    m.put("D103", "2");
    m.put("D104", "1");
    m.put("D105", "vD105");
    m.put("D106", "vD106");
    m.put("D107", "vD107");
    m.put("D108", "vD108");
    m.put("D109", "vD109");
    m.put("D110", "vD110");
    m.put("D111", "1");
    m.put("D113", "1");
    m.put("D115", "1");
    m.put("D117", "vD117");
    m.put("D118", "vD118");
    m.put("D119", "vD119");
    m.put("D131", "vD131");
    m.put("D132", "vD132");
    m.put("D141", "1");
    m.put("D143", "vD143");
    m.put("D144", "vD144");
    m.put("D145", "vD145");
    m.put("D201", "1");
    m.put("D202", "1");
    m.put("D203", "MKD");
    m.put("D205", "1");
    m.put("D206", "vD206");
    m.put("D207", "vD207");
    m.put("D208", "1");
    m.put("D210", "vD210");
    m.put("D211", "vD211");
    m.put("D212", "vD212");
    m.put("D213", "vD213");
    m.put("D218", "vD218");
    m.put("D219", "1");
    m.put("D221", "1");
    m.put("D223", "1");
    m.put("D214", "vD214");
    m.put("D215", "vD215");
    m.put("D216", "vD216");
    m.put("D217", "vD217");
    m.put("E011", "1");
    m.put("E013", "2026-03-24");
    m.put("E021", "vE021");
    m.put("E022", "vE022");
    m.put("E023", "vE023");
    m.put("E024", "vE024");
    m.put("E025", "2026-03-01");
    m.put("E401", "1");
    m.put("E601", "1");
    m.put("E606", "1");
    m.put("E608", "9100.5000");
    m.put("E609", "AED");
    m.put("E611", "9400.500");
    m.put("E621", "1");
    m.put("E623", "vE623");
    m.put("E624", "vE624");
    m.put("E625", "vE625");
    m.put("E626", "1");
    m.put("E627", "vE627");
    m.put("E628", "vE628");
    m.put("E629", "vE629");
    m.put("E631", "vE631");
    m.put("E632", "vE632");
    m.put("E641", "1");
    m.put("E643", "vE643");
    m.put("E644", "109");
    m.put("E645", "11000.500");
    m.put("E653", "AED");
    m.put("E651", "11300.50");
    m.put("E652", "2026-03-03");
    m.put("E701", "vE701");
    m.put("E702", "vE702");
    m.put("E703", "vE703");
    m.put("E704", "vE704");
    m.put("E705", "vE705");
    m.put("E706", "vE706");
    m.put("E707", "vE707");
    m.put("E708", "vE708");
    m.put("E709", "87");
    m.put("E711", "12500.50");
    m.put("E712", "MKD");
    m.put("E714", "vE714");
    m.put("E715", "1");
    m.put("E717", "13100.5000");
    m.put("E718", "13200.5");
    m.put("E719", "vE719");
    m.put("E721", "13400.500");
    m.put("E725", "13500.5000");
    m.put("E727", "13600.5");
    m.put("EA002", "13700.50");
    m.put("EA003", "13800.500");
    m.put("EA004", "13900.5000");
    m.put("EA006", "14000.5");
    m.put("EA007", "14100.50");
    m.put("EA008", "14200.500");
    m.put("EA009", "14300.5000");
    m.put("E731", "1");
    m.put("E733", "14600.500");
    m.put("E734", "14700.5000");
    m.put("E735", "14800.5");
    m.put("E736", "14900.50");
    m.put("E737", "15000.500");
    m.put("E751", "vE751");
    m.put("E752", "2026-03-13");
    m.put("E753", "vE753");
    m.put("E754", "vE754");
    m.put("E755", "vE755");
    m.put("E759", "vE759");
    m.put("E760", "vE760");
    m.put("E761", "vE761");
    m.put("E771", "1");
    m.put("E773", "vE773");
    m.put("E774", "vE774");
    m.put("E775", "163");
    m.put("E776", "164");
    m.put("E777", "16500.50");
    m.put("E778", "16600.500");
    m.put("E779", "1");
    m.put("E781", "vE781");
    m.put("E782", "17000.500");
    m.put("E783", "171");
    m.put("E784", "vE784");
    m.put("E785", "173");
    m.put("E786", "vE786");
    m.put("F002", "17500.5000");
    m.put("F003", "17600.5");
    m.put("F004", "17700.50");
    m.put("F005", "17800.500");
    m.put("F008", "17900.5000");
    m.put("F009", "18000.5");
    m.put("F033", "18100.50");
    m.put("F034", "18200.500");
    m.put("F035", "18300.5000");
    m.put("F010", "18400.5");
    m.put("F011", "18500.50");
    m.put("F012", "18600.500");
    m.put("F013", "18700.5000");
    m.put("F025", "18800.5");
    m.put("F014", "18900.50");
    m.put("F015", "19000.500");
    m.put("F016", "19100.5000");
    m.put("F036", "19200.5");
    m.put("F037", "19300.50");
    m.put("F026", "19400.500");
    m.put("F017", "19500.5000");
    m.put("F018", "19600.5");
    m.put("F019", "19700.50");
    m.put("F020", "19800.500");
    m.put("F023", "19900.5000");
    m.put("G002", "vG002");
    m.put("G003", "vG003");
    m.put("G004", "vG004");
    m.put("G051", "87");
    m.put("G053", "20500.50");
    m.put("G054", "87");
    m.put("G056", "20800.5");
    m.put("G057", "1");
    m.put("H002", "1");
    m.put("H004", "vH004");
    m.put("H005", "vH005");
    m.put("H006", "vH006");
    m.put("H007", "vH007");
    m.put("H008", "vH008");
    m.put("H009", "1");
    m.put("H011", "2026-03-25");
    m.put("H012", "vH012");
    m.put("H013", "vH013");
    m.put("H014", "1");
    m.put("H016", "vH016");
    m.put("H017", "vH017");
    m.put("H018", "vH018");
    return m;
  }

  /** Catalog description fields: the model gives a free text, the catalog wins. */
  static List<String> catalogDescriptionIds() {
    return List.of(
        "B003", "C003", "D012", "D014", "D016", "D020", "D032", "D112", "D114", "D116", "D142",
        "D204", "D209", "D220", "D222", "D224", "E012", "E402", "E602", "E607", "E610", "E622",
        "E642", "E654", "E710", "E713", "E716", "E732", "E772", "E780", "G052", "G055", "G058",
        "H003", "H010", "H015");
  }
}
