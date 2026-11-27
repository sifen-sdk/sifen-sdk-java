/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

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
import io.github.sifensdk.core.model.ItemDiscountsAndAdvances;
import io.github.sifensdk.core.model.ItemValues;
import io.github.sifensdk.core.model.ItemVat;
import io.github.sifensdk.core.model.Operation;
import io.github.sifensdk.core.model.OperationCondition;
import io.github.sifensdk.core.model.Payment;
import io.github.sifensdk.core.model.Receiver;
import io.github.sifensdk.core.model.SelfInvoice;
import io.github.sifensdk.core.model.Stamp;
import io.github.sifensdk.core.model.Totals;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Synthetic documents for tests: an electronic invoice and the electronic credit note that returns
 * its first item. Every value is invented (the issuer RUC is the example of the Manual Técnico,
 * 44444401-7) and the amounts follow the SIFEN formulas in guaraníes, with no rounding needed:
 *
 * <ul>
 *   <li>item 1: 1 × 110000, VAT 10 %: taxable base 100000, VAT 10000;
 *   <li>item 2: 2 × 10500 = 21000, VAT 5 %: taxable base 20000, VAT 1000;
 *   <li>invoice total 131000, VAT 11000; credit note and debit note total 110000, VAT 10000;
 *   <li>self-invoice: 2 × 50000 = 100000, without VAT group (C002 = 4, R-E730-P1).
 * </ul>
 */
public final class Samples {

  private Samples() {}

  /**
   * An electronic invoice (C002 = 1), cash sale of goods to a taxpayer (B2B) in guaraníes.
   *
   * @return the invoice
   */
  public static ElectronicDocument invoice() {
    return ElectronicDocument.builder()
        .operation(operation("123456789"))
        .stamp(stamp("1", "0000001"))
        .generalData(
            GeneralData.builder()
                .issueDateTime(LocalDateTime.of(2026, 11, 26, 10, 0, 0))
                .commercialOperation(
                    CommercialOperation.builder()
                        .transactionTypeCode("1")
                        .taxTypeCode("1")
                        .currencyCode("PYG")
                        .build())
                .issuer(issuer())
                .receiver(receiver())
                .build())
        .documentTypeData(
            DocumentTypeData.builder()
                .invoice(Invoice.builder().presenceIndicatorCode("1").build())
                .operationCondition(
                    OperationCondition.builder()
                        .conditionCode("1")
                        .addPayment(
                            Payment.builder()
                                .typeCode("1")
                                .amount(gs("131000"))
                                .currencyCode("PYG")
                                .build())
                        .build())
                .addItem(item10())
                .addItem(item5())
                .build())
        .totals(
            totals("131000")
                .vat5Subtotal(gs("21000"))
                .vat10Subtotal(gs("110000"))
                .vat5Amount(gs("1000"))
                .vat10Amount(gs("10000"))
                .vatTotal(gs("11000"))
                .vat5TaxableBaseTotal(gs("20000"))
                .vat10TaxableBaseTotal(gs("100000"))
                .vatTaxableBaseTotal(gs("120000"))
                .build())
        .build();
  }

  /**
   * An electronic credit note (C002 = 5) for the return (E401 = 2) of the first item of {@link
   * #invoice()}, which it references by CDC.
   *
   * @return the credit note
   */
  public static ElectronicDocument creditNote() {
    return ElectronicDocument.builder()
        .operation(operation("987654321"))
        .stamp(stamp("5", "0000001"))
        .generalData(
            GeneralData.builder()
                .issueDateTime(LocalDateTime.of(2026, 11, 27, 9, 30, 0))
                .commercialOperation(
                    CommercialOperation.builder().taxTypeCode("1").currencyCode("PYG").build())
                .issuer(issuer())
                .receiver(receiver())
                .build())
        .documentTypeData(
            DocumentTypeData.builder()
                .creditDebitNote(CreditDebitNote.builder().reasonCode("2").build())
                .addItem(item10())
                .build())
        .totals(
            totals("110000")
                .vat10Subtotal(gs("110000"))
                .vat10Amount(gs("10000"))
                .vatTotal(gs("10000"))
                .vat10TaxableBaseTotal(gs("100000"))
                .vatTaxableBaseTotal(gs("100000"))
                .build())
        .addAssociatedDocument(
            AssociatedDocument.builder().typeCode("1").cdc(Cdc.of(invoice())).build())
        .build();
  }

  /**
   * An electronic debit note (C002 = 6) for a cost recovery (E401 = 6) on {@link #invoice()}, which
   * it references by CDC. Same structure as {@link #creditNote()}: the specification groups both
   * notes (E400, H001, F014 with C002 = 5 or 6).
   *
   * @return the debit note
   */
  public static ElectronicDocument debitNote() {
    return ElectronicDocument.builder()
        .operation(operation("192837465"))
        .stamp(stamp("6", "0000001"))
        .generalData(
            GeneralData.builder()
                .issueDateTime(LocalDateTime.of(2026, 11, 27, 11, 0, 0))
                .commercialOperation(
                    CommercialOperation.builder().taxTypeCode("1").currencyCode("PYG").build())
                .issuer(issuer())
                .receiver(receiver())
                .build())
        .documentTypeData(
            DocumentTypeData.builder()
                .creditDebitNote(CreditDebitNote.builder().reasonCode("6").build())
                .addItem(item10())
                .build())
        .totals(
            totals("110000")
                .vat10Subtotal(gs("110000"))
                .vat10Amount(gs("10000"))
                .vatTotal(gs("10000"))
                .vat10TaxableBaseTotal(gs("100000"))
                .vatTaxableBaseTotal(gs("100000"))
                .build())
        .addAssociatedDocument(
            AssociatedDocument.builder().typeCode("1").cdc(Cdc.of(invoice())).build())
        .build();
  }

  /**
   * An electronic self-invoice (C002 = 4): the issuer buys 2 units at 50000 from a seller who is
   * not a taxpayer, with a Paraguayan identity card, and documents it itself. What the
   * specification requires for C002 = 4, besides the seller group (E300, R-E300-C1):
   *
   * <ul>
   *   <li>the receiver is the issuer: taxpayer (D201 = 1, R-D201-F1), B2C (D202 = 2, R-D202-F4) and
   *       the issuer's RUC (D206 = D101, R-D206-D1);
   *   <li>currency PYG (R-D015-F2), cash (E601 = 1, R-E601-D1);
   *   <li>no VAT group per item (R-E730-P1); the item total is E721 × E711 (R-EA008-A2);
   *   <li>no subtotals by rate, VAT, commission or guaraní total (R-F002-P4 … R-F026-P4); the gross
   *       total is the sum of the item totals (R-F008-A3);
   *   <li>one associated document, an electronic proof (H002 = 3, R-H002-D1), here of not being a
   *       taxpayer (H014 = 1).
   * </ul>
   *
   * @return the self-invoice
   */
  public static ElectronicDocument selfInvoice() {
    return ElectronicDocument.builder()
        .operation(operation("564738291"))
        .stamp(stamp("4", "0000001"))
        .generalData(
            GeneralData.builder()
                .issueDateTime(LocalDateTime.of(2026, 11, 27, 15, 0, 0))
                .commercialOperation(
                    CommercialOperation.builder()
                        .transactionTypeCode("1")
                        .taxTypeCode("1")
                        .currencyCode("PYG")
                        .build())
                .issuer(issuer())
                .receiver(
                    Receiver.builder()
                        .natureCode("1")
                        .operationTypeCode("2")
                        .countryCode("PRY")
                        .taxpayerTypeCode("2")
                        .ruc("44444401")
                        .checkDigit("7")
                        .name("DE generado en ambiente de prueba - sin valor comercial ni fiscal")
                        .build())
                .build())
        .documentTypeData(
            DocumentTypeData.builder()
                .selfInvoice(seller())
                .operationCondition(
                    OperationCondition.builder()
                        .conditionCode("1")
                        .addPayment(
                            Payment.builder()
                                .typeCode("1")
                                .amount(gs("100000"))
                                .currencyCode("PYG")
                                .build())
                        .build())
                .addItem(
                    Item.builder()
                        .internalCode("C-003")
                        .description("Producto agrícola de ejemplo")
                        .unitOfMeasureCode("77")
                        .quantity(new BigDecimal("2"))
                        .valuation(values("50000", "100000"))
                        .build())
                .build())
        .totals(totals("100000").build())
        .addAssociatedDocument(
            AssociatedDocument.builder().typeCode("3").certificateTypeCode("1").build())
        .build();
  }

  /** A seller who is not a taxpayer (E301 = 1), with a Paraguayan identity card (E304 = 1). */
  private static SelfInvoice seller() {
    return SelfInvoice.builder()
        .sellerNatureCode("1")
        .sellerIdTypeCode("1")
        .sellerIdNumber("1234567")
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

  private static Operation operation(String securityCode) {
    return Operation.builder().emissionTypeCode("1").securityCode(securityCode).build();
  }

  private static Stamp stamp(String documentType, String number) {
    return Stamp.builder()
        .documentTypeCode(documentType)
        .number("12345678")
        .establishment("001")
        .dispatchPoint("001")
        .documentNumber(number)
        .startDate(LocalDate.of(2026, 1, 1))
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

  private static Item item10() {
    return Item.builder()
        .internalCode("A-001")
        .description("Producto de ejemplo gravado al 10 %")
        .unitOfMeasureCode("77")
        .quantity(new BigDecimal("1"))
        .valuation(values("110000", "110000"))
        .vat(vat("10", "100000", "10000"))
        .build();
  }

  private static Item item5() {
    return Item.builder()
        .internalCode("B-002")
        .description("Producto de ejemplo gravado al 5 %")
        .unitOfMeasureCode("77")
        .quantity(new BigDecimal("2"))
        .valuation(values("10500", "21000"))
        .vat(vat("5", "20000", "1000"))
        .build();
  }

  private static ItemValues values(String unitPrice, String total) {
    return ItemValues.builder()
        .unitPrice(gs(unitPrice))
        .grossTotal(gs(total))
        .discountsAndAdvances(
            ItemDiscountsAndAdvances.builder()
                .discountAmount(gs("0"))
                .globalDiscountAmount(gs("0"))
                .advanceAmount(gs("0"))
                .globalAdvanceAmount(gs("0"))
                .total(gs(total))
                .build())
        .build();
  }

  private static ItemVat vat(String rate, String taxableBase, String amount) {
    return ItemVat.builder()
        .treatmentCode("1")
        .taxableProportionRate(new BigDecimal("100"))
        .rate(new BigDecimal(rate))
        .taxableBase(gs(taxableBase))
        .amount(gs(amount))
        .exemptBase(gs("0"))
        .build();
  }

  private static Totals.Builder totals(String total) {
    return Totals.builder()
        .grossTotal(gs(total))
        .itemDiscountTotal(gs("0"))
        .itemGlobalDiscountTotal(gs("0"))
        .itemAdvanceTotal(gs("0"))
        .itemGlobalAdvanceTotal(gs("0"))
        .globalDiscountRate(gs("0"))
        .discountTotal(gs("0"))
        .advanceTotal(gs("0"))
        .roundingAmount(gs("0"))
        .netTotal(gs(total));
  }

  private static BigDecimal gs(String value) {
    return new BigDecimal(value);
  }
}
