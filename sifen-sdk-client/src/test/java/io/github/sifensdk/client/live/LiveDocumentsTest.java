/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.live;

import io.github.sifensdk.client.EventResult;
import io.github.sifensdk.client.ReceptionResult;
import io.github.sifensdk.client.ResultMessage;
import io.github.sifensdk.client.SifenClient;
import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.SifenDocuments;
import io.github.sifensdk.core.SifenEvents;
import io.github.sifensdk.core.cdc.SecurityCode;
import io.github.sifensdk.core.model.AdditionalCommercialData;
import io.github.sifensdk.core.model.AssociatedDocument;
import io.github.sifensdk.core.model.Carrier;
import io.github.sifensdk.core.model.CommercialOperation;
import io.github.sifensdk.core.model.CreditDebitNote;
import io.github.sifensdk.core.model.DeliveryNote;
import io.github.sifensdk.core.model.DocumentTypeData;
import io.github.sifensdk.core.model.Electricity;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.Event;
import io.github.sifensdk.core.model.GeneralData;
import io.github.sifensdk.core.model.Insurance;
import io.github.sifensdk.core.model.InsurancePolicy;
import io.github.sifensdk.core.model.Item;
import io.github.sifensdk.core.model.ItemDiscountsAndAdvances;
import io.github.sifensdk.core.model.ItemValues;
import io.github.sifensdk.core.model.Operation;
import io.github.sifensdk.core.model.OperationCondition;
import io.github.sifensdk.core.model.Payment;
import io.github.sifensdk.core.model.Receiver;
import io.github.sifensdk.core.model.SectorSpecificFields;
import io.github.sifensdk.core.model.SelfInvoice;
import io.github.sifensdk.core.model.Stamp;
import io.github.sifensdk.core.model.Supermarket;
import io.github.sifensdk.core.model.Totals;
import io.github.sifensdk.core.model.Transport;
import io.github.sifensdk.core.model.TransportDelivery;
import io.github.sifensdk.core.model.TransportDeparture;
import io.github.sifensdk.core.model.TransportUpdate;
import io.github.sifensdk.core.model.TransportVehicle;
import io.github.sifensdk.core.signing.Csc;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.KeyMaterials;
import io.github.sifensdk.core.signing.SignedDocument;
import io.github.sifensdk.core.validation.ValidationIssue;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Sends one document of each type and group added in phase 4 to SIFEN's test environment and prints
 * SIFEN's answer: debit note, self-invoice, delivery note (and a transport update event on it), an
 * invoice with transport and one invoice per sector group. Runs only with {@code
 * SIFEN_TEST_DOCUMENTS=true} and the credentials of {@code LiveSifenTest}. Prints only states and
 * codes.
 */
@EnabledIfEnvironmentVariable(named = "SIFEN_TEST_DOCUMENTS", matches = "true")
class LiveDocumentsTest {

  private static final String TEST_NAME =
      "DE generado en ambiente de prueba - sin valor comercial ni fiscal";

  private Properties p;
  private SecureRandom random;
  private long number;

  @Test
  void sendsEachNewDocumentType() throws Exception {
    p = new Properties();
    try (Reader r =
        Files.newBufferedReader(Path.of(env("SIFEN_TEST_ISSUER_FILE")), StandardCharsets.UTF_8)) {
      p.load(r);
    }
    KeyMaterial key =
        KeyMaterials.pkcs12(
            Path.of(env("SIFEN_TEST_P12_PATH")), env("SIFEN_TEST_P12_PASSWORD").toCharArray());
    Csc csc = Csc.of(env("SIFEN_TEST_CSC_ID"), env("SIFEN_TEST_CSC").toCharArray());
    SifenClient client =
        SifenClient.builder().environment(Environment.TEST).keyMaterial(key).build();
    SifenDocuments documents = SifenDocuments.builder().environment(Environment.TEST).build();
    random = new SecureRandom();
    number = (System.currentTimeMillis() / 1000L) % 8_000_000L + 1_000_000L;

    String invoice = send(client, documents, key, csc, "factura base", base());
    send(client, documents, key, csc, "nota de debito", debitNote(invoice));
    send(client, documents, key, csc, "autofactura", selfInvoice());
    // The self-invoice formulas, each with one amount off by 1 (signed despite the SDK's error).
    ElectronicDocument self = selfInvoice();
    sendAnyway(
        client,
        key,
        csc,
        "autofactura F008+1",
        self.toBuilder()
            .totals(
                self.totals().toBuilder()
                    .grossTotal(self.totals().grossTotal().add(BigDecimal.ONE))
                    .netTotal(self.totals().netTotal().add(BigDecimal.ONE))
                    .build())
            .build());
    ElectronicDocument other = selfInvoice();
    Item item = other.documentTypeData().items().get(0);
    ItemValues values = item.valuation();
    sendAnyway(
        client,
        key,
        csc,
        "autofactura EA008+1",
        other.toBuilder()
            .documentTypeData(
                other.documentTypeData().toBuilder()
                    .items(
                        List.of(
                            item.toBuilder()
                                .valuation(
                                    values.toBuilder()
                                        .discountsAndAdvances(
                                            values.discountsAndAdvances().toBuilder()
                                                .total(
                                                    values
                                                        .discountsAndAdvances()
                                                        .total()
                                                        .add(BigDecimal.ONE))
                                                .build())
                                        .build())
                                .build()))
                    .build())
            .build());
    String deliveryNote = send(client, documents, key, csc, "nota de remision", deliveryNote());
    send(client, documents, key, csc, "factura con transporte", withTransport());
    // C-407: the carrier may be left out when the transport is by road and the vehicle is
    // identified by its identification number (E967 = 1); with two vehicles, one by plate.
    ElectronicDocument noCarrier = deliveryNote();
    Transport t = noCarrier.documentTypeData().transport();
    sendAnyway(
        client,
        key,
        csc,
        "remision sin transportista",
        noCarrier.toBuilder()
            .documentTypeData(
                noCarrier.documentTypeData().toBuilder()
                    .transport(t.toBuilder().carrier(null).build())
                    .build())
            .build());
    ElectronicDocument twoVehicles = deliveryNote();
    Transport t2 = twoVehicles.documentTypeData().transport();
    sendAnyway(
        client,
        key,
        csc,
        "remision sin transportista dos vehiculos",
        twoVehicles.toBuilder()
            .documentTypeData(
                twoVehicles.documentTypeData().toBuilder()
                    .transport(
                        t2.toBuilder()
                            .carrier(null)
                            .addVehicle(
                                TransportVehicle.builder()
                                    .type("Camioneta")
                                    .brand("Marca")
                                    .identificationTypeCode("2")
                                    .plateNumber("ABC123")
                                    .build())
                            .build())
                    .build())
            .build());
    send(
        client,
        documents,
        key,
        csc,
        "sector energia",
        withSector(
            SectorSpecificFields.builder()
                .addElectricity(
                    Electricity.builder()
                        .meterNumber("MED-0001")
                        .activityCode("1")
                        .categoryCode("R1")
                        .previousReading(new BigDecimal("12400.00"))
                        .currentReading(new BigDecimal("12750.50"))
                        .consumption(new BigDecimal("350.50"))
                        .build())
                .build()));
    send(
        client,
        documents,
        key,
        csc,
        "sector seguros",
        withSector(
            SectorSpecificFields.builder()
                .insurance(
                    Insurance.builder()
                        .addPolicy(
                            InsurancePolicy.builder()
                                .code("POL-HOGAR")
                                .validityUnitDescription("año")
                                .validityPeriod(BigDecimal.ONE)
                                .number("2026-000001")
                                .build())
                        .build())
                .build()));
    send(
        client,
        documents,
        key,
        csc,
        "sector supermercados",
        withSector(
            SectorSpecificFields.builder()
                .supermarket(
                    Supermarket.builder()
                        .cashierName("Cajero 01")
                        .cashAmount(new BigDecimal("20000"))
                        .changeAmount(new BigDecimal("9000"))
                        .build())
                .build()));
    LocalDate today = LocalDate.now(SifenDocuments.PARAGUAY);
    send(
        client,
        documents,
        key,
        csc,
        "datos adicionales",
        withSector(
            SectorSpecificFields.builder()
                .additionalCommercialData(
                    AdditionalCommercialData.builder()
                        .cycle(today.toString().substring(0, 7))
                        .cycleStartDate(today.withDayOfMonth(1))
                        .cycleEndDate(today.withDayOfMonth(today.lengthOfMonth()))
                        .addPaymentDueDate(today.plusDays(10))
                        .build())
                .build()));

    if (deliveryNote != null) {
      SifenEvents events = SifenEvents.builder().environment(Environment.TEST).build();
      try {
        EventResult result =
            client.sendEvents(
                List.of(
                    events.sign(
                        Event.builder()
                            .id(Long.toString(System.currentTimeMillis() / 1000L))
                            .detail(
                                TransportUpdate.builder()
                                    .cdc(deliveryNote)
                                    .reasonCode("2")
                                    .driverName("Otro chofer de prueba")
                                    .driverIdDocumentNumber("7654321")
                                    .transportModeCode("1")
                                    .build())
                            .build(),
                        key)));
        for (EventResult.Event e : result.events()) {
          System.out.println(
              "VIVO evento de transporte: " + e.status() + " " + codes(e.messages()));
        }
      } catch (RuntimeException e) {
        System.out.println("VIVO evento de transporte: NO ENVIADO " + e.getMessage());
      }
    }
  }

  // ---------------------------------------------------------------- documents

  private ElectronicDocument base() {
    return LiveSifenTest.invoice(p, number++, random);
  }

  private Stamp stamp(String type, String documentNumber) {
    return Stamp.builder()
        .documentTypeCode(type)
        .number(p.getProperty("timbrado"))
        .establishment(p.getProperty("establecimiento"))
        .dispatchPoint(p.getProperty("puntoExpedicion"))
        .documentNumber(documentNumber)
        .startDate(LocalDate.parse(p.getProperty("timbradoInicio")))
        .build();
  }

  private ElectronicDocument retyped(ElectronicDocument document, String type) {
    String n = String.format("%07d", number++);
    return document.toBuilder()
        .stamp(stamp(type, n))
        .operation(
            document.operation().toBuilder().securityCode(SecurityCode.generate(random, n)).build())
        .build();
  }

  private ElectronicDocument debitNote(@Nullable String invoiceCdc) {
    ElectronicDocument b = base();
    ElectronicDocument note =
        b.toBuilder()
            .generalData(
                b.generalData().toBuilder()
                    .commercialOperation(
                        CommercialOperation.builder().taxTypeCode("1").currencyCode("PYG").build())
                    .build())
            .documentTypeData(
                DocumentTypeData.builder()
                    .creditDebitNote(CreditDebitNote.builder().reasonCode("6").build())
                    .items(b.documentTypeData().items())
                    .build())
            .addAssociatedDocument(
                AssociatedDocument.builder()
                    .typeCode("1")
                    .cdc(invoiceCdc == null ? "0".repeat(44) : invoiceCdc)
                    .build())
            .build();
    return retyped(note, "6");
  }

  private ElectronicDocument selfInvoice() {
    ElectronicDocument b = base();
    BigDecimal total = new BigDecimal("100000");
    BigDecimal zero = BigDecimal.ZERO;
    ElectronicDocument self =
        b.toBuilder()
            .generalData(
                b.generalData().toBuilder()
                    .receiver(b.generalData().receiver().toBuilder().operationTypeCode("2").build())
                    .build())
            .documentTypeData(
                DocumentTypeData.builder()
                    .selfInvoice(
                        SelfInvoice.builder()
                            .sellerNatureCode("1")
                            // A made-up passport: a made-up identity card may belong to a
                            // registered taxpayer, and SIFEN rejects it with 2562.
                            .sellerIdTypeCode("2")
                            .sellerIdNumber("SDKPRUEBA1")
                            .sellerName("Vendedor de prueba")
                            .sellerAddress("Calle del Vendedor")
                            .sellerHouseNumber("0")
                            .sellerDepartmentCode(p.getProperty("departamento"))
                            .sellerCityCode(p.getProperty("ciudad"))
                            .transactionAddress("Calle de la Transacción")
                            .transactionDepartmentCode(p.getProperty("departamento"))
                            .transactionCityCode(p.getProperty("ciudad"))
                            .build())
                    .operationCondition(
                        OperationCondition.builder()
                            .conditionCode("1")
                            .addPayment(
                                Payment.builder()
                                    .typeCode("1")
                                    .amount(total)
                                    .currencyCode("PYG")
                                    .build())
                            .build())
                    .addItem(
                        Item.builder()
                            .internalCode("SDK-AFE")
                            .description("Producto de prueba")
                            .unitOfMeasureCode("77")
                            .quantity(new BigDecimal("2"))
                            .valuation(
                                ItemValues.builder()
                                    .unitPrice(new BigDecimal("50000"))
                                    .grossTotal(total)
                                    .discountsAndAdvances(
                                        ItemDiscountsAndAdvances.builder()
                                            .discountAmount(zero)
                                            .globalDiscountAmount(zero)
                                            .advanceAmount(zero)
                                            .globalAdvanceAmount(zero)
                                            .total(total)
                                            .build())
                                    .build())
                            .build())
                    .build())
            .totals(
                Totals.builder()
                    .grossTotal(total)
                    .itemDiscountTotal(zero)
                    .itemGlobalDiscountTotal(zero)
                    .itemAdvanceTotal(zero)
                    .itemGlobalAdvanceTotal(zero)
                    .globalDiscountRate(zero)
                    .discountTotal(zero)
                    .advanceTotal(zero)
                    .roundingAmount(zero)
                    .netTotal(total)
                    .build())
            .addAssociatedDocument(
                AssociatedDocument.builder().typeCode("3").certificateTypeCode("1").build())
            .build();
    return retyped(self, "4");
  }

  private ElectronicDocument deliveryNote() {
    ElectronicDocument b = base();
    LocalDate today = LocalDate.now(SifenDocuments.PARAGUAY);
    Receiver receiver =
        b.generalData().receiver().toBuilder()
            .address(p.getProperty("direccion"))
            .houseNumber(p.getProperty("numeroCasa"))
            .departmentCode(p.getProperty("departamento"))
            .districtCode(p.getProperty("distrito"))
            .cityCode(p.getProperty("ciudad"))
            .build();
    ElectronicDocument note =
        b.toBuilder()
            .operation(
                Operation.builder()
                    .emissionTypeCode("1")
                    .securityCode("000000001")
                    .taxAuthorityInformation("Documento de prueba del SIFEN SDK")
                    .build())
            .generalData(
                GeneralData.builder()
                    .issueDateTime(
                        LocalDateTime.now(SifenDocuments.PARAGUAY).truncatedTo(ChronoUnit.SECONDS))
                    .issuer(b.generalData().issuer())
                    .receiver(receiver)
                    .build())
            .documentTypeData(
                DocumentTypeData.builder()
                    .deliveryNote(
                        DeliveryNote.builder()
                            .reasonCode("1")
                            .responsibleCode("1")
                            .estimatedKilometers(15)
                            .futureInvoiceDate(today)
                            .build())
                    .addItem(
                        Item.builder()
                            .internalCode("SDK-NRE")
                            .description("Producto de prueba")
                            .unitOfMeasureCode("77")
                            .quantity(new BigDecimal("10"))
                            .goodsRelevanceCode("1")
                            .breakageOrShrinkageQuantity(BigDecimal.ZERO)
                            .breakageOrShrinkageRate(BigDecimal.ZERO)
                            .build())
                    .transport(transport(today, true))
                    .build())
            .totals(null)
            .build();
    return retyped(note, "7");
  }

  private ElectronicDocument withTransport() {
    ElectronicDocument b = base();
    return b.toBuilder()
        .documentTypeData(
            b.documentTypeData().toBuilder()
                .transport(transport(LocalDate.now(SifenDocuments.PARAGUAY), false))
                .build())
        .build();
  }

  private Transport transport(LocalDate today, boolean full) {
    Transport.Builder t =
        Transport.builder()
            .modeCode("1")
            .freightPayerCode("1")
            .estimatedStartDate(today)
            .estimatedEndDate(today.plusDays(1))
            .addDeliveryLocation(
                TransportDelivery.builder()
                    .address(p.getProperty("direccion"))
                    .houseNumber(p.getProperty("numeroCasa"))
                    .departmentCode(p.getProperty("departamento"))
                    .districtCode(p.getProperty("distrito"))
                    .cityCode(p.getProperty("ciudad"))
                    .build());
    if (!full) {
      return t.addVehicle(
              TransportVehicle.builder()
                  .type("Camioneta")
                  .brand("Marca")
                  .identificationTypeCode("2")
                  .plateNumber("ABC123")
                  .build())
          .build();
    }
    return t.typeCode("1")
        .departureLocation(
            TransportDeparture.builder()
                .address(p.getProperty("direccion"))
                .houseNumber(p.getProperty("numeroCasa"))
                .departmentCode(p.getProperty("departamento"))
                .districtCode(p.getProperty("distrito"))
                .cityCode(p.getProperty("ciudad"))
                .build())
        .addVehicle(
            TransportVehicle.builder()
                .type("Camión")
                .brand("Marca")
                .identificationTypeCode("1")
                .identificationNumber("VIN0000000000001")
                .build())
        .carrier(
            Carrier.builder()
                .natureCode("2")
                .name("Transportista de prueba")
                .idDocumentTypeCode("1")
                .idDocumentNumber("1234567")
                .driverIdDocumentNumber("1234567")
                .driverName("Chofer de prueba")
                .taxAddress("Calle del Transportista")
                .driverAddress("Calle del Chofer")
                .build())
        .build();
  }

  private ElectronicDocument withSector(SectorSpecificFields sector) {
    ElectronicDocument b = base();
    return b.toBuilder()
        .documentTypeData(b.documentTypeData().toBuilder().sectorSpecificFields(sector).build())
        .build();
  }

  // ---------------------------------------------------------------- sending

  private static boolean selected(String label) {
    String only = System.getenv("SIFEN_TEST_DOCUMENTS_ONLY");
    return only == null || only.isBlank() || label.matches(only);
  }

  private static @Nullable String send(
      SifenClient client,
      SifenDocuments documents,
      KeyMaterial key,
      Csc csc,
      String label,
      ElectronicDocument document) {
    if (!selected(label)) {
      return null;
    }
    List<ValidationIssue> errors = documents.validate(document).errors();
    if (!errors.isEmpty()) {
      System.out.println(
          "VIVO "
              + label
              + ": NO VALIDA "
              + errors.stream()
                  .map(i -> i.ruleId() + " " + i.path())
                  .collect(Collectors.joining(" | ")));
      return null;
    }
    try {
      SignedDocument signed = documents.sign(document, key, csc);
      ReceptionResult r = client.send(signed);
      System.out.println(
          "VIVO " + label + ": " + r.document().status() + " " + codes(r.document().messages()));
      return signed.cdc();
    } catch (RuntimeException e) {
      System.out.println("VIVO " + label + ": NO ENVIADO " + e.getMessage());
      return null;
    }
  }

  private static void sendAnyway(
      SifenClient client, KeyMaterial key, Csc csc, String label, ElectronicDocument document) {
    if (!selected(label)) {
      return;
    }
    SifenDocuments plain = SifenDocuments.builder().environment(Environment.TEST).build();
    java.util.Set<String> flagged =
        plain.validate(document).issues().stream()
            .map(ValidationIssue::ruleId)
            .collect(Collectors.toSet());
    SifenDocuments lenient =
        SifenDocuments.builder().environment(Environment.TEST).warnings(flagged).build();
    ReceptionResult r = client.send(lenient.sign(document, key, csc));
    System.out.println(
        "VIVO "
            + label
            + ": SDK "
            + flagged
            + " | "
            + r.document().status()
            + " "
            + codes(r.document().messages()));
  }

  private static String codes(List<ResultMessage> messages) {
    return messages.stream()
        .map(m -> m.code() + " " + m.message())
        .collect(Collectors.joining(" | "));
  }

  private static String env(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing environment variable " + name);
    }
    return value;
  }
}
