/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.calc;

import io.github.sifensdk.core.model.AssociatedDocument;
import io.github.sifensdk.core.model.CommercialOperation;
import io.github.sifensdk.core.model.CreditDebitNote;
import io.github.sifensdk.core.model.DocumentTypeData;
import io.github.sifensdk.core.model.EconomicActivity;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.GeneralData;
import io.github.sifensdk.core.model.Invoice;
import io.github.sifensdk.core.model.Issuer;
import io.github.sifensdk.core.model.Item;
import io.github.sifensdk.core.model.Operation;
import io.github.sifensdk.core.model.OperationCondition;
import io.github.sifensdk.core.model.Payment;
import io.github.sifensdk.core.model.Receiver;
import io.github.sifensdk.core.model.SelfInvoice;
import io.github.sifensdk.core.model.Stamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Synthetic documents for the calculator's tests (data of the Manual's example, no real data). */
final class Documents {

  static final String CDC = "01444444017001001001452822017012515873260988";

  private Documents() {}

  /**
   * A document of the given type (1, 4, 5 or 6) with the items and the payment as given, without
   * totals.
   */
  static ElectronicDocument document(
      String type,
      String currency,
      @Nullable String exchangeRateCondition,
      @Nullable BigDecimal exchangeRate,
      List<Item> items,
      BigDecimal payment) {
    boolean self = type.equals("4");
    CommercialOperation.Builder operation =
        CommercialOperation.builder().taxTypeCode("1").currencyCode(currency);
    if (type.equals("1") || self) {
      operation.transactionTypeCode("1");
    }
    if (!currency.equals("PYG")) {
      operation.exchangeRateConditionCode(exchangeRateCondition);
      if ("1".equals(exchangeRateCondition)) {
        operation.exchangeRate(exchangeRate);
      }
    }
    DocumentTypeData.Builder data = DocumentTypeData.builder();
    switch (type) {
      case "1":
        data.invoice(Invoice.builder().presenceIndicatorCode("1").build());
        break;
      case "4":
        data.selfInvoice(seller());
        break;
      default:
        data.creditDebitNote(
            CreditDebitNote.builder().reasonCode(type.equals("5") ? "2" : "6").build());
    }
    if (type.equals("1") || self) {
      data.operationCondition(
          OperationCondition.builder()
              .conditionCode("1")
              .addPayment(
                  Payment.builder()
                      .typeCode("1")
                      .amount(payment)
                      .currencyCode(currency)
                      .exchangeRate(
                          currency.equals("PYG")
                              ? null
                              : exchangeRate != null ? exchangeRate : new BigDecimal("7300"))
                      .build())
              .build());
    }
    items.forEach(data::addItem);
    ElectronicDocument.Builder document =
        ElectronicDocument.builder()
            .operation(Operation.builder().emissionTypeCode("1").securityCode("123456789").build())
            .stamp(
                Stamp.builder()
                    .documentTypeCode(type)
                    .number("12345678")
                    .establishment("001")
                    .dispatchPoint("001")
                    .documentNumber("0000001")
                    .startDate(LocalDate.of(2026, 1, 1))
                    .build())
            .generalData(
                GeneralData.builder()
                    .issueDateTime(LocalDateTime.of(2026, 11, 27, 10, 0, 0))
                    .commercialOperation(operation.build())
                    .issuer(issuer())
                    .receiver(self ? selfReceiver() : receiver())
                    .build())
            .documentTypeData(data.build());
    if (self) {
      document.addAssociatedDocument(
          AssociatedDocument.builder().typeCode("3").certificateTypeCode("1").build());
    } else if (!type.equals("1")) {
      document.addAssociatedDocument(AssociatedDocument.builder().typeCode("1").cdc(CDC).build());
    }
    return document.build();
  }

  static Item item(int index, BigDecimal quantity, ItemAmounts amounts) {
    return Item.builder()
        .internalCode("I-" + index)
        .description("Producto de ejemplo " + index)
        .unitOfMeasureCode("77")
        .quantity(quantity)
        .values(amounts.values())
        .vat(amounts.vat())
        .build();
  }

  private static SelfInvoice seller() {
    return SelfInvoice.builder()
        .sellerNatureCode("1")
        .sellerIdDocumentTypeCode("1")
        .sellerIdDocumentNumber("1234567")
        .sellerName("Vendedor de Ejemplo")
        .sellerAddress("Calle del Vendedor")
        .sellerHouseNumber("0")
        .sellerDepartmentCode("1")
        .sellerCityCode("1")
        .transactionAddress("Calle de la Transacción")
        .transactionDepartmentCode("1")
        .transactionCityCode("1")
        .build();
  }

  private static Issuer issuer() {
    return Issuer.builder()
        .ruc("44444401")
        .checkDigit("7")
        .taxpayerTypeCode("2")
        .name("DE generado en ambiente de prueba - sin valor comercial ni fiscal")
        .address("Calle de Ejemplo")
        .houseNumber("123")
        .departmentCode("1")
        .districtCode("1")
        .cityCode("1")
        .phone("021000000")
        .email("facturacion@example.com")
        .addEconomicActivity(
            EconomicActivity.builder().code("62010").description("Actividad de ejemplo").build())
        .build();
  }

  private static Receiver receiver() {
    return Receiver.builder()
        .natureCode("1")
        .operationTypeCode("1")
        .countryCode("PRY")
        .taxpayerTypeCode("2")
        .ruc("44444402")
        .checkDigit("5")
        .name("Cliente de Ejemplo S.A.")
        .email("cliente@example.com")
        .build();
  }

  private static Receiver selfReceiver() {
    return Receiver.builder()
        .natureCode("1")
        .operationTypeCode("2")
        .countryCode("PRY")
        .taxpayerTypeCode("2")
        .ruc("44444401")
        .checkDigit("7")
        .name("DE generado en ambiente de prueba - sin valor comercial ni fiscal")
        .build();
  }
}
