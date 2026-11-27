/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.model.Carrier;
import io.github.sifensdk.core.model.DeliveryNote;
import io.github.sifensdk.core.model.DocumentTypeData;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.GeneralData;
import io.github.sifensdk.core.model.Item;
import io.github.sifensdk.core.model.Operation;
import io.github.sifensdk.core.model.Receiver;
import io.github.sifensdk.core.model.Stamp;
import io.github.sifensdk.core.model.Transport;
import io.github.sifensdk.core.model.TransportDelivery;
import io.github.sifensdk.core.model.TransportDeparture;
import io.github.sifensdk.core.model.TransportVehicle;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Synthetic documents with transport of goods: an electronic delivery note (C002 = 7) and the
 * invoice of {@link Samples} with a transport group. Every value is invented; issuer and receiver
 * are the ones of {@link Samples} (the issuer RUC is the example of the Manual Técnico,
 * 44444401-7).
 */
public final class TransportSamples {

  private TransportSamples() {}

  /**
   * A minimal electronic delivery note (C002 = 7): goods moved for a sale (E501 = 1) whose invoice
   * will be issued later in the same month (E506, required by R-E506-C1 without associated
   * documents), by road with the issuer's own truck. Following the rules with C002 = 7 it has no
   * commercial operation (R-D010-P1), no operation condition (R-E600-P1), no item values or VAT
   * (R-E720-P1, R-E730-P1) and no totals (R-F001-P1); it carries the information for the tax
   * authority (R-B006-C1), the receiver address (R-D213-C1), the goods relevance code of each item
   * (R-E715-C1) with its breakage quantity and rate (R-E717-C1, R-E718-C1) and the transport with
   * departure, delivery, vehicle and carrier (R-E900-C1, R-E920-C1, R-E940-C1, R-E960-C1,
   * R-E980-C1).
   *
   * <p>The text of {@code dInfoFisc} (B006) is a placeholder: the issuer writes the message that
   * Resolución General 41/2014, art. 3, inc. 7 requires.
   *
   * @return the delivery note
   */
  public static ElectronicDocument deliveryNote() {
    GeneralData invoiceData = Samples.invoice().generalData();
    return ElectronicDocument.builder()
        .operation(
            Operation.builder()
                .emissionTypeCode("1")
                .securityCode("246813579")
                .taxAuthorityInformation("Texto sintético de prueba según RG 41/2014")
                .build())
        .stamp(
            Stamp.builder()
                .documentTypeCode("7")
                .number("12345678")
                .establishment("001")
                .dispatchPoint("001")
                .documentNumber("0000001")
                .startDate(LocalDate.of(2026, 1, 1))
                .build())
        .generalData(
            GeneralData.builder()
                .issueDateTime(LocalDateTime.of(2026, 11, 26, 8, 0, 0))
                .issuer(invoiceData.issuer())
                .receiver(receiverWithAddress(invoiceData.receiver()))
                .build())
        .documentTypeData(
            DocumentTypeData.builder()
                .deliveryNote(
                    DeliveryNote.builder()
                        .reasonCode("1")
                        .responsibleCode("1")
                        .estimatedKilometers(15)
                        .futureInvoiceDate(LocalDate.of(2026, 11, 30))
                        .build())
                .addItem(
                    Item.builder()
                        .internalCode("A-001")
                        .description("Producto de ejemplo")
                        .unitOfMeasureCode("77")
                        .quantity(new BigDecimal("10"))
                        .goodsRelevanceCode("1")
                        .breakageOrShrinkageQuantity(new BigDecimal("0"))
                        .breakageOrShrinkageRate(new BigDecimal("0"))
                        .build())
                .transport(
                    Transport.builder()
                        .typeCode("1")
                        .modeCode("1")
                        .freightPayerCode("1")
                        .estimatedStartDate(LocalDate.of(2026, 11, 26))
                        .estimatedEndDate(LocalDate.of(2026, 11, 27))
                        .departureLocation(
                            TransportDeparture.builder()
                                .address("Calle de Ejemplo")
                                .houseNumber("123")
                                .departmentCode("1")
                                .districtCode("1")
                                .cityCode("1")
                                .build())
                        .addDeliveryLocation(delivery())
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
                                .name("Transportista de Ejemplo")
                                .idDocumentTypeCode("1")
                                .idDocumentNumber("1234567")
                                .driverIdDocumentNumber("1234567")
                                .driverName("Chofer de Ejemplo")
                                .taxAddress("Calle del Transportista 456")
                                .driverAddress("Calle del Chofer 789")
                                .build())
                        .build())
                .build())
        .build();
  }

  /**
   * The invoice of {@link Samples#invoice()} with the goods delivered by road (E900): transport is
   * optional in an invoice, and so are departure and carrier; here the delivery location and a
   * vehicle identified by its plate (E967 = 2, R-E965-C1).
   *
   * @return the invoice with transport
   */
  public static ElectronicDocument invoiceWithTransport() {
    ElectronicDocument invoice = Samples.invoice();
    return invoice.toBuilder()
        .documentTypeData(
            invoice.documentTypeData().toBuilder()
                .transport(
                    Transport.builder()
                        .modeCode("1")
                        .freightPayerCode("1")
                        .estimatedStartDate(LocalDate.of(2026, 11, 26))
                        .estimatedEndDate(LocalDate.of(2026, 11, 26))
                        .addDeliveryLocation(delivery())
                        .addVehicle(
                            TransportVehicle.builder()
                                .type("Camioneta")
                                .brand("Marca")
                                .identificationTypeCode("2")
                                .plateNumber("ABC123")
                                .build())
                        .build())
                .build())
        .build();
  }

  private static TransportDelivery delivery() {
    return TransportDelivery.builder()
        .address("Avenida de Ejemplo")
        .houseNumber("456")
        .departmentCode("1")
        .districtCode("1")
        .cityCode("1")
        .build();
  }

  private static Receiver receiverWithAddress(Receiver receiver) {
    return receiver.toBuilder()
        .address("Avenida de Ejemplo")
        .houseNumber("456")
        .departmentCode("1")
        .districtCode("1")
        .cityCode("1")
        .build();
  }
}
